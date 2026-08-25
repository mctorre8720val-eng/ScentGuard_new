package com.example.scentguard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scentguard.data.model.ChartData
import kotlin.math.roundToInt

@Composable
fun ScentGuardChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    var selectedIndex by remember { mutableStateOf(-1) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Gas Trend",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (data.points.isNotEmpty() && selectedIndex != -1) {
                    Text(
                        text = "${data.points[selectedIndex].y.roundToInt()} ppm at ${data.points[selectedIndex].label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = lineColor,
                        fontWeight = FontWeight.Bold
                    )
                } else if (data.points.size > 1) {
                    Text(
                        "Scrub chart for details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(
                text = if (data.points.isEmpty()) "No data" else "Stable",
                style = MaterialTheme.typography.labelSmall,
                color = lineColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            if (data.points.size < 2) {
                Text(
                    text = if (data.points.isEmpty()) "Waiting for sensor data..." else "Collecting more points...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(data.points) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val width = size.width
                                    val spacing = width / (data.points.size - 1)
                                    val index = (offset.x / spacing).roundToInt().coerceIn(0, data.points.size - 1)
                                    selectedIndex = index
                                    tryAwaitRelease()
                                    selectedIndex = -1
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (data.points.size - 1)
                    
                    val maxVal = if (data.maxVal == data.minVal) data.maxVal + 1f else data.maxVal
                    val minVal = data.minVal
                    
                    val path = Path()
                    val fillPath = Path()
                    
                    data.points.forEachIndexed { i, point ->
                        val x = i * spacing
                        val y = height - ((point.y - minVal) / (maxVal - minVal)) * height
                        
                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * spacing
                            val prevY = height - ((data.points[i - 1].y - minVal) / (maxVal - minVal)) * height
                            path.quadraticTo(prevX, prevY, (x + prevX) / 2, (y + prevY) / 2)
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                        
                        if (i == data.points.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // Draw Area Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw Line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    
                    // Scrubbing Line
                    if (selectedIndex != -1) {
                        val scrubX = selectedIndex * spacing
                        drawLine(
                            color = lineColor.copy(alpha = 0.5f),
                            start = Offset(scrubX, 0f),
                            end = Offset(scrubX, height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = Offset(scrubX, height - ((data.points[selectedIndex].y - minVal) / (maxVal - minVal)) * height)
                        )
                    }

                    // Draw Horizontal grid lines
                    val gridLines = 3
                    for (j in 0..gridLines) {
                        val yLine = (height / gridLines) * j
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.1f),
                            start = Offset(0f, yLine),
                            end = Offset(width, yLine),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }
        }
        
        if (data.points.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.points.filterIndexed { index, _ -> index % 4 == 0 || index == data.points.size - 1 }.forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
