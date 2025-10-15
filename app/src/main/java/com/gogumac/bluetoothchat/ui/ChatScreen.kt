// 📌 패키지 선언: 이 파일은 com.gogumac.bluetoothchat.ui 패키지에 속함
package com.gogumac.bluetoothchat.ui

// ----------------------
// Android 및 Compose 관련 라이브러리 임포트
// ----------------------
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.navigation.NavController

// ----------------------
// Compose 상태, 텍스트 스타일, 단위 등에 관한 임포트
// ----------------------
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ----------------------
// 프로젝트 내 리소스, 테마 및 ViewModel/데이터 클래스 임포트
// ----------------------
import com.gogumac.bluetoothchat.R
import com.gogumac.bluetoothchat.ui.viewmodel.ChatScreenViewModel
import com.gogumac.bluetoothchat.data.Message

/**
 * 📌 ChatScreen 함수
 * - Bluetooth 채팅 화면을 구성하는 최상위 Composable 함수.
 * - 사용자가 Bluetooth 기기와의 연결 상태를 확인하고 메시지를 주고받을 수 있다.
 * - 수신된 데이터가 있을 경우, "라인을 생성하시겠습니까?" 다이얼로그를 표시하여 LinesScreen 전환을 유도한다.
 *
 * @param navController NavController를 통해 화면 전환을 관리한다.
 * @param onBackPressed 뒤로 가기 버튼 클릭 시 호출되는 함수.
 * @param onNavigateToLinesScreen LinesScreen으로 전환할 때 데이터를 전달하는 함수.
 * @param modifier UI 수정에 사용할 Modifier, 기본값은 Modifier.
 * @param viewModel ChatScreen의 상태를 관리하는 ChatScreenViewModel 객체.
 */
@Composable
fun ChatScreen(
    navController: NavController,
    onBackPressed: () -> Unit,
    onNavigateToLinesScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatScreenViewModel = ChatScreenViewModel()
) {
    // ViewModel 상태 수신
    val textLines by viewModel.textLines.collectAsState()
    val messageList by viewModel.messageList.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()
    val receivedData by viewModel.receivedData.collectAsState()

    // LazyColumn의 스크롤 상태 저장
    val listState = rememberLazyListState()

    // 뒤로 가기 버튼 처리
    BackHandler {
        onBackPressed()
    }

    // 메시지 목록 업데이트 시 자동 스크롤
    LaunchedEffect(messageList) {
        if (messageList.isNotEmpty()) {
            listState.animateScrollToItem(index = messageList.size - 1)
        }
    }

    // Bluetooth 데이터 수신 시, "라인을 생성하시겠습니까?" 다이얼로그 표시
    receivedData?.let { data ->
        ConfirmCreateLineDialog(
            data = data,
            onConfirm = {
                viewModel.clearReceivedData()
                onNavigateToLinesScreen(data)
            },
            onDismiss = {
                viewModel.clearReceivedData()
            }
        )
    }

    // 전체 화면 Surface
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 영역: Bluetooth 기기 정보 표시
            connectedDevice?.let {
                DeviceInfo(
                    deviceName = it.name,
                    deviceImage = it.image,
                    backgroundColor = it.color
                )
            }
            // 중앙 영역: 메시지 목록 (LazyColumn)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .padding(horizontal = 12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messageList) { message ->
                        Talk(
                            content = message.text,
                            isMine = message.isMine,
                            deviceName = message.device?.name.orEmpty(),
                            deviceColor = message.device?.color ?: Color.Green,
                            deviceImage = message.device?.image
                        )
                    }
                }
            }
            // 하단 영역 (입력 영역): 사용자가 데이터를 입력하는 ChatInputMultiLine
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(5.5f)
                    .padding(horizontal = 5.dp, vertical = 20.dp)
            ) {
                ChatInputMultiLine(
                    textLines = textLines,
                    onValueChanged = { index, value -> viewModel.setTextLine(index, value) }
                )
            }
            // 하단 영역 (버튼 영역): 3개의 버튼 배치 ("전송", "미리 보기", "실시간 속도")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // "전송 버튼": 기존 sendAllMessages() 호출 (이메일 아이콘)
                    IconButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(25.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        onClick = { viewModel.sendAllMessages() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            tint = Color.White,
                            contentDescription = "Send All"
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // "미리 보기" 버튼: 입력 데이터를 LinesScreen으로 전달
                    IconButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(25.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        onClick = {
                            val latestTextLines = viewModel.textLines.value
                            val updatedPreviewData = textLines.mapIndexed { index, value ->
                                if (value.isBlank()) listOf("160", "35", "7", "50", "5", "Green")[index]
                                else value
                            }.joinToString(" ")
                            onNavigateToLinesScreen(updatedPreviewData)
                        }
                    ) {
                        Text(
                            text = "미리 보기",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // "실시간 속도" 버튼:
                    // 이 버튼을 누르면 Bluetooth를 통해 실시간 속도 데이터(예: "160 35 7 50 0 sensor")가 전송되고,
                    // 연결된 기기에서는 이메일 아이콘 버튼과 동일하게 "라인을 생성하시겠습니까?" 다이얼로그가 뜨며, 동일한 화면(LinesScreen)이 표시된다.
                    IconButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(25.dp)
                            .background(
                                MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        onClick = {
                            // 센서 모드 데이터 생성 (예: "160 35 7 50 0 sensor")
                            val sensorDataList = listOf("160", "35", "7", "50", "0", "sensor")
                            val sensorModeData = sensorDataList.joinToString(" ")

                            // viewModel의 각 텍스트 라인 업데이트 (6개 입력 필드 모두 업데이트)
                            sensorDataList.forEachIndexed { index, value ->
                                viewModel.setTextLine(index, value)
                            }

                            // Bluetooth를 통해 실시간 속도 데이터 전송
                            viewModel.sendRealTimeSpeedData(sensorModeData)
                        }
                    ) {
                        Text(
                            text = "실시간 속도",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}




// ----------------------
// ConfirmCreateLineDialog: Bluetooth 데이터를 수신했을 때 사용자 확인 다이얼로그
// ----------------------
/**
 * 📌 ConfirmCreateLineDialog
 * - Bluetooth 데이터를 수신하면 사용자에게 해당 데이터를 확인할 수 있도록 다이얼로그를 표시한다.
 * - 사용자가 "Yes"를 클릭하면 onConfirm이, "No"를 클릭하면 onDismiss가 호출된다.
 *
 * @param data 수신된 Bluetooth 데이터 (예: "180 50 25 10 100 Green")
 * @param onConfirm 사용자가 "Yes" 버튼을 클릭할 때 호출되는 함수
 * @param onDismiss 사용자가 "No" 버튼을 클릭하거나 외부를 터치할 때 호출되는 함수
 */
@Composable
fun ConfirmCreateLineDialog(
    data: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("라인을 생성하시겠습니까?") },
        text = { Text("데이터: $data") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

// ----------------------
// ChatInputMultiLine: 여러 입력 필드를 통한 데이터 입력 UI
// ----------------------
/**
 * 📌 ChatInputMultiLine
 * - 사용자가 환자 키, 선 간격, 선 높이, 선 길이, 및 속도를 입력할 수 있는 UI를 제공한다.
 * - 각 필드의 값은 onValueChanged 콜백을 통해 상위 ViewModel로 전달된다.
 *
 * @param textLines 사용자가 입력한 6개 필드의 값 리스트
 * @param onValueChanged 특정 필드 값 변경 시 호출되는 콜백 (인덱스와 새 값)
 * @param modifier UI 수정용 Modifier
 * @param minHeight 입력 필드 최소 높이 (기본 50.dp)
 * @param maxHeight 입력 필드 최대 높이 (기본 50.dp)
 */
@Composable
fun ChatInputMultiLine(
    textLines: List<String>,
    onValueChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 50.dp,
    maxHeight: Dp = 50.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val labels = listOf("환자 키 (cm)", "선의 간격", "선의 높이", "선의 길이", "속도")
        labels.forEachIndexed { i, label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$label:",
                    modifier = Modifier.width(80.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = minHeight, max = maxHeight)
                        .background(Color.White, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    TextField(
                        value = textLines.getOrElse(i) { "" },
                        onValueChange = { newText -> onValueChanged(i, newText) },
                        textStyle = TextStyle(fontSize = 16.sp, textAlign = TextAlign.Start),
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }
        }
        // 6번째 필드: 색상 선택을 위한 드롭다운 메뉴
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "선의 색상:",
                modifier = Modifier.width(100.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            val selectedColor = remember { mutableStateOf(textLines.getOrElse(5) { "" }) }
            ColorDropdownMenu(
                selectedColor = selectedColor.value,
                onColorSelected = { color ->
                    selectedColor.value = color
                    onValueChanged(5, color)
                }
            )
        }
    }
}

// ----------------------
// ColorDropdownMenu: 색상 선택 드롭다운 메뉴
// ----------------------
/**
 * 📌 ColorDropdownMenu
 * - 사용자가 선의 색상을 선택할 수 있도록 드롭다운 메뉴를 제공한다.
 * - 선택된 색상은 onColorSelected 콜백을 통해 전달된다.
 *
 * @param selectedColor 현재 선택된 색상 (예: "Green")
 * @param onColorSelected 사용자가 색상을 선택할 때 호출되는 콜백 (선택한 색상 전달)
 */
@Composable
fun ColorDropdownMenu(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf("Green", "Red", "White")
    val expanded = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.White, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(6.dp))
                .clickable { expanded.value = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            colors.forEach { color ->
                DropdownMenuItem(
                    text = { Text(color) },
                    onClick = {
                        onColorSelected(color)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

// ----------------------
// DeviceInfo: 연결된 Bluetooth 기기 정보 표시 UI
// ----------------------
/**
 * 📌 DeviceInfo
 * - 연결된 Bluetooth 기기의 이름과 아이콘(또는 첫 글자)을 상단에 표시한다.
 *
 * @param deviceName Bluetooth 기기의 이름
 * @param deviceImage Bluetooth 기기의 아이콘 (없을 경우 null)
 * @param backgroundColor 기기 관련 배경 색상
 * @param modifier 추가 Modifier
 */
@Composable
fun DeviceInfo(
    deviceName: String,
    deviceImage: ImageVector?,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val appendText = stringResource(id = R.string.connecting_with)
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(deviceName)
        }
        append(appendText)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(color = MaterialTheme.colorScheme.primary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(36.dp)
                .background(color = backgroundColor, shape = CircleShape),
            border = BorderStroke(1.dp, color = Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (deviceImage != null) {
                    Image(
                        imageVector = deviceImage,
                        contentDescription = "Bluetooth Device Icon",
                        modifier = Modifier
                    )
                } else {
                    Text(
                        text = deviceName[0].toString().uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Text(
            text = annotatedString,
            modifier = Modifier.padding(10.dp),
            color = Color.White
        )
    }
}

// ----------------------
// MessageBubble: 채팅 메시지를 말풍선 형태로 표시하는 UI
// ----------------------
/**
 * 📌 MessageBubble
 * - 개별 채팅 메시지를 말풍선 형식으로 표시하며,
 *   메시지를 보낸 사람에 따라 오른쪽(내 메시지) 또는 왼쪽(상대 메시지)에 정렬한다.
 *
 * @param message Message 데이터 객체
 */
@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isMine) MaterialTheme.colorScheme.primary else Color.Gray
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = Color.White
            )
        }
    }
}

// ----------------------
// Talk: 채팅 메시지와 기기 정보를 함께 표시하는 UI (메시지 말풍선 + 기기 배지)
// ----------------------
/**
 * 📌 Talk
 * - 채팅 메시지를 말풍선 형태로 표시하며,
 *   내가 보낸 메시지는 오른쪽, 상대방이 보낸 메시지는 왼쪽에 기기 배지와 함께 표시한다.
 *
 * @param content 채팅 메시지 텍스트
 * @param deviceName 메시지를 보낸 기기의 이름
 * @param deviceColor 기기 관련 색상
 * @param isMine 메시지를 보낸 주체가 자신인지 여부
 * @param modifier 추가 Modifier
 * @param defaultSize 기기 배지 및 말풍선의 기본 크기 (기본 48.dp)
 * @param deviceImage 기기 아이콘 (없으면 null)
 */
@Composable
fun Talk(
    content: String,
    deviceName: String,
    deviceColor: Color,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    defaultSize: Dp = 48.dp,
    deviceImage: ImageVector? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        if (isMine) {
            TalkUnit(content, defaultSize = defaultSize)
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (!isMine) {
            Column(
                modifier = Modifier.width(defaultSize),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DeviceBadge(
                    deviceName = deviceName,
                    deviceColor = deviceColor,
                    deviceImage = deviceImage,
                    size = defaultSize
                )
                Text(
                    text = deviceName,
                    modifier = Modifier.padding(top = 4.dp),
                    style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            TalkUnit(content, defaultSize = defaultSize)
        }
    }
}

// ----------------------
// DeviceBadge: 기기 아이콘 또는 첫 글자를 뱃지 형태로 표시하는 UI
// ----------------------
/**
 * 📌 DeviceBadge
 * - 채팅 메시지를 보낸 기기를 식별하기 위해 아이콘 또는 기기 이름의 첫 글자를 뱃지 형태로 표시한다.
 *
 * @param deviceName 기기 이름
 * @param deviceColor 기기 관련 배경 색상
 * @param modifier 추가 Modifier
 * @param size 뱃지의 크기 (기본 48.dp)
 * @param deviceImage 기기 아이콘 (없으면 null)
 */
@Composable
fun DeviceBadge(
    deviceName: String,
    deviceColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    deviceImage: ImageVector? = null
) {
    Card(
        modifier = modifier.size(size),
        border = BorderStroke(1.dp, color = Color.LightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = deviceColor, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (deviceImage != null) {
                Image(
                    imageVector = deviceImage,
                    contentDescription = "Device Icon"
                )
            } else {
                Text(
                    text = deviceName[0].toString().uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ----------------------
// TalkUnit: 단일 메시지 말풍선 UI
// ----------------------
/**
 * 📌 TalkUnit
 * - 단일 채팅 메시지를 말풍선 형태의 카드 UI로 감싸 내부에 메시지 텍스트를 표시한다.
 *
 * @param text 채팅 메시지 텍스트
 * @param modifier 추가 Modifier
 * @param defaultSize 말풍선의 기본 크기 (기본 48.dp)
 */
@Composable
fun TalkUnit(
    text: String,
    modifier: Modifier = Modifier,
    defaultSize: Dp = 48.dp
) {
    Card(
        modifier = modifier.padding(top = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(10.dp)
                    .widthIn(max = 250.dp)
            )
        }
    }
}

// ----------------------
// ChatInput: 사용자 입력 필드와 전송 버튼을 포함하는 채팅 입력 UI
// ----------------------
/**
 * 📌 ChatInput
 * - 사용자가 채팅 메시지를 입력하고, 전송 버튼을 통해 메시지를 발신하는 UI를 제공한다.
 *
 * @param text 현재 입력된 텍스트
 * @param onValueChanged 사용자가 텍스트 입력 시 호출되는 콜백
 * @param modifier 추가 Modifier
 * @param minHeight 입력 필드 최소 높이 (기본 48.dp)
 * @param maxHeight 입력 필드 최대 높이 (기본 150.dp)
 * @param buttonWidth 전송 버튼 너비 (기본 48.dp)
 * @param onSendButtonClicked 전송 버튼 클릭 시 호출되는 콜백 함수
 */
@Composable
fun ChatInput(
    text: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    maxHeight: Dp = 150.dp,
    buttonWidth: Dp = 48.dp,
    onSendButtonClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
    ) {
        HorizontalDivider(modifier = Modifier.height(1.dp))
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = minHeight, max = maxHeight),
                value = text,
                onValueChange = onValueChanged,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            // 전송 버튼: 기존 기능 유지
            IconButton(
                modifier = Modifier
                    .width(buttonWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)),
                onClick = onSendButtonClicked
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    tint = Color.White,
                    contentDescription = "Send Message"
                )
            }
        }
    }
}
