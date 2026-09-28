package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DanaBorderDark
import com.example.ui.theme.DanaGold
import com.example.ui.theme.DanaSurfaceDark
import com.example.ui.theme.DanaSurfaceElevatedDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    isCandleChart: Boolean = true,
    activeIndicator: String = "MA", // "MA", "BOLL", "NONE"
    decimals: Int = 2,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(DanaSurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading market data...", color = TextSecondaryDark, fontSize = 12.sp)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    val currentSelectedCandle = selectedIndex?.let { idx ->
        if (idx in candles.indices) candles[idx] else candles.lastOrNull()
    } ?: candles.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DanaSurfaceDark)
    ) {
        // Scrubber / OHLC Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DanaSurfaceElevatedDark.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentSelectedCandle != null) {
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val timeStr = timeFormat.format(Date(currentSelectedCandle.timestamp))

                Text(text = timeStr, color = TextMutedDark, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "O: ${formatVal(currentSelectedCandle.open, decimals)}",
                    color = TextSecondaryDark,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "H: ${formatVal(currentSelectedCandle.high, decimals)}",
                    color = BullGreen,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "L: ${formatVal(currentSelectedCandle.low, decimals)}",
                    color = BearRed,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(6.dp))

                val isCandleUp = currentSelectedCandle.close >= currentSelectedCandle.open
                Text(
                    text = "C: ${formatVal(currentSelectedCandle.close, decimals)}",
                    color = if (isCandleUp) BullGreen else BearRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .pointerInput(candles) {
                    detectTapGestures(
                        onTap = { offset ->
                            val candleWidth = size.width / candles.size
                            val idx = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
                .pointerInput(candles) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val candleWidth = size.width / candles.size
                            val idx = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                            selectedIndex = idx
                        },
                        onDragEnd = {
                            selectedIndex = null
                        },
                        onDragCancel = {
                            selectedIndex = null
                        },
                        onDrag = { change, _ ->
                            val candleWidth = size.width / candles.size
                            val idx = (change.position.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val rightMargin = 120f
                val chartWidth = canvasWidth - rightMargin
                val topMargin = 20f
                val bottomMargin = 40f
                val chartHeight = canvasHeight - topMargin - bottomMargin

                val minPrice = candles.minOf { it.low } * 0.999
                val maxPrice = candles.maxOf { it.high } * 1.001
                val priceRange = max(maxPrice - minPrice, 0.0001)

                fun getY(price: Double): Float {
                    val normalized = ((maxPrice - price) / priceRange).toFloat()
                    return topMargin + (normalized * chartHeight)
                }

                // 1. Draw horizontal grid lines & price labels on right
                val gridCount = 4
                for (i in 0..gridCount) {
                    val price = minPrice + (priceRange / gridCount) * i
                    val y = getY(price)

                    drawLine(
                        color = DanaBorderDark.copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatVal(price, decimals),
                        topLeft = Offset(chartWidth + 8f, y - 14f),
                        style = TextStyle(
                            color = TextMutedDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                val count = candles.size
                val stepX = chartWidth / count
                val candleBodyWidth = max(2f, stepX * 0.68f)

                // 2. Indicators: Moving Averages or Bollinger Bands
                if (activeIndicator == "MA" && count >= 5) {
                    drawMaLine(candles, 7, DanaGold, ::getY, stepX, chartWidth)
                    if (count >= 15) {
                        drawMaLine(candles, 20, AccentCyan, ::getY, stepX, chartWidth)
                    }
                } else if (activeIndicator == "BOLL" && count >= 10) {
                    drawBollingerBands(candles, 14, 2.0, ::getY, stepX)
                }

                // 3. Draw Chart Body (Candles or Line)
                if (isCandleChart) {
                    candles.forEachIndexed { i, candle ->
                        val centerX = (i * stepX) + (stepX / 2f)
                        val openY = getY(candle.open)
                        val closeY = getY(candle.close)
                        val highY = getY(candle.high)
                        val lowY = getY(candle.low)

                        val isUp = candle.close >= candle.open
                        val candleColor = if (isUp) BullGreen else BearRed

                        // Wick line
                        drawLine(
                            color = candleColor,
                            start = Offset(centerX, highY),
                            end = Offset(centerX, lowY),
                            strokeWidth = 2f
                        )

                        // Body rectangle
                        val topY = min(openY, closeY)
                        val bodyHeight = max(kotlin.math.abs(closeY - openY), 2.5f)

                        drawRect(
                            color = candleColor,
                            topLeft = Offset(centerX - candleBodyWidth / 2f, topY),
                            size = Size(candleBodyWidth, bodyHeight)
                        )
                    }
                } else {
                    // Line Area Chart
                    val linePath = Path()
                    val areaPath = Path()

                    candles.forEachIndexed { i, candle ->
                        val x = (i * stepX) + (stepX / 2f)
                        val y = getY(candle.close)
                        if (i == 0) {
                            linePath.moveTo(x, y)
                            areaPath.moveTo(x, canvasHeight - bottomMargin)
                            areaPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            areaPath.lineTo(x, y)
                        }
                    }

                    val lastX = ((count - 1) * stepX) + (stepX / 2f)
                    areaPath.lineTo(lastX, canvasHeight - bottomMargin)
                    areaPath.close()

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            listOf(DanaGold.copy(alpha = 0.35f), Color.Transparent),
                            startY = topMargin,
                            endY = canvasHeight - bottomMargin
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = DanaGold,
                        style = Stroke(width = 3f)
                    )
                }

                // 4. Current Price Pulsing Line across chart
                val latest = candles.last()
                val latestY = getY(latest.close)
                val latestColor = if (latest.close >= latest.open) BullGreen else BearRed

                drawLine(
                    color = latestColor.copy(alpha = 0.85f),
                    start = Offset(0f, latestY),
                    end = Offset(chartWidth, latestY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )

                // Current price tag on right
                drawRect(
                    color = latestColor,
                    topLeft = Offset(chartWidth, latestY - 14f),
                    size = Size(rightMargin, 28f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = formatVal(latest.close, decimals),
                    topLeft = Offset(chartWidth + 6f, latestY - 10f),
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // 5. Crosshair when user interacts
                selectedIndex?.let { idx ->
                    if (idx in candles.indices) {
                        val c = candles[idx]
                        val crossX = (idx * stepX) + (stepX / 2f)
                        val crossY = getY(c.close)

                        // Vertical line
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(crossX, topMargin),
                            end = Offset(crossX, canvasHeight - bottomMargin),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        // Horizontal line
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(0f, crossY),
                            end = Offset(chartWidth, crossY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        // Glowing point
                        drawCircle(
                            color = DanaGold,
                            radius = 4.5f,
                            center = Offset(crossX, crossY)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMaLine(
    candles: List<Candle>,
    period: Int,
    color: Color,
    getY: (Double) -> Float,
    stepX: Float,
    chartWidth: Float
) {
    val path = Path()
    var isStarted = false

    for (i in (period - 1) until candles.size) {
        val subset = candles.subList(i - period + 1, i + 1)
        val maValue = subset.map { it.close }.average()
        val x = (i * stepX) + (stepX / 2f)
        val y = getY(maValue)

        if (!isStarted) {
            path.moveTo(x, y)
            isStarted = true
        } else {
            path.lineTo(x, y)
        }
    }

    if (isStarted) {
        drawPath(path = path, color = color, style = Stroke(width = 2f))
    }
}

private fun DrawScope.drawBollingerBands(
    candles: List<Candle>,
    period: Int,
    multiplier: Double,
    getY: (Double) -> Float,
    stepX: Float
) {
    val upperPath = Path()
    val lowerPath = Path()
    val midPath = Path()
    var started = false

    for (i in (period - 1) until candles.size) {
        val subset = candles.subList(i - period + 1, i + 1)
        val sma = subset.map { it.close }.average()
        val variance = subset.map {
            val diff = it.close - sma
            diff * diff
        }.average()
        val stdDev = kotlin.math.sqrt(variance)

        val upper = sma + (multiplier * stdDev)
        val lower = sma - (multiplier * stdDev)

        val x = (i * stepX) + (stepX / 2f)
        val upperY = getY(upper)
        val lowerY = getY(lower)
        val midY = getY(sma)

        if (!started) {
            upperPath.moveTo(x, upperY)
            lowerPath.moveTo(x, lowerY)
            midPath.moveTo(x, midY)
            started = true
        } else {
            upperPath.lineTo(x, upperY)
            lowerPath.lineTo(x, lowerY)
            midPath.lineTo(x, midY)
        }
    }

    if (started) {
        drawPath(upperPath, color = AccentCyan.copy(alpha = 0.7f), style = Stroke(width = 1.5f))
        drawPath(midPath, color = DanaGold.copy(alpha = 0.6f), style = Stroke(width = 1.5f))
        drawPath(lowerPath, color = AccentCyan.copy(alpha = 0.7f), style = Stroke(width = 1.5f))
    }
}

private fun formatVal(value: Double, decimals: Int): String {
    return "%,.${decimals}f".format(value)
}
