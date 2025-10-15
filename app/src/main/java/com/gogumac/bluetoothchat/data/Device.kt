// 패키지 선언: 이 파일이 `com.gogumac.bluetoothchat.data` 패키지에 속함
package com.gogumac.bluetoothchat.data

// 필요한 Android 및 Jetpack Compose 라이브러리 가져오기
import android.annotation.SuppressLint // 경고 억제를 위한 어노테이션
import android.bluetooth.BluetoothDevice // Android의 블루투스 기기 클래스
import androidx.compose.ui.graphics.Color // UI에서 사용할 색상
import androidx.compose.ui.graphics.vector.ImageVector // UI에서 사용할 아이콘 벡터 그래픽

/**
 * 📌 Bluetooth 기기를 나타내는 데이터 클래스.
 * - 블루투스 장치를 **UI에서 쉽게 관리**할 수 있도록 데이터를 저장.
 * - 기기의 이름, MAC 주소, UI에서 사용할 색상 및 아이콘 정보를 포함.
 *
 * @param name 블루투스 기기의 이름 (예: "My Bluetooth Device")
 * @param mac 블루투스 기기의 MAC 주소 (고유 식별자, 예: "00:11:22:AA:BB:CC")
 * @param color 기기를 UI에서 표시할 때 사용할 색상
 * @param image 기기 아이콘 (선택적, 기본값은 null)
 */
data class Device(
    val name: String,       // 블루투스 기기의 이름 (예: "My Headset")
    val mac: String,        // 블루투스 기기의 MAC 주소 (예: "00:11:22:AA:BB:CC")
    val color: Color,       // UI에서 기기를 표시할 때 사용할 색상
    val image: ImageVector? = null // 기기 아이콘 (기본값은 null)
) {
    /**
     * 📌 Companion Object: 정적 메서드 및 유틸리티 함수를 포함
     * - 블루투스 장치(`BluetoothDevice`)를 `Device` 객체로 변환하는 확장 함수를 제공.
     */
    companion object {
        /**
         * 📌 블루투스 기기(BluetoothDevice)를 Device 데이터 클래스로 변환하는 확장 함수.
         * - `BluetoothDevice` 객체를 `Device` 객체로 변환하여 UI에서 쉽게 사용할 수 있도록 함.
         *
         * @receiver BluetoothDevice 블루투스 기기 객체
         * @return Device 변환된 Device 객체
         */
        @SuppressLint("MissingPermission") // 블루투스 기기 정보 접근 시 권한이 필요하지만, 예외 처리 생략 (주의 필요)
        fun BluetoothDevice.toDevice(): Device =
            Device(
                name = name,   // BluetoothDevice의 name을 가져와서 사용
                mac = address, // BluetoothDevice의 MAC 주소 (address)를 사용
                color = Color.Cyan, // 기본적으로 Cyan 색상 적용 (UI에서 기본 값으로 설정)
                image = null   // 아이콘은 기본적으로 null (필요시 추가 가능)
            )
    }
}
