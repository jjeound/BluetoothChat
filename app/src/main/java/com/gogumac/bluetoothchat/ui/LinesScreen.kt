// 📌 패키지 선언: LinesScreen.kt 파일은 com.gogumac.bluetoothchat.ui 패키지에 속함
package com.gogumac.bluetoothchat.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/**
 * 📌 LinesScreen
 * - 선형 가속도 센서를 사용해서 가속도 데이터를 기반으로 기기의 이동 속도를 추정하고,
 *   그에 따라 선의 이동 속도를 조절한다.
 *
 * @param navController 네비게이션을 담당하는 객체
 * @param receivedData 블루투스를 통해 받은 데이터 문자열 (사용자 입력: 선의 너비, 색상 등)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinesScreen(navController: NavController?, receivedData: String?) {
    // 1. Context 및 SensorManager 획득
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }

    // 2. 선 이동에 사용할 Y축 오프셋 및 현재 속도를 저장할 상태 변수
    var offsetY by remember { mutableStateOf(0f) }
    var currentSpeed by remember { mutableStateOf(0f) }

    // 3. 전달받은 데이터 문자열 분리 (기본값 제공)
    val parts = receivedData?.split(" ") ?: listOf("160", "35", "7", "50", "5", "Green")
    if (parts.size < 5) return

    // 4. 센서 모드 판별 (parts의 6번째 값이 "sensor"이면 센서 모드)
    val isSensorMode = parts.getOrNull(5)?.lowercase() == "sensor"

    // 5. 수동 입력 속도 (센서 모드가 아닐 때 사용)
    val speed = if (isSensorMode) 0f else (parts[4].toFloatOrNull() ?: 1f)

    // 6. 사용자 관련 데이터: 사용자 키, 선 간격, 기본 선 높이, 선 길이
    val userHeight = parts[0].toFloatOrNull() ?: 160f
    val spacingDp = parts[1].toFloatOrNull()?.dp ?: 16.dp
    val baseLineHeightDp = parts[2].toFloatOrNull()?.dp ?: 4.dp
    val baseLineLengthDp = parts[3].toFloatOrNull()?.dp ?: 100.dp

    // 7. 선 색상 결정
    val colorInput = parts[5]
    val lineColor = when (colorInput.lowercase()) {
        "red" -> Color.Red
        "white" -> Color.White
        else -> Color.Green
    }

    // 8. 화면 크기 및 캔버스 관련 계산
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val canvasWidthPx = with(density) { screenWidthDp.toPx() }
    val canvasHeightPx = with(density) { screenHeightDp.toPx() }
    val maxLineLengthPx = with(density) { screenWidthDp.toPx() * 0.9f }
    val baseLineHeightPx = with(density) { baseLineHeightDp.toPx() }
    val spacingPx = with(density) { spacingDp.toPx() * 6f }

    // 9. 선의 높이와 간격은 고정값 사용 (회전 센서 제거)
    val minHeightPx = baseLineHeightPx * 10f
    val maxHeightPx = baseLineHeightPx * 12.5f
    val adjustedLineHeightPx = (minHeightPx + maxHeightPx) / 2f
    val adjustedSpacingPx = spacingPx
    val totalSpacing = adjustedLineHeightPx + adjustedSpacingPx

    // 10. 센서 이벤트 리스너 (선형 가속도 센서를 사용)
    val sensorListener = remember {
        object : SensorEventListener {
            private var lastUpdateTime = System.currentTimeMillis()
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastUpdateTime) / 1000f
                lastUpdateTime = currentTime

                when (event.sensor.type) {
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        // 전체 가속도 벡터의 크기를 계산
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]
                        val accMagnitude = sqrt(ax * ax + ay * ay + az * az)

                        // multiplier 값은 기기의 속도에 따라 차이를 확대하는 데 사용 (실험적으로 조정)
                        val multiplier = 5f

                        // 속도 추정: 단순 적분 방식 (노이즈 및 드리프트가 있을 수 있음)
                        currentSpeed += accMagnitude * deltaTime * multiplier

                        // 감쇠 효과: 가속도가 없으면 속도가 서서히 감소하도록 (0.9는 감쇠 계수, 필요시 조정)
                        currentSpeed *= 0.9f

                        // 클램핑: 너무 큰 값은 제한
                        currentSpeed = currentSpeed.coerceIn(-10f, 10f)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // 11. 센서 등록 (선형 가속도 센서만 등록, SENSOR_DELAY_FASTEST)
    DisposableEffect(Unit) {
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        accelSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    // 12. 선 이동 애니메이션 처리: 센서 모드에서는 currentSpeed를 사용하여 offsetY 업데이트
    LaunchedEffect(isSensorMode, currentSpeed, speed) {
        // speedMultiplier로 최종 반응속도를 추가 조정 (필요시 테스트 후 조정)
        val speedMultiplier = 10f
        while (true) {
            val speedFactor = if (isSensorMode) currentSpeed * speedMultiplier else speed
            offsetY = (offsetY + speedFactor) % totalSpacing
            delay(16L) // 약 60fps
        }
    }

    // 13. UI 구성 (Scaffold와 Canvas)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* 필요시 앱바 타이틀 추가 */ },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                // 디버깅용: currentSpeed 값을 화면 상단에 표시
                Text(
                    text = "Speed: ${"%.2f".format(currentSpeed)}",
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val numLines = (canvasHeightPx / totalSpacing).toInt() + 2000
                    val startYOffset = (canvasHeightPx / 2f) + offsetY
                    for (i in numLines - 1 downTo 0) {
                        val currentWidth = maxLineLengthPx
                        val rectLeft = (canvasWidthPx - currentWidth) / 2f
                        val rectTop = startYOffset - (numLines - 1 - i) * totalSpacing
                        val trapezoidTopWidth = currentWidth * 0.95f
                        val trapezoidBottomWidth = currentWidth
                        val topLeftX = (canvasWidthPx - trapezoidTopWidth) / 2f
                        val topRightX = topLeftX + trapezoidTopWidth
                        val bottomLeftX = (canvasWidthPx - trapezoidBottomWidth) / 2f
                        val bottomRightX = bottomLeftX + trapezoidBottomWidth

                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(bottomLeftX, rectTop + adjustedLineHeightPx)
                            lineTo(bottomRightX, rectTop + adjustedLineHeightPx)
                            lineTo(topRightX, rectTop)
                            lineTo(topLeftX, rectTop)
                            close()
                        }
                        drawPath(path = path, color = lineColor)
                    }
                }
            }
        }
    }
}
