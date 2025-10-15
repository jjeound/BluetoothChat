// 패키지 선언: 이 파일이 `com.gogumac.bluetoothchat.data` 패키지에 속함
package com.gogumac.bluetoothchat.data

/**
 * 📌 채팅 메시지를 나타내는 데이터 클래스.
 * - 블루투스를 통해 송수신되는 메시지를 저장하는 **데이터 모델**.
 * - 각 메시지는 텍스트 내용, 보낸 장치 정보, 보낸 사람이 누구인지 여부를 포함.
 *
 * @param text 메시지의 텍스트 내용 (예: "Hello, Bluetooth!")
 * @param device 메시지를 보낸 블루투스 기기 (null일 수 있음, 기본값 `null`)
 * @param isMine 메시지가 내가 보낸 것인지 여부 (`true`이면 내가 보낸 메시지)
 */
data class Message(
    val text: String, // 메시지의 텍스트 (예: "안녕하세요!")
    val device: Device? = null, // 메시지를 보낸 블루투스 기기 (기본값: `null`, 즉 특정 기기가 아닐 수도 있음)
    val isMine: Boolean // 이 메시지가 내 메시지인지 (`true`: 내가 보낸 메시지, `false`: 상대방이 보낸 메시지)
)
