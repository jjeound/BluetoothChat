// 📌 패키지 선언 (현재 파일이 속한 패키지 위치)
package com.gogumac.bluetoothchat.ui
// 📌 블루투스 관련 기능과 UI를 구성하는 데 필요한 Android 및 Compose 라이브러리 가져오기
import android.annotation.SuppressLint // 특정 경고(예: 블루투스 권한 경고)를 억제하는 어노테이션
import android.bluetooth.BluetoothDevice // 블루투스 기기 정보를 가져오기 위한 클래스
// 📌 Jetpack Compose의 레이아웃 관련 라이브러리
import androidx.compose.foundation.border // 테두리를 그리기 위한 기능
// Column, Row 등 레이아웃 관련 요소 포함
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn // 목록을 표시하는 Compose 컴포넌트
import androidx.compose.foundation.lazy.items // LazyColumn에서 리스트를 처리하는 기능
import androidx.compose.foundation.shape.RoundedCornerShape // UI 요소의 모서리를 둥글게 만드는 기능
// 📌 Jetpack Compose의 Material 3 UI 관련 라이브러리
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
// 📌 Compose의 아이콘 관련 라이브러리 (메일 아이콘 사용)
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.MailOutline
// 📌 Jetpack Compose의 애니메이션 관련 라이브러리
import androidx.compose.animation.core.animateFloatAsState // 애니메이션 효과 적용을 위한 함수
// 📌 Compose의 상태 관련 라이브러리
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
// 📌 UI 관련 라이브러리
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 📌 Compose UI의 툴링 관련 라이브러리 (미리보기 기능 포함)
import androidx.compose.ui.tooling.preview.Preview
// 📌 Compose의 기본 테마를 가져오기 위한 라이브러리
import com.gogumac.bluetoothchat.R
import com.gogumac.bluetoothchat.ui.theme.BluetoothChatTheme
import com.gogumac.bluetoothchat.ui.theme.ConnectBackground


/**
 * 📌 블루투스 기기 목록을 표시하고 연결을 관리하는 `ConnectScreen`
 *
 * @param modifier UI 스타일을 조정할 수 있는 Modifier
 * @param deviceList 블루투스 기기 리스트 (페어링된 기기 목록)
 * @param onBluetoothDeviceScanRequest 새 블루투스 기기를 검색하는 콜백 함수
 * @param onDeviceConnectRequest 특정 블루투스 기기와 연결 요청을 하는 콜백 함수 (MAC 주소 전달)
 * @param onSetDiscoverableRequest 블루투스 장치를 검색 가능하도록 설정하는 콜백 함수
 * @param onServerSocketOpenRequested 블루투스 서버 소켓을 여는 요청을 하는 콜백 함수
 */
@SuppressLint("MissingPermission") // 블루투스 권한 관련 경고를 억제
@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier, // 기본값을 제공하여 외부에서 수정 가능하도록 설정
    deviceList: List<BluetoothDevice>, // 연결 가능한 블루투스 기기 리스트
    onBluetoothDeviceScanRequest: () -> Unit, // 블루투스 검색 요청 함수
    onDeviceConnectRequest: (String) -> Unit, // 블루투스 기기와 연결 요청을 보낼 때 사용하는 콜백
    onSetDiscoverableRequest: () -> Unit, // 블루투스를 검색 가능하도록 만드는 함수
    onServerSocketOpenRequested: () -> Unit // 블루투스 서버 소켓을 여는 함수
) {
    // 📌 화면을 세로로 정렬하는 `Column` 레이아웃
    Column(
        modifier = modifier
            .fillMaxSize() // 화면 전체를 차지하도록 설정
            .padding(16.dp) // 16dp 패딩 추가
    ) {
        // 📌 저장된 블루투스 기기 목록을 표시하는 부분
        Column(modifier = modifier.weight(1f)) {
            Text("저장된 기기 목록") // 블루투스 기기 목록을 표시하는 타이틀

            // 📌 구분선 추가 (목록과 새 기기 추가 버튼 사이)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 📌 블루투스 기기 목록을 표시하는 `LazyColumn`
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 📌 페어링된 블루투스 기기 목록을 `SwipeDeviceItem`으로 표시
                items(deviceList) {
                    val name = it.name ?: "Unknown Device" // 기기 이름이 없을 경우 "Unknown Device"로 표시
                    val address = it.address // 블루투스 기기의 MAC 주소

                    // `SwipeDeviceItem`을 사용하여 각 블루투스 기기를 표시
                    SwipeDeviceItem(
                        name = name,
                        macAddress = address,
                        requestConnectDevice = { onDeviceConnectRequest(address) } // 클릭 시 연결 요청
                    )
                }

                // 📌 새 블루투스 기기를 추가하는 버튼 (목록 마지막에 배치)
                item {
                    TextButton(onClick = { onBluetoothDeviceScanRequest() }) {
                        Text(text = "+ 새 기기 연결하기", modifier = Modifier)
                    }
                }
            }
        }

        // 📌 블루투스 설정 관련 버튼 (검색 가능 모드 설정 및 서버 소켓 열기)
        Row(modifier = Modifier.height(64.dp)) {
            // 📌 블루투스 기기 검색 가능 모드 설정 버튼
            Button(
                onClick = onSetDiscoverableRequest,
                modifier = Modifier
                    .weight(1f) // 버튼을 동일한 크기로 맞춤
                    .fillMaxHeight()
            ) {
                Text(
                    text = stringResource(id = R.string.set_discoverable),
                    style = TextStyle(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.size(8.dp)) // 버튼 사이 간격 추가

            // 📌 블루투스 서버 소켓을 여는 버튼
            Button(
                onClick = onServerSocketOpenRequested,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = stringResource(id = R.string.open_server_socket),
                    style = TextStyle(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


/**
 * 📌 `SwipeDeviceItem`
 * - 블루투스 기기를 나타내며, 스와이프하여 연결할 수 있도록 구현.
 *
 * @param name 블루투스 기기의 이름
 * @param macAddress 블루투스 기기의 MAC 주소
 * @param requestConnectDevice 사용자가 스와이프하면 기기와 연결 요청을 보내는 콜백 함수
 */
@OptIn(ExperimentalMaterial3Api::class) // 실험적 API 사용 선언
@Composable
fun SwipeDeviceItem(
    modifier: Modifier = Modifier,
    name: String?,
    macAddress: String,
    requestConnectDevice: () -> Unit
) {
    // 📌 스와이프 상태를 관리하는 `rememberSwipeToDismissBoxState`
    val swipeState =
        rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.2f }, confirmValueChange = {
            // 사용자가 왼쪽에서 오른쪽으로 스와이프하면 `requestConnectDevice()` 실행
            if (it == SwipeToDismissBoxValue.EndToStart) {
                requestConnectDevice()
            }
            false // 스와이프를 완료하지 않고 원래 위치로 복귀
        })

    // 📌 애니메이션 설정 (스와이프 시 아이콘 크기 변화)
    val scale by animateFloatAsState(
        targetValue = if (swipeState.targetValue == SwipeToDismissBoxValue.Settled) 1f else 1.25f,
        label = ""
    )

    // 📌 스와이프 UI 구현
    SwipeToDismissBox(
        modifier = modifier,
        state = swipeState,
        enableDismissFromStartToEnd = false, // 왼쪽에서 오른쪽으로 스와이프 비활성화
        backgroundContent = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                color = ConnectBackground
            ) {
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        }
    ) {
        // 📌 블루투스 기기 정보 표시
        BluetoothDeviceItem(name = name, macAddress = macAddress)
    }
}


/**
 * 📌 `BluetoothDeviceItem`
 * - 블루투스 기기를 UI에서 표시하는 컴포넌트.
 * - 기기의 이름, MAC 주소, 테두리 스타일 등을 설정 가능.
 *
 * @param name 블루투스 기기의 이름 (null일 수도 있음)
 * @param macAddress 블루투스 기기의 MAC 주소
 * @param borderColor 테두리 색상 (기본값: 연한 회색)
 * @param borderWidth 테두리 두께 (기본값: 0.4dp)
 */
@Composable
fun BluetoothDeviceItem(
    modifier: Modifier = Modifier, // Modifier를 기본값으로 설정하여 외부에서 스타일 조정 가능
    name: String?, // 블루투스 기기의 이름 (null일 경우 처리 필요)
    macAddress: String, // 블루투스 기기의 MAC 주소
    borderColor: Color = Color.LightGray, // 기본 테두리 색상을 연한 회색으로 설정
    borderWidth: Dp = 0.4.dp // 기본 테두리 두께를 0.4dp로 설정
) {
    // 📌 블루투스 기기 정보를 감싸는 `Surface` (Material 디자인 적용)
    Surface(
        modifier = modifier
            .height(64.dp) // 높이를 64dp로 설정하여 균일한 높이 유지
            .fillMaxWidth() // 가로 너비를 전체로 설정
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(8.dp)), // 테두리 적용
        shape = RoundedCornerShape(8.dp), // 모서리를 둥글게 설정
        color = Color.White // 배경 색상 (흰색)
    ) {
        // 📌 블루투스 기기 정보를 세로로 정렬하는 `Column`
        Column(
            modifier = Modifier.padding(12.dp), // 내부 패딩 추가 (12dp)
            verticalArrangement = Arrangement.SpaceBetween // 요소 간의 간격 자동 조정
        ) {
            // 📌 블루투스 기기 이름을 표시하는 `Text`
            Text(
                text = name ?: "no name", // 기기 이름이 없으면 "no name"으로 대체
                modifier = Modifier, // 추가적인 Modifier 설정 없음
                overflow = TextOverflow.Ellipsis, // 너무 긴 이름은 "..."으로 표시
                maxLines = 1 // 한 줄까지만 표시
            )

            // 📌 블루투스 MAC 주소를 표시하는 `Text`
            Text(
                text = "mac : $macAddress", // "mac : {MAC 주소}" 형식으로 출력
                style = TextStyle(color = Color.Gray, fontSize = 12.sp), // 글자 크기 및 색상 지정
                modifier = Modifier // 추가적인 Modifier 설정 없음
            )
        }
    }
}

/**
 * 📌 `ConnectScreenPreview`
 * - Jetpack Compose 미리보기 기능을 활용하여 `ConnectScreen`의 UI를 테스트할 수 있음.
 */
@Preview(showBackground = true)
@Composable
fun ConnectScreenPreview() {
    BluetoothChatTheme { // 테마 적용
        ConnectScreen(
            deviceList = listOf(), // 블루투스 기기 목록을 비워둠 (샘플 UI)
            onBluetoothDeviceScanRequest = {}, // 클릭 시 아무 동작도 하지 않도록 설정
            onDeviceConnectRequest = {}, // 클릭 시 아무 동작도 하지 않도록 설정
            onServerSocketOpenRequested = {}, // 클릭 시 아무 동작도 하지 않도록 설정
            onSetDiscoverableRequest = {} // 클릭 시 아무 동작도 하지 않도록 설정
        )
    }
}

/**
 * 📌 `DeviceItemPreview`
 * - `BluetoothDeviceItem` UI 컴포넌트를 미리보기에서 테스트하는 기능.
 */
@Preview(showBackground = true)
@Composable
fun DeviceItemPreview() {
    BluetoothChatTheme { // 테마 적용
        BluetoothDeviceItem(
            name = "TEST1".repeat(10), // 아주 긴 블루투스 기기 이름 (Ellipsis 처리 테스트)
            macAddress = "12:34:56:78:90" // 예제 MAC 주소
        )
    }
}
