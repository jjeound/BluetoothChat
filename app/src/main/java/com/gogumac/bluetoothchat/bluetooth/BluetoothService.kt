// 🔹 패키지 선언 (현재 BluetoothService 클래스가 속한 패키지)
package com.gogumac.bluetoothchat.bluetooth

// 🔹 Android에서 블루투스 기능을 사용하기 위한 다양한 라이브러리 임포트
import android.annotation.SuppressLint  // 특정 경고를 억제하기 위한 어노테이션
import android.app.Activity  // 액티비티 관련 클래스 (블루투스 UI 제어 시 필요)
import android.bluetooth.BluetoothAdapter  // 블루투스 어댑터 (블루투스 기본 제어)
import android.bluetooth.BluetoothDevice  // 블루투스 장치 정보를 나타내는 클래스
import android.bluetooth.BluetoothServerSocket  // 블루투스 서버 소켓 (연결 수락 시 사용)
import android.bluetooth.BluetoothSocket  // 블루투스 소켓 (데이터 송수신 시 사용)
import android.companion.AssociationRequest  // 블루투스 장치 연결 요청을 위한 클래스
import android.companion.BluetoothDeviceFilter  // 블루투스 장치 필터 (특정 장치 검색 시 사용)
import android.companion.CompanionDeviceManager  // Android Companion API를 위한 장치 관리자
import android.companion.CompanionDeviceManager.EXTRA_DEVICE  // 장치 정보를 전달하기 위한 상수
import android.content.BroadcastReceiver  // 브로드캐스트 리시버 (이벤트 감지)
import android.content.Context  // 애플리케이션의 전반적인 환경 정보 제공
import android.content.Intent  // 안드로이드 인텐트 (새로운 화면 전환 및 동작 수행)
import android.content.IntentFilter  // 특정 이벤트를 감지하는 필터
import android.content.IntentSender  // 인텐트 전송을 위한 클래스
import android.os.Build  // 안드로이드 SDK 버전 확인을 위한 클래스
import android.os.ParcelUuid  // UUID(고유 식별자) 데이터를 다루는 클래스
import android.util.Log  // 로그 출력 (디버깅용)
import android.widget.Toast  // 사용자에게 간단한 메시지를 표시하는 UI 요소
import androidx.activity.ComponentActivity  // Jetpack Compose와 함께 사용할 액티비티
import androidx.activity.result.ActivityResultLauncher  // 액티비티 결과를 처리하는 클래스
import androidx.activity.result.IntentSenderRequest  // 인텐트 전송 요청을 위한 클래스
import androidx.activity.result.contract.ActivityResultContracts  // 액티비티 실행 계약 클래스
import androidx.lifecycle.DefaultLifecycleObserver  // 생명주기 이벤트를 감지하는 인터페이스
import androidx.lifecycle.LifecycleOwner  // 생명주기 소유자 (Activity, Fragment 등)
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException  // 입출력 관련 예외 처리 클래스
import java.util.UUID  // UUID(고유 식별자) 생성 및 관리
import java.util.concurrent.Executor  // 비동기 작업 실행을 위한 Executor

// ✅ 디버깅용 로그 태그 정의
private const val TAG = "BLUETOOTH_DEBUG_TAG"

/**
 * 📌 **블루투스 서비스 클래스**
 * - 블루투스 장치 검색, 페어링, 연결, 데이터 전송을 담당하는 핵심 클래스
 * - Android의 Bluetooth API를 활용하여 블루투스 기능을 제어
 * - Jetpack Compose 및 Lifecycle과 함께 동작하도록 설계됨
 *
 * @param bluetoothAdapter 블루투스 어댑터 객체 (Bluetooth 기능을 제어하는 핵심 객체)
 * @param activity 블루투스를 사용하는 액티비티 (Lifecycle 관리를 위해 사용)
 */
@SuppressLint("MissingPermission")  // 블루투스 권한 관련 경고 억제
class BluetoothService(
    private val bluetoothAdapter: BluetoothAdapter,  // 블루투스 어댑터
    private val activity: ComponentActivity? = null  // 사용 중인 액티비티 (생명주기 관리용)
) : DefaultLifecycleObserver {  // 생명주기 이벤트 감지를 위해 DefaultLifecycleObserver 구현

    // ✅ 현재 블루투스 상태를 저장하는 StateFlow (초기 상태: 연결 없음)
    private val _state = MutableStateFlow(BluetoothState.STATE_NONE)
    val state: StateFlow<BluetoothState> = _state  // 외부에서 읽을 수 있도록 StateFlow로 변환

    // ✅ 블루투스 페어링 상태를 관리하는 StateFlow
    private val pairingState = MutableStateFlow(BluetoothState.STATE_NONE)

    // ✅ 블루투스 연결 UUID 설정 (RFCOMM 통신을 위한 고유 식별자)
    private val myUUID =
        ParcelUuid(UUID.fromString("5dc96d32-4d88-43c5-b096-f40d4623e985"))  // UUID 생성

    // ✅ 블루투스 채팅 서비스의 이름 (서버 소켓에서 사용됨)
    private val NAME = "BluetoothChat"

    // ✅ 검색된 블루투스 장치 목록을 관리하는 MutableStateFlow
    private val _discoveredDevices = MutableStateFlow(mutableSetOf<BluetoothDevice>())
    val discoveredDevices: StateFlow<MutableSet<BluetoothDevice>> = _discoveredDevices

    // ✅ 블루투스 이벤트를 수신하기 위한 IntentFilter 설정
    private val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)  // 장치 발견 시
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)  // 검색 시작 시
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)  // 검색 종료 시
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)  // 페어링 상태 변경 시
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)  // 블루투스 연결이 끊어질 때
    }

    // ✅ 블루투스 서버 소켓 (연결 요청을 수락하는 역할)
    private var serverSocket: BluetoothServerSocket? = null

    // ✅ 클라이언트와 연결된 블루투스 소켓
    private var connectSocket: BluetoothSocket? = null

    // ✅ 현재 연결된 블루투스 장치를 관리하는 StateFlow
    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice

    // ✅ 페어링된 블루투스 장치 목록을 관리하는 StateFlow
    private val _pairedDeviceList = MutableStateFlow(bluetoothAdapter.bondedDevices.toList())
    val pairedDeviceList: StateFlow<List<BluetoothDevice>> = _pairedDeviceList

    // ✅ 블루투스 메시지를 관리하는 SharedFlow (메시지 전송을 위한 스트림)
    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    // ✅ 블루투스 연결을 관리하는 CoroutineScope (백그라운드에서 실행)
    private val connectingScope = CoroutineScope(Job() + Dispatchers.IO)

    // ✅ 특정 블루투스 장치와 연결하는 Job을 관리하는 Map
    private val connectingDeviceJobMap = mutableMapOf<String, Job>()

    // ✅ 블루투스 장치 필터 설정 (페어링 요청 시 사용)
    private val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder().build()

    /**
     * 📌 블루투스 장치 검색을 위한 **BroadcastReceiver**
     * - 블루투스 장치 검색 및 연결 관련 이벤트를 감지하여 적절한 처리를 수행
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action  // 수신된 인텐트의 액션을 가져옴

            when (action) {
                // ✅ 블루투스 장치가 발견되었을 때
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= 33)
                        p1.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    else
                        p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    Log.d(TAG, "foundedDevice : ${device?.name} ${device?.address}")

                    device?.let {
                        // 발견된 장치를 `_discoveredDevices` 목록에 추가
                        _discoveredDevices.value = mutableSetOf<BluetoothDevice>().apply {
                            addAll(_discoveredDevices.value)
                            add(device)
                        }
                    }
                }

                // ✅ 블루투스 검색이 시작되었을 때
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _state.value = BluetoothState.STATE_DISCOVERING  // 상태 업데이트
                }

                // ✅ 블루투스 검색이 종료되었을 때
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _state.value = BluetoothState.STATE_DISCOVERING_FINISHED  // 상태 업데이트
                }

                // ✅ 블루투스 장치의 페어링 상태가 변경되었을 때
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = if (Build.VERSION.SDK_INT >= 33)
                        p1.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    else
                        p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    Log.d(TAG, "pairedDevice : ${device?.name} ${device?.address}")

                    // 페어링 상태 값 가져오기
                    val bondState =
                        p1.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                    when (bondState) {
                        BluetoothDevice.BOND_NONE -> {  // 페어링 해제됨
                            Log.d(TAG, "none")
                            pairingState.value = BluetoothState.STATE_NONE
                        }

                        BluetoothDevice.BOND_BONDING -> {  // 페어링 진행 중
                            Log.d(TAG, "bonding")
                            pairingState.value = BluetoothState.STATE_BONDING
                        }

                        BluetoothDevice.BOND_BONDED -> {  // 페어링 완료됨
                            Log.d(TAG, "bonded")
                            pairingState.value = BluetoothState.STATE_BONDED
                            device?.let {
                                _pairedDeviceList.value = pairedDeviceList.value + device  // 목록 갱신
                            }
                        }
                    }
                }

                // ✅ 블루투스 연결이 끊어졌을 때
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device = if (Build.VERSION.SDK_INT >= 33)
                        p1.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    else
                        p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (device?.address == connectedDevice.value?.address) {
                        finishConnect()  // 연결 종료 처리
                    }
                }
            }
        }
    }

    // ✅ 블루투스 검색 및 연결을 위한 ActivityResultLauncher 초기화
    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var bluetoothDiscoverableLauncher: ActivityResultLauncher<Intent>

    // ✅ 블루투스 장치 필터를 적용한 AssociationRequest 생성
    private val pairingRequest: AssociationRequest =
        AssociationRequest.Builder().addDeviceFilter(deviceFilter).build()

    // ✅ Companion Device Manager 초기화 (Android Companion API 사용)
    private var deviceManager: CompanionDeviceManager =
        activity?.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

    /**
     * 📌 CompanionDeviceManager Callback
     * - 장치 검색, 페어링 요청 시 결과를 처리하는 콜백
     */
    private val discoveringCallback: CompanionDeviceManager.Callback =
        object : CompanionDeviceManager.Callback() {
            // ✅ 페어링 대기 상태일 때 (페어링을 사용자에게 요청)
            override fun onAssociationPending(intentSender: IntentSender) {
                super.onAssociationPending(intentSender)
                if (Build.VERSION.SDK_INT >= 33) {
                    Log.d(TAG, "associate success (scan success)")
                    val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                    bluetoothScanLauncher.launch(intentSenderRequest)  // 페어링 요청 실행
                }
            }

            // ✅ 페어링 실패 시
            override fun onFailure(p0: CharSequence?) {
                Log.d(TAG, "associate fail")
            }

            // ✅ 장치가 검색되었을 때
            override fun onDeviceFound(intentSender: IntentSender) {
                super.onDeviceFound(intentSender)
                if (Build.VERSION.SDK_INT < 33) {
                    Log.d(TAG, "associate success (scan success)")
                    val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                    bluetoothScanLauncher.launch(intentSenderRequest)  // 페어링 요청 실행
                }
            }
        }

    // ✅ 클래스 초기화 (생명주기 이벤트 등록 및 블루투스 활성화 확인)
    init {
        activity?.lifecycle?.addObserver(this)  // 생명주기 옵저버 등록

        // 블루투스가 활성화되어 있는지 확인
        if (!bluetoothAdapter.isEnabled) {
            _state.value = BluetoothState.STATE_DISABLE
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            // bluetoothEnableLauncher.launch(enableBtIntent)  // 블루투스 활성화 요청 (주석 처리됨)
        }

        // ✅ 블루투스 상태 변경 로그 출력
        CoroutineScope(Dispatchers.Main).launch {
            _state.collect {
                Log.d("checkfor", "state changed : $it")
            }
        }

        // ✅ 현재 연결된 장치 정보 변경 로그 출력
        CoroutineScope(Dispatchers.Main).launch {
            _connectedDevice.collect {
                Log.d(TAG, "connectedDevice changed : $it, ${it?.name}")
            }
        }
    }


    /**
     * 📌 onCreate()
     * - 액티비티가 생성될 때 실행되며 블루투스 이벤트 리시버를 등록하고
     *   블루투스 검색 및 연결을 위한 ActivityResultLauncher를 설정함.
     */
    override fun onCreate(owner: LifecycleOwner) {
        activity?.let {
            // ✅ 블루투스 이벤트 리시버 등록
            it.registerReceiver(receiver, filter)

            // ✅ 블루투스 스캔(ActivityResultLauncher) 초기화
            bluetoothScanLauncher =
                it.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    Log.d(TAG, "bluetoothScan finish")

                    if (result.resultCode == Activity.RESULT_OK) {
                        val device =
                            if (Build.VERSION.SDK_INT >= 33) result.data?.extras?.getParcelable(
                                EXTRA_DEVICE, BluetoothDevice::class.java
                            ) else result.data?.extras?.get(EXTRA_DEVICE) as BluetoothDevice?

                        Log.d(TAG, "Request pairing device name : ${device?.name}")

                        // ✅ 선택된 장치와 페어링 시도
                        device?.createBond()
                    } else {
                        Log.d(TAG, "scan canceled")
                    }
                }

            // ✅ 블루투스 검색 가능 상태 설정 (discoverable)
            bluetoothDiscoverableLauncher =
                it.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        Log.d(TAG, "discoverable finish")
                    } else {
                        Log.d(TAG, "discoverable canceled")
                    }
                }
        }
        super.onCreate(owner)
    }

    /**
     * 📌 onDestroy()
     * - 액티비티가 종료될 때 실행되며, 블루투스 이벤트 리시버를 해제하고
     *   코루틴을 종료하여 자원을 정리함.
     */
    override fun onDestroy(owner: LifecycleOwner) {
        activity?.unregisterReceiver(receiver)  // ✅ 블루투스 이벤트 리시버 해제
        connectingScope.cancel()  // ✅ 연결 관리 코루틴 취소
        super.onDestroy(owner)
    }

    /**
     * 📌 블루투스를 검색 가능 상태로 변경하는 함수
     * - BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE을 사용하여 기기를
     *   검색 가능하도록 설정함.
     */
    fun setBluetoothDiscoverable() {
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        bluetoothDiscoverableLauncher.launch(discoverableIntent)  // ✅ 검색 가능 모드 활성화 요청
    }

    /**
     * 📌 블루투스 검색을 시작하는 함수
     * - CompanionDeviceManager API를 이용하여 블루투스 장치를 검색하고 페어링 요청을 수행
     */
    fun startDiscovering(activity: Activity) {
        Log.d(TAG, "discovering state : ${bluetoothAdapter.isDiscovering} findStart!")

        if (!bluetoothAdapter.isDiscovering) {
            _state.value = BluetoothState.STATE_DISCOVERING  // ✅ 상태 업데이트
            val executor: Executor = Executor { it.run() }

            // ✅ Android 13(API 33) 이상에서는 executor를 사용
            if (Build.VERSION.SDK_INT >= 33) {
                deviceManager.associate(pairingRequest, executor, discoveringCallback)
            } else {
                deviceManager.associate(pairingRequest, discoveringCallback, null)
            }

            activity.registerReceiver(receiver, filter)  // ✅ 이벤트 리시버 등록
        }
    }

    /**
     * 📌 블루투스 장치와 페어링을 요청하는 함수
     * - 선택된 블루투스 장치와 페어링을 시도하며, 페어링 상태를 Flow로 반환함.
     */
    fun requestPairing(address: String): Flow<BluetoothState> {
        val device = bluetoothAdapter.getRemoteDevice(address)  // ✅ MAC 주소를 통해 장치 가져오기
        device.createBond()  // ✅ 페어링 요청
        pairingState.value = BluetoothState.STATE_START_BOND
        return pairingState
    }

    /**
     * 📌 블루투스 서버 소켓을 여는 함수
     * - BluetoothAdapter.listenUsingRfcommWithServiceRecord()를 사용하여
     *   블루투스 서버 소켓을 생성하고, 클라이언트의 연결 요청을 수락함.
     */
    suspend fun openServerSocket() = withContext(Dispatchers.IO) {
        // ✅ 기존에 열려 있던 서버 소켓 및 연결 소켓을 닫음 (중복 방지)
        serverSocket?.close()
        connectSocket?.close()

        try {
            // ✅ 블루투스 서버 소켓 생성
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, myUUID.uuid)
            Log.d(TAG, "open ServerSocket : $serverSocket")
            _state.value = BluetoothState.STATE_OPEN_SERVER_SOCKET  // ✅ 상태 업데이트

            // ✅ 클라이언트 연결을 기다림
            connectSocket = serverSocket?.accept()

            connectSocket?.also {
                serverSocket?.close()  // ✅ 연결이 성립되면 서버 소켓 닫기
            }
        } catch (e: IOException) {
            Log.e(TAG, "open ServerSocket fail : $e")
            return@withContext null
        }
        return@withContext connectSocket?.remoteDevice
    }

    /**
     * 📌 클라이언트의 연결을 수락하는 함수
     * - 서버 소켓을 통해 연결된 클라이언트 장치를 등록하고 상태를 업데이트함.
     */
    fun acceptConnect() {
        connectSocket?.let {
            _connectedDevice.value = it.remoteDevice  // ✅ 연결된 장치 정보 저장
            _state.value = BluetoothState.STATE_CONNECTED  // ✅ 상태 업데이트

            val device = it.remoteDevice
            if (device.address in connectingDeviceJobMap) {
                connectingDeviceJobMap[device.address]?.cancel()  // ✅ 기존 연결 취소
            }

            // ✅ 클라이언트로부터 메시지 수신을 위한 코루틴 실행
            connectingDeviceJobMap[device.address] =
                connectingScope.launch { listenMessage(it) }

            Log.d(TAG, "connect success.\n connected with ${it.remoteDevice}")
        }
    }

    /**
     * 📌 클라이언트의 연결을 거부하는 함수
     * - 현재 블루투스 소켓을 닫아 연결을 종료함.
     */
    fun rejectConnect() {
        connectSocket?.close()  // ✅ 연결 닫기
        connectSocket = null
    }

    /**
     * 📌 블루투스 서버 소켓을 닫는 함수
     * - BluetoothServerSocket을 닫아 더 이상 연결 요청을 받지 않도록 함.
     */
    fun closeServerSocket() {
        if (serverSocket != null) {
            Log.d(TAG, "close ServerSocket : $serverSocket")
            serverSocket?.close()  // ✅ 서버 소켓 닫기
        }
    }

    /**
     * 📌 특정 블루투스 장치와 연결을 요청하는 함수
     * - BluetoothSocket을 사용하여 특정 블루투스 장치에 직접 연결을 시도함.
     */
    suspend fun requestConnect(address: String) = withContext(Dispatchers.IO) {
        // ✅ 기존 연결이 존재하면 닫기
        if (connectSocket != null) {
            connectSocket?.close()
        }
        if (serverSocket != null) {
            serverSocket?.close()
        }

        val device = bluetoothAdapter.getRemoteDevice(address)  // ✅ 연결할 장치 가져오기
        connectSocket = device.createRfcommSocketToServiceRecord(myUUID.uuid)

        try {
            connectSocket?.connect()  // ✅ 블루투스 연결 시도
            _connectedDevice.value = connectSocket?.remoteDevice
            Log.d(TAG, "connect success.\n connected with $device")
            _state.value = BluetoothState.STATE_CONNECTED

            if (address in connectingDeviceJobMap) {
                connectingDeviceJobMap[address]?.cancel()  // ✅ 기존 연결 작업 취소
            }

            // ✅ 메시지 수신을 위한 코루틴 실행
            connectingDeviceJobMap[address] =
                connectingScope.launch { listenMessage(connectSocket!!) }
        } catch (e: IOException) {
            Log.e(TAG, "connect fail : $e")
            return@withContext false
        }
        return@withContext true
    }


    /**
     * 📌 블루투스 메시지를 수신하는 함수
     * - BluetoothSocket을 통해 클라이언트 또는 서버로부터 메시지를 지속적으로 수신함.
     * - 수신한 메시지를 `_messageFlow`에 추가하여 UI에서 관찰할 수 있도록 함.
     */
    private suspend fun listenMessage(connectSocket: BluetoothSocket) =
        withContext(Dispatchers.IO) { // ✅ 블루투스 통신은 별도의 IO 스레드에서 실행
            var numBytes: Int
            val buffer = ByteArray(1024) // ✅ 한 번에 읽어올 최대 바이트 크기 지정 (1024 바이트)

            val inputStream = connectSocket.inputStream // ✅ 블루투스 소켓의 입력 스트림 가져오기

            while (true) { // ✅ 메시지를 지속적으로 수신하는 무한 루프
                numBytes = try {
                    inputStream.read(buffer) // ✅ 입력 스트림에서 데이터를 읽어옴 (블록킹 호출)
                } catch (e: IOException) {
                    Log.d(TAG, "Input Stream was disconnected", e) // ✅ 스트림이 끊어진 경우 로그 출력
                    break // ✅ 루프 종료 (메시지 수신 중단)
                }

                if (numBytes > 0) { // ✅ 실제 데이터가 있는 경우만 처리
                    _messageFlow.emit(String(buffer, 0, numBytes)) // ✅ 메시지를 SharedFlow에 전달
                    Log.d(TAG, "listenMessage : $buffer") // ✅ 수신된 데이터 로그 출력
                }
            }
        }

    /**
     * 📌 블루투스 메시지를 전송하는 함수
     * - `connectSocket`을 통해 연결된 장치로 메시지를 전송함.
     */
    suspend fun sendMessage(msg: ByteArray) = withContext(Dispatchers.IO) {
        val outputStream = connectSocket?.outputStream // ✅ 블루투스 소켓의 출력 스트림 가져오기
        try {
            outputStream?.write(msg) // ✅ 메시지 전송
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e) // ✅ 메시지 전송 실패 시 오류 로그 출력
        }
    }

    /**
     * 📌 블루투스 연결을 종료하는 함수
     * - 현재 연결된 블루투스 장치와의 연결을 종료하고 상태를 업데이트함.
     */
    fun finishConnect(): Boolean {
        try {
            // ✅ 현재 연결된 장치가 있으면 Job을 취소하여 메시지 수신을 중단함
            connectedDevice.value?.address?.let {
                if (it in connectingDeviceJobMap) {
                    connectingDeviceJobMap[it]?.cancel()
                }
            }

            _connectedDevice.value = null // ✅ 연결된 장치 정보 초기화
            _state.value = BluetoothState.STATE_CLOSE_CONNECT // ✅ 상태를 "연결 종료"로 변경

            connectSocket?.close() // ✅ 블루투스 소켓 닫기
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e) // ✅ 소켓 닫기 실패 시 오류 로그 출력
            return false
        }
        return true
    }

    /**
     * 📌 블루투스 검색을 중단하는 함수
     * - 블루투스 장치 검색을 중단하고 상태를 업데이트함.
     */
    fun finishDiscovering() {
        if (bluetoothAdapter.isDiscovering) { // ✅ 현재 검색 중인지 확인
            val res = bluetoothAdapter.cancelDiscovery() // ✅ 검색 중지 요청
            if (res) {
                _state.value = BluetoothState.STATE_DISCOVERING_FINISHED // ✅ 상태를 "검색 종료"로 변경
            }
        }
    }
}