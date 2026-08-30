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

    // Constants for visualization
    val maxPpm = 2000f
    val safeLimit = 1000f
    val dangerLimit = 1500f
    
    // Minimal horizontal margin to prevent edge clipping
    val horizontalMargin = 12.dp

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Odor Concentration Trend",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (data.points.isNotEmpty() && selectedIndex != -1) {
                    val point = data.points[selectedIndex]
                    val status = when {
                        point.y < safeLimit -> "SAFE"
                        point.y < dangerLimit -> "WARN"
                        else -> "DANGER"
                    }
                    Text(
                        text = "${point.y.roundToInt()} ppm ($status) at ${point.label}",
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
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp),
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
                                    val marginPx = horizontalMargin.toPx()
                                    val effectiveWidth = width - 2 * marginPx
                                    val spacing = effectiveWidth / (data.points.size - 1)
                                    val index = ((offset.x - marginPx) / spacing).roundToInt().coerceIn(0, data.points.size - 1)
                                    selectedIndex = index
                                    tryAwaitRelease()
                                    selectedIndex = -1
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val marginPx = horizontalMargin.toPx()
                    val effectiveWidth = width - 2 * marginPx
                    val spacing = effectiveWidth / (data.points.size - 1)
                    
                    val minVal = 0f
                    val scaleRange = maxPpm - minVal
                    
                    // Helper to get Y coordinate for a PPM value
                    fun getY(ppm: Float) = height - ((ppm - minVal) / scaleRange) * height

                    // 1. Draw Threshold Zones (Background)
                    // SAFE Zone (Emerald tint)
                    drawRect(
                        color = Color(0xFF34C759).copy(alpha = 0.06f),
                        topLeft = Offset(0f, getY(safeLimit)),
                        size = androidx.compose.ui.geometry.Size(width, height - getY(safeLimit))
                    )
                    // WARN Zone (Orange tint)
                    drawRect(
                        color = Color(0xFFFF9500).copy(alpha = 0.06f),
                        topLeft = Offset(0f, getY(dangerLimit)),
                        size = androidx.compose.ui.geometry.Size(width, getY(safeLimit) - getY(dangerLimit))
                    )
                    // DANGER Zone (Red tint)
                    drawRect(
                        color = Color(0xFFFF3B30).copy(alpha = 0.06f),
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(width, getY(dangerLimit))
                    )

                    // 2. Draw Grid Lines at Thresholds
                    val gridAlpha = 0.05f
                    drawLine(Color.Gray.copy(alpha = gridAlpha), Offset(0f, getY(safeLimit)), Offset(width, getY(safeLimit)), strokeWidth = 1.dp.toPx())
                    drawLine(Color.Gray.copy(alpha = gridAlpha), Offset(0f, getY(dangerLimit)), Offset(width, getY(dangerLimit)), strokeWidth = 1.dp.toPx())

                    val path = Path()
                    val fillPath = Path()
                    
                    data.points.forEachIndexed { i, point ->
                        val x = marginPx + i * spacing
                        val y = getY(point.y).coerceIn(0f, height)
                        
                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = marginPx + (i - 1) * spacing
                            val prevY = getY(data.points[i - 1].y).coerceIn(0f, height)
                            
                            // Cubic curve for ultra-smooth line
                            val cp1X = prevX + (x - prevX) / 2
                            path.cubicTo(cp1X, prevY, cp1X, y, x, y)
                            
                            fillPath.cubicTo(cp1X, prevY, cp1X, y, x, y)
                        }
                        
                        if (i == data.points.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // 3. Draw Area Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )

                    // 4. Draw Trend Line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    
                    // 5. Scrubbing Indicator
                    if (selectedIndex != -1) {
                        val scrubX = marginPx + selectedIndex * spacing
                        val scrubY = getY(data.points[selectedIndex].y).coerceIn(0f, height)
                        drawLine(
                            color = lineColor.copy(alpha = 0.5f),
                            start = Offset(scrubX, 0f),
                            end = Offset(scrubX, height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )
                    }
                }
            }
        }
        
        if (data.points.size > 1) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                val availableWidth = maxWidth
                // Estimate label width needs (including some gap)
                val minLabelWidth = 72.dp 
                val maxVisibleLabels = (availableWidth / minLabelWidth).toInt().coerceAtLeast(2)
                val step = (data.points.size / maxVisibleLabels).coerceAtLeast(1)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    data.points.filterIndexed { index, _ -> 
                        index % step == 0 || index == data.points.size - 1 
                    }.forEach { point ->
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
