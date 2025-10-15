// ✅ 패키지 선언: 이 파일이 `com.gogumac.bluetoothchat.ui` 패키지에 속함
package com.gogumac.bluetoothchat.ui

// ✅ 블루투스 및 시스템 관련 import
import android.Manifest // Android 권한 요청을 위한 Manifest
import android.annotation.SuppressLint // Lint 경고 억제 (예: 권한 관련 경고)
import android.bluetooth.BluetoothAdapter // 블루투스 어댑터 (Bluetooth 기능 관리)
import android.bluetooth.BluetoothManager // 블루투스 매니저 (BluetoothAdapter 접근 용도)
import android.content.Intent // 다른 화면(Activity) 전환을 위한 Intent 사용
import android.content.pm.PackageManager // 앱의 권한 상태를 확인하는 용도
import android.os.Build // Android OS 버전 체크용
import android.os.Bundle // Activity 상태 저장 및 복구

// ✅ UI 관련 import
import android.widget.Toast // 사용자에게 메시지를 표시하는 Toast 기능
import androidx.activity.ComponentActivity // Activity의 Jetpack Compose 버전
import androidx.activity.compose.setContent // Compose 기반 UI를 설정하는 함수
import androidx.activity.enableEdgeToEdge // 엣지 투 엣지 디자인을 활성화
import androidx.activity.result.ActivityResultLauncher // Activity 결과를 관리하는 런처
import androidx.activity.result.contract.ActivityResultContracts // Activity 실행 및 결과 계약 관리

// ✅ Jetpack Compose 관련 import
import androidx.compose.foundation.layout.fillMaxSize // UI 크기 조절을 위한 Modifier
import androidx.compose.foundation.layout.safeDrawingPadding // 시스템 UI 영역과 겹치지 않도록 패딩 추가
import androidx.compose.runtime.collectAsState // Flow 데이터를 Compose에서 관찰하는 함수
import androidx.compose.ui.Modifier // UI 요소의 속성을 변경하는 Modifier
import androidx.compose.ui.platform.LocalContext // 현재 Activity의 Context를 가져오는 함수
import androidx.compose.ui.res.stringResource // 문자열 리소스를 가져오는 함수
import androidx.core.content.ContextCompat

// ✅ 네비게이션 관련 import
import androidx.navigation.compose.NavHost // 네비게이션의 컨테이너 역할을 하는 Compose 함수
import androidx.navigation.compose.composable // 특정 화면을 네비게이션 목적지로 설정하는 함수
import androidx.navigation.compose.dialog // 다이얼로그를 네비게이션에 추가하는 함수
import androidx.navigation.compose.rememberNavController // 네비게이션 컨트롤러를 기억하는 함수
import androidx.navigation.toRoute // 네비게이션 경로를 추출하는 확장 함수

// ✅ 코루틴 관련 import
import kotlinx.coroutines.CoroutineScope // 코루틴 범위 정의
import kotlinx.coroutines.CoroutineStart // 코루틴을 지연 실행할 때 사용하는 설정
import kotlinx.coroutines.Dispatchers // 코루틴 실행 시 사용할 스레드 디스패처 (IO, Main 등)
import kotlinx.coroutines.async // 비동기 작업을 실행하는 함수
import kotlinx.coroutines.launch // 코루틴을 실행하는 함수
import kotlinx.coroutines.withContext // 특정 Dispatchers에서 코드 실행을 위해 사용

// ✅ Bluetooth 관련 import
import com.gogumac.bluetoothchat.R // 리소스 파일 (예: strings.xml, colors.xml 등)
import com.gogumac.bluetoothchat.bluetooth.BluetoothService // Bluetooth 기능을 관리하는 서비스 클래스
import com.gogumac.bluetoothchat.bluetooth.BluetoothState // Bluetooth 상태를 나타내는 Enum 클래스

// ✅ UI 다이얼로그 관련 import
import com.gogumac.bluetoothchat.ui.dialogs.ConnectableDeviceListDialog // 블루투스 기기 리스트 다이얼로그
import com.gogumac.bluetoothchat.ui.dialogs.ErrorDialog // 오류 발생 시 표시할 다이얼로그
import com.gogumac.bluetoothchat.ui.dialogs.LoadingDialog // 로딩 화면 다이얼로그
import com.gogumac.bluetoothchat.ui.dialogs.SelectConnectAcceptDialog // Bluetooth 연결 요청을 승인하는 다이얼로그

// ✅ UI 테마 관련 import
import com.gogumac.bluetoothchat.ui.theme.BluetoothChatTheme // 애플리케이션 테마 설정

// ✅ ViewModel 관련 import
import com.gogumac.bluetoothchat.ui.viewmodel.ChatScreenViewModel // 채팅 화면의 상태를 관리하는 ViewModel

/**
 * 📌 `MainActivity` 클래스: Bluetooth 채팅 앱의 진입점
 * - Jetpack Compose를 활용하여 UI를 구성
 * - Bluetooth 관련 설정 및 네비게이션 관리
 */
class MainActivity : ComponentActivity() {

    // ✅ 블루투스 관련 ActivityResultLauncher 선언 (권한 요청 및 활성화)
    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>> // Bluetooth 권한 요청
    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent> // Bluetooth 활성화 요청
    private lateinit var bluetoothSettingLauncher: ActivityResultLauncher<Intent> // Bluetooth 설정 화면 이동
    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Intent> // Bluetooth 장치 검색 실행

    /**
     * 📌 `onCreate` 함수: Activity가 생성될 때 호출됨.
     * - Bluetooth 기능 초기화
     * - Jetpack Compose UI 설정
     * - 네비게이션 컨트롤러 설정
     */
    @SuppressLint("MissingPermission") // 블루투스 권한 관련 경고 무시
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ BluetoothManager 가져오기 (Android 시스템에서 Bluetooth 기능을 관리)
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)

        // ✅ BluetoothAdapter 가져오기 (블루투스 어댑터, 기기에서 블루투스를 사용할 수 있는지 확인)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        // ✅ Bluetooth 활성화 요청을 위한 Launcher 설정
        bluetoothEnableLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            } else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Bluetooth 권한 요청을 위한 Launcher 설정
        bluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val deniedList = result.filter { !it.value }.map { it.key }
            if (deniedList.isNotEmpty()) {
                val map = deniedList.groupBy { permission ->
                    if (shouldShowRequestPermissionRationale(permission)) "DENIED" else "EXPLAINED"
                }
                map["DENIED"]?.let { explainBluetoothConnectPermission() }
            }
        }

        // ✅ 현재 기기가 블루투스를 지원하는지 확인
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
        }

        // ✅ Bluetooth 권한 요청 실행
        requestBluetoothConnectPermission()

        // ✅ BluetoothService 인스턴스 생성
        val service = BluetoothService(bluetoothAdapter!!, this)

        // ✅ Edge-to-Edge UI 적용 (전체 화면을 UI가 차지하도록 설정)
        enableEdgeToEdge()

        // ✅ Jetpack Compose UI 설정
        setContent {
            val navController = rememberNavController() // 네비게이션 컨트롤러 설정

            val discoveredDevice = service.discoveredDevices.collectAsState() // 검색된 블루투스 기기 목록 관찰
            val bluetoothState = service.state.collectAsState() // 블루투스 상태 관찰
            val savedBluetoothDevices = service.pairedDeviceList.collectAsState() // 페어링된 기기 목록 관찰
            val chatScreenViewModel = ChatScreenViewModel(service) // ChatScreen을 위한 ViewModel

            // ✅ 블루투스 연결이 종료되었을 경우 `ConnectScreen`으로 돌아감
            if (bluetoothState.value == BluetoothState.STATE_CLOSE_CONNECT) {
                navController.popBackStack<Connect>(inclusive = false)
                Toast.makeText(LocalContext.current, R.string.disconnected, Toast.LENGTH_SHORT).show()
            }

            // ✅ 앱 테마 적용
            BluetoothChatTheme {
                NavHost(
                    navController = navController, // 네비게이션 컨트롤러 설정
                    startDestination = Connect, // 첫 화면으로 `ConnectScreen` 설정
                    modifier = Modifier.safeDrawingPadding() // 시스템 UI와 겹치지 않도록 패딩 적용
                ) {
                    // ✅ ConnectScreen (블루투스 기기 연결 화면)
                    composable<Connect> {
                        ConnectScreen(
                            modifier = Modifier.fillMaxSize(), // 화면 전체 크기로 설정
                            deviceList = savedBluetoothDevices.value, // 페어링된 블루투스 기기 목록 전달
                            onBluetoothDeviceScanRequest = { service.startDiscovering(this@MainActivity) }, // 블루투스 검색 실행

                            // ✅ 특정 블루투스 장치에 연결을 요청할 때 실행할 함수
                            onDeviceConnectRequest = { address ->
                                // `DialogConnectLoading` 화면으로 이동 (연결 과정 표시)
                                navController.navigate(DialogConnectLoading(address))
                            },

                            // ✅ 블루투스 서버 소켓을 열 때 실행할 함수
                            onServerSocketOpenRequested = {
                                // `ServerSocketLoading` 화면으로 이동하여 로딩 UI 표시
                                navController.navigate(ServerSocketLoading)

                                // 블루투스 서버 소켓을 열기 위해 비동기 작업 실행
                                CoroutineScope(Dispatchers.Main).launch {
                                    val res = async {
                                        service.openServerSocket() // 블루투스 서버 소켓 열기 (클라이언트 연결 대기)
                                    }.await()

                                    // 로딩 화면을 종료
                                    navController.popBackStack()

                                    if (res != null) {
                                        // 연결 요청을 보낸 클라이언트 정보를 받아서 다이얼로그로 표시
                                        navController.navigate(
                                            DialogSelectConnectAccept(
                                                res.name, // 클라이언트 장치 이름
                                                res.address // 클라이언트 MAC 주소
                                            )
                                        )
                                    } else {
                                        // 서버 소켓 열기에 실패한 경우 에러 화면으로 이동
                                        navController.navigate(Error)
                                    }
                                }
                            },

                            // ✅ 블루투스를 검색 가능하도록 설정하는 요청을 처리하는 함수
                            onSetDiscoverableRequest = {
                                service.setBluetoothDiscoverable() // 기기를 검색 가능하도록 설정
                            }
                        )
                    }

                    // ✅ 채팅 화면 (ChatScreen) 설정
                    composable<Chat> {
                        ChatScreen(
                            navController = navController, // ✅ `NavController` 전달
                            modifier = Modifier.fillMaxSize(), // 화면 전체 크기로 설정
                            viewModel = chatScreenViewModel, // ChatScreen에 ViewModel 전달
                            onBackPressed = {
                                navController.popBackStack(Connect, inclusive = false) // ✅ ConnectScreen으로 이동
                            },
                            onNavigateToLinesScreen = { data -> // ✅ '다른 기능' 버튼 클릭 시 LinesScreen으로 이동
                                navController.navigate("lines_screen/$data")
                            }
                        )
                    }

                    // ✅ 라인 생성 화면 (LinesScreen) 설정
                    composable("lines_screen/{receivedData}") { backStackEntry ->
                        val receivedData = backStackEntry.arguments?.getString("receivedData") ?: "0 0 0 0 0 0"
                        LinesScreen(
                            navController = navController,
                            receivedData = receivedData,
                        )
                    }

                    // ✅ 블루투스 연결 오류 발생 시 표시할 다이얼로그
                    dialog<Error> {
                        ErrorDialog {
                            // 다이얼로그가 닫힐 때 이전 화면으로 돌아가기
                            navController.popBackStack()
                        }
                    }

                    // ✅ 블루투스 장치 검색 결과를 보여주는 다이얼로그
                    dialog<Discovery> {
                        ConnectableDeviceListDialog(
                            deviceList = discoveredDevice.value.toList(), // 검색된 블루투스 장치 목록 전달
                            bluetoothDiscoveringState = bluetoothState.value, // 현재 블루투스 검색 상태 전달

                            // 사용자가 특정 장치를 선택했을 때 실행할 로직
                            onSelectDevice = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    // 선택한 장치와 페어링을 요청하고 상태를 수집 (Flow 사용)
                                    service.requestPairing(it.address).collect { state ->
                                        withContext(Dispatchers.Main) {
                                            when (state) {
                                                BluetoothState.STATE_BONDING -> {
                                                    // 페어링 진행 중이면 다이얼로그를 `DialogPairingLoading`으로 변경
                                                    navController.navigate(DialogPairingLoading)
                                                }

                                                BluetoothState.STATE_BONDED -> {
                                                    // 페어링 완료 시 토스트 메시지 표시
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        ContextCompat.getString(
                                                            this@MainActivity,
                                                            R.string.complete_pairing
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // `ConnectScreen`으로 이동
                                                    navController.popBackStack<Connect>(inclusive = false)
                                                }

                                                BluetoothState.STATE_NONE -> {
                                                    // 페어링 실패 시 다이얼로그 닫고 `Error` 화면으로 이동
                                                    navController.popBackStack()
                                                    navController.navigate(Error)
                                                }

                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            },

                            // 사용자가 다이얼로그를 닫았을 때 실행할 로직
                            onDismiss = {
                                service.finishDiscovering() // 블루투스 검색 종료
                                navController.popBackStack() // 다이얼로그 닫기
                            }
                        )
                    }

                    // ✅ 블루투스 서버 소켓을 열 때 표시할 로딩 다이얼로그
                    dialog<ServerSocketLoading> {
                        // 토스트 메시지를 위한 문자열 가져오기
                        val toastMsg = stringResource(id = R.string.close_serever_socket_noti)

                        LoadingDialog(
                            modifier = Modifier,
                            onDismissRequest = {
                                // 다이얼로그 닫기 및 서버 소켓 종료
                                navController.popBackStack()
                                service.closeServerSocket()

                                // 서버 소켓이 닫혔음을 사용자에게 알리는 토스트 메시지 표시
                                Toast.makeText(this@MainActivity, toastMsg, Toast.LENGTH_SHORT)
                                    .show()
                            },
                            text = stringResource(id = R.string.open_server_socket) // 로딩 다이얼로그에 표시할 텍스트
                        )
                    }

                    // ✅ 블루투스 장치와의 연결을 시도하는 로딩 다이얼로그
                    dialog<DialogConnectLoading> { backStackEntry ->
                        // 다이얼로그에서 전달받은 블루투스 장치의 MAC 주소 가져오기
                        val address = backStackEntry.toRoute<DialogConnectLoading>().deviceAddress

                        // 블루투스 연결 요청을 처리하는 Coroutine Job
                        val job =
                            CoroutineScope(Dispatchers.Main).launch(start = CoroutineStart.LAZY) {
                                val res =
                                    async { service.requestConnect(address) }.await() // 비동기 요청
                                navController.popBackStack() // 다이얼로그 닫기
                                if (res) {
                                    // 연결 성공 시 채팅 화면으로 이동
                                    Toast.makeText(
                                        this@MainActivity,
                                        ContextCompat.getString(
                                            this@MainActivity,
                                            R.string.connected
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(Chat)
                                } else {
                                    // 연결 실패 시 에러 다이얼로그로 이동
                                    navController.navigate(Error)
                                }
                            }

                        // 로딩 다이얼로그 UI 표시
                        LoadingDialog(
                            modifier = Modifier,
                            onDismissRequest = {
                                job.cancel() // 사용자가 다이얼로그를 닫으면 연결 요청 취소
                                navController.popBackStack()
                            },
                            text = stringResource(id = R.string.connecting) // "연결 중..." 텍스트 표시
                        )

                        job.start() // 코루틴 실행 (지연 시작됨)
                    }

                    // ✅ 블루투스 페어링 요청 중 표시하는 로딩 다이얼로그
                    dialog<DialogPairingLoading> {
                        LoadingDialog(
                            modifier = Modifier,
                            onDismissRequest = { }, // 사용자가 닫을 수 없도록 설정
                            text = stringResource(id = R.string.request_paring_alert) // "페어링 요청 중..." 텍스트 표시
                        )
                    }

                    // ✅ 블루투스 연결 요청을 승인 또는 거부하는 다이얼로그
                    dialog<DialogSelectConnectAccept> { backStackEntry ->
                        // 연결 요청을 보낸 장치의 이름 및 MAC 주소 가져오기
                        val deviceName =
                            backStackEntry.toRoute<DialogSelectConnectAccept>().deviceName
                        val deviceAddress =
                            backStackEntry.toRoute<DialogSelectConnectAccept>().deviceAddress

                        // 연결 요청 승인 또는 거부를 결정하는 다이얼로그
                        SelectConnectAcceptDialog(
                            deviceName = deviceName, // 장치 이름 표시
                            deviceAddress = deviceAddress, // 장치 주소 표시
                            onConfirmed = {
                                // 사용자가 연결 요청을 승인한 경우
                                navController.popBackStack() // 다이얼로그 닫기
                                service.acceptConnect() // 연결 승인
                                navController.navigate(Chat) // 채팅 화면으로 이동
                            },
                            onCanceled = {
                                // 사용자가 연결 요청을 거부한 경우
                                Toast.makeText(
                                    this@MainActivity,
                                    ContextCompat.getString(
                                        this@MainActivity,
                                        R.string.reject_connect
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                                service.rejectConnect() // 연결 거부
                                navController.popBackStack() // 다이얼로그 닫기
                            }
                        )
                    }
                }
            }
        }
    }
    // ✅ 블루투스 관련 권한 목록 정의
    private val bluetoothPermissions = mutableListOf(
        Manifest.permission.BLUETOOTH, // ✅ 블루투스 기능 사용 권한 (Android 12 이하)
        Manifest.permission.BLUETOOTH_ADMIN, // ✅ 블루투스 설정 변경 권한 (Android 12 이하)
        Manifest.permission.ACCESS_FINE_LOCATION, // ✅ 정확한 위치 권한 (블루투스 검색 시 필요)
        Manifest.permission.ACCESS_COARSE_LOCATION // ✅ 대략적인 위치 권한 (블루투스 검색 시 필요)
    ).apply {
        if (Build.VERSION.SDK_INT >= 31) { // ✅ Android 12 (API 31) 이상인 경우 추가 권한 필요
            add(Manifest.permission.BLUETOOTH_CONNECT) // ✅ 블루투스 연결 권한 (Android 12 이상)
            add(Manifest.permission.BLUETOOTH_SCAN) // ✅ 블루투스 검색 권한 (Android 12 이상)
        }
    }

    /**
     * 📌 블루투스 관련 권한을 요청하는 함수.
     * - 사용자의 기기에서 블루투스 관련 권한을 요청하고, 필요한 경우 추가 설명을 표시함.
     */
    private fun requestBluetoothConnectPermission() {
        val notGrantedPermissionList = mutableListOf<String>() // ✅ 허가되지 않은 권한을 저장할 리스트

        // ✅ 블루투스 권한 목록을 하나씩 확인
        for (permission in bluetoothPermissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)

            if (result == PackageManager.PERMISSION_GRANTED) continue // ✅ 이미 허용된 권한이면 건너뜀

            notGrantedPermissionList.add(permission) // ✅ 허용되지 않은 권한 추가

            // ✅ 사용자가 권한 요청을 거부한 후 다시 요청할 때 설명이 필요한 경우
            if (shouldShowRequestPermissionRationale(permission)) {
                explainBluetoothConnectPermission() // ✅ 사용자에게 설명하는 함수 호출
            }
        }

        // ✅ 허용되지 않은 권한이 하나라도 있으면 사용자에게 권한 요청
        if (notGrantedPermissionList.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(notGrantedPermissionList.toTypedArray())
        }
    }

    /**
     * 📌 블루투스 권한이 필요한 이유를 사용자에게 설명하는 함수.
     * - `Toast` 메시지를 통해 블루투스 사용을 위해 권한이 필요함을 안내.
     */
    private fun explainBluetoothConnectPermission() {
        Toast.makeText(
            this,
            ContextCompat.getString(this, R.string.permission_required), // ✅ "권한이 필요합니다." 메시지 표시
            Toast.LENGTH_SHORT
        ).show()
    }
}
