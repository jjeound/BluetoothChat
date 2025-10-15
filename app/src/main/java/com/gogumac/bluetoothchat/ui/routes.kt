package com.gogumac.bluetoothchat.ui

import android.bluetooth.BluetoothDevice
import kotlinx.serialization.Serializable

@Serializable
object Lines
/**
 * 블루투스 연결을 시도할 때 사용되는 상태 객체.
 * - `object`로 선언하여 단일 인스턴스로 관리됨.
 */
@Serializable
object Connect

/**
 * 블루투스 채팅 중임을 나타내는 상태 객체.
 * - 블루투스 장치 간 데이터 통신이 진행 중일 때 사용됨.
 */
@Serializable
object Chat

/**
 * 블루투스 연결 또는 작업 중 오류가 발생했음을 나타내는 상태 객체.
 * - 연결 실패, 데이터 전송 오류 등 다양한 에러 상황을 표현할 수 있음.
 */
@Serializable
object Error

/**
 * 블루투스 장치 검색(Discovery) 중임을 나타내는 상태 객체.
 * - 블루투스 기기를 검색하는 동안 UI에서 이 상태를 사용할 수 있음.
 */
@Serializable
object Discovery

/**
 * 블루투스 서버 소켓이 열리는 동안 UI에서 로딩 상태를 나타내는 객체.
 * - 서버 소켓이 `accept()`를 호출하기 전까지의 대기 상태를 표현함.
 */
@Serializable
object ServerSocketLoading

/**
 * 블루투스 장치와 연결을 시도할 때 표시되는 다이얼로그 상태.
 * - 특정 블루투스 장치(`deviceAddress`)와 연결을 시도하는 중임을 나타냄.
 *
 * @param deviceAddress 연결을 시도하는 블루투스 장치의 MAC 주소.
 */
@Serializable
data class DialogConnectLoading(val deviceAddress: String)

/**
 * 블루투스 페어링 진행 중일 때 표시되는 다이얼로그 상태.
 * - 사용자가 블루투스 장치와 페어링하는 과정에서 로딩 UI를 제공할 수 있음.
 */
@Serializable
object DialogPairingLoading

/**
 * 블루투스 연결을 해제할 때 사용자에게 확인을 요청하는 다이얼로그 상태.
 * - 연결을 해제할 때 나타나는 UI 요소로 활용됨.
 */
@Serializable
object DisconnectAlertDialog

/**
 * 블루투스 장치에서 연결 요청을 수락할지 선택하는 다이얼로그 상태.
 * - 블루투스 연결 요청을 수락 또는 거부할 수 있도록 UI에서 표시됨.
 *
 * @param deviceName 연결 요청을 보낸 블루투스 장치의 이름.
 * @param deviceAddress 연결 요청을 보낸 블루투스 장치의 MAC 주소.
 */
@Serializable
data class DialogSelectConnectAccept(val deviceName: String, val deviceAddress: String)
