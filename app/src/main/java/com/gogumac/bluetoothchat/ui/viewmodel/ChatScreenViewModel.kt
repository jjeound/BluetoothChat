// 📌 패키지 선언: 이 ViewModel이 `com.gogumac.bluetoothchat.ui.viewmodel` 패키지에 속함
package com.gogumac.bluetoothchat.ui.viewmodel

// ✅ Android ViewModel 관련 라이브러리
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// ✅ 코루틴 관련 라이브러리
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ✅ Bluetooth 관련 데이터 및 서비스
import com.gogumac.bluetoothchat.bluetooth.BluetoothService
import com.gogumac.bluetoothchat.data.Device
import com.gogumac.bluetoothchat.data.Device.Companion.toDevice
import com.gogumac.bluetoothchat.data.Message

/**
 * 📌 블루투스 채팅 화면을 위한 ViewModel 클래스.
 * - 채팅 메시지 목록을 관리하고 BluetoothService를 통해 데이터 송수신을 처리.
 * - Bluetooth 기기와의 연결 상태 및 입력 데이터, 수신 데이터를 관리.
 *
 * @param bluetoothService Bluetooth 통신을 담당하는 서비스 객체 (기본값 `null`)
 */
class ChatScreenViewModel(private val bluetoothService: BluetoothService? = null) : ViewModel() {

    // ✅ 채팅 메시지 리스트 (보낸 메시지 + 받은 메시지를 저장)
    private val _messageList = MutableStateFlow(listOf<Message>())
    val messageList: StateFlow<List<Message>> = _messageList.asStateFlow()

    // ✅ 현재 연결된 Bluetooth 기기 정보 (BluetoothService에서 가져옴)
    val connectedDevice: StateFlow<Device?> = bluetoothService?.connectedDevice
        ?.transform { emit(it?.toDevice()) }
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        ?: MutableStateFlow(null).asStateFlow()

    // ✅ 6개의 입력 필드를 관리하는 리스트 (사용자가 입력한 값)
    private val _textLines = MutableStateFlow(List(6) { "" })
    val textLines: StateFlow<List<String>> = _textLines.asStateFlow()

    // ✅ Bluetooth에서 수신된 데이터를 저장 (특정 형식의 데이터만)
    private val _receivedData = MutableStateFlow<String?>(null)
    val receivedData: StateFlow<String?> = _receivedData.asStateFlow()

    /**
     * 📌 초기화 블록
     * - ViewModel 생성 시 Bluetooth 메시지를 수신하는 listenMessage() 실행.
     */
    init {
        viewModelScope.launch {
            listenMessage()
        }
    }

    /**
     * 📌 입력 필드 값을 업데이트하는 함수.
     *
     * @param index 변경할 필드 인덱스 (0 ~ 5)
     * @param newValue 새 값 (문자열)
     */
    fun setTextLine(index: Int, newValue: String) {
        if (index in 0..5) {
            _textLines.value = _textLines.value.toMutableList().apply {
                this[index] = newValue
            }
        }
    }

    /**
     * 📌 입력된 데이터를 Bluetooth로 전송하는 함수.
     * - 빈 입력은 기본값 대체 후 하나의 문자열로 전송.
     */
    fun sendAllMessages() {
        val defaultValues = listOf("160", "35", "7", "50", "5", "Green")
        val filledValues = _textLines.value.mapIndexed { index, value ->
            if (value.isBlank()) defaultValues[index] else value
        }
        val combinedMessage = filledValues.joinToString(separator = " ")

        viewModelScope.launch {
            bluetoothService?.sendMessage(combinedMessage.toByteArray())
            _messageList.value = _messageList.value.toMutableList().apply {
                add(Message(text = "전송: $combinedMessage", isMine = true))
            }
        }
    }

    /**
     * 📌 실시간 속도 데이터를 Bluetooth로 전송하는 함수.
     * - 센서 모드 데이터를 그대로 전송하여, 스마트기기에 동일한 패턴의 메시지가 도착하도록 한다.
     *
     * @param data 전송할 데이터 문자열 (예: "160 35 7 50 0 sensor")
     */
    fun sendRealTimeSpeedData(data: String) {
        viewModelScope.launch {
            // 센서 모드 데이터는 그대로 전송하여 정규표현식과 일치시킴
            bluetoothService?.sendMessage(data.toByteArray())
            _messageList.value = _messageList.value.toMutableList().apply {
                add(Message(text = "전송: $data", isMine = true))
            }
        }
    }

    /**
     * 📌 Bluetooth로 수신된 데이터를 감지 및 저장하는 함수.
     */
    private suspend fun listenMessage() = withContext(Dispatchers.IO) {
        bluetoothService?.messageFlow?.collect { msg ->
            if (msg.isNotEmpty()) {
                _messageList.value = _messageList.value.toMutableList().apply {
                    add(Message(text = msg, device = connectedDevice.value, isMine = false))
                }
                // 메시지가 정해진 형식(예: "숫자 숫자 숫자 숫자 숫자 문자열")과 일치하면 수신 데이터를 저장
                if (msg.matches(Regex("\\d+ \\d+ \\d+ \\d+ \\d+ \\S+"))) {
                    _receivedData.value = msg
                }
            }
        }
    }

    /**
     * 📌 수신된 데이터를 초기화하는 함수.
     */
    fun clearReceivedData() {
        _receivedData.value = null
    }

    /**
     * 📌 수동으로 수신 데이터를 설정하는 함수.
     * - 실시간 속도 버튼을 눌렀을 때, Bluetooth 연결된 기기에서 다이얼로그 표시를 위해 사용.
     *
     * @param data 설정할 데이터 문자열 (예: "160 35 7 50 0 sensor")
     */
    fun simulateReceivedData(data: String) {
        _receivedData.value = data
    }
}
