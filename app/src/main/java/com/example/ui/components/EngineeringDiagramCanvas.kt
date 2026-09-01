package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagramPoint
import com.example.data.model.DiagramType
import com.example.data.model.EngineeringDiagramData

@Composable
fun EngineeringDiagramCanvas(
    diagramData: EngineeringDiagramData,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<DiagramPoint?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("engineering_diagram_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Engineering Diagram",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = diagramData.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vector Engineering Blueprint • Tap points to inspect",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Drawing Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        color = Color(0xFF0F172A), // Engineering Blueprint Dark Slate
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                val axisColor = Color(0xFF94A3B8)
                val gridColor = Color(0xFF1E293B)
                val curveColor = Color(0xFF38BDF8) // Electric Cyan
                val highlightColor = Color(0xFFF59E0B) // Amber
                val pointColor = Color(0xFF34D399) // Mint Emerald

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(start = 42.dp, end = 20.dp, top = 20.dp, bottom = 36.dp)
                        .pointerInput(diagramData) {
                            detectTapGestures { tapOffset ->
                                val w = size.width
                                val h = size.height
                                val normalizedX = (tapOffset.x / w).coerceIn(0f, 1f)
                                val normalizedY = (1f - (tapOffset.y / h)).coerceIn(0f, 1f)

                                // Find closest diagram point
                                val closest = diagramData.points.minByOrNull { pt ->
                                    val dx = pt.x - normalizedX
                                    val dy = pt.y - normalizedY
                                    dx * dx + dy * dy
                                }
                                if (closest != null) {
                                    val dist = (closest.x - normalizedX) * (closest.x - normalizedX) +
                                            (closest.y - normalizedY) * (closest.y - normalizedY)
                                    if (dist < 0.05f) {
                                        selectedPoint = closest
                                    } else {
                                        selectedPoint = null
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Grid Lines
                    val gridCols = 6
                    val gridRows = 5
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                    for (i in 1 until gridCols) {
                        val x = (w / gridCols) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f,
                            pathEffect = pathEffect
                        )
                    }
                    for (j in 1 until gridRows) {
                        val y = (h / gridRows) * j
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = pathEffect
                        )
                    }

                    // 2. Draw Main Axes with Arrows
                    // Y Axis
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, h),
                        end = Offset(0f, 0f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                    // Y Arrow head
                    drawLine(axisColor, Offset(0f, 0f), Offset(-6f, 10f), strokeWidth = 2f)
                    drawLine(axisColor, Offset(0f, 0f), Offset(6f, 10f), strokeWidth = 2f)

                    // X Axis
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, h),
                        end = Offset(w, h),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                    // X Arrow head
                    drawLine(axisColor, Offset(w, h), Offset(w - 10f, h - 6f), strokeWidth = 2f)
                    drawLine(axisColor, Offset(w, h), Offset(w - 10f, h + 6f), strokeWidth = 2f)

                    // Draw Diagram Specific Shapes
                    when (diagramData.type) {
                        DiagramType.PV_DIAGRAM_OTTO -> {
                            drawOttoCycle(w, h, curveColor, highlightColor)
                        }
                        DiagramType.PV_DIAGRAM_DIESEL -> {
                            drawDieselCycle(w, h, curveColor, highlightColor)
                        }
                        DiagramType.PV_DIAGRAM_CARNOT, DiagramType.TS_DIAGRAM_CYCLE -> {
                            drawCarnotCycle(w, h, curveColor, highlightColor)
                        }
                        DiagramType.STRESS_STRAIN_CURVE -> {
                            drawStressStrainCurve(w, h, curveColor, highlightColor)
                        }
                        DiagramType.MOHR_CIRCLE -> {
                            drawMohrCircle(w, h, curveColor, highlightColor)
                        }
                        else -> {
                            drawGenericEngineeringGraph(w, h, curveColor, highlightColor)
                        }
                    }

                    // 3. Draw State Points & Labels
                    diagramData.points.forEach { pt ->
                        val px = pt.x * w
                        val py = (1f - pt.y) * h

                        // Glow halo
                        drawCircle(
                            color = pointColor.copy(alpha = 0.35f),
                            radius = 12f,
                            center = Offset(px, py)
                        )
                        // Solid core
                        drawCircle(
                            color = if (selectedPoint?.label == pt.label) highlightColor else pointColor,
                            radius = 6.5f,
                            center = Offset(px, py)
                        )
                        // White center
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = Offset(px, py)
                        )

                        // Native Canvas Label Text
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 32f
                                isFakeBoldText = true
                                setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                            }
                            drawText(pt.label, px + 12f, py - 10f, paint)
                        }
                    }
                }

                // Axis Names Outside Canvas Boundary
                Text(
                    text = diagramData.yAxisLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 6.dp, top = 4.dp)
                )

                Text(
                    text = diagramData.xAxisLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 6.dp)
                )
            }

            // Interactive Inspector Card
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPoint?.let { pt ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "State Point [${pt.label}]",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = pt.description.ifBlank { "Coordinates: (x: ${pt.x}, y: ${pt.y})" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Process Labels List
            if (diagramData.processLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Processes & State Transitions:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                diagramData.processLabels.forEach { label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom Formula & Notes
            if (diagramData.formula.isNotBlank() || diagramData.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (diagramData.formula.isNotBlank()) {
                        Text(
                            text = "Eq: ${diagramData.formula}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (diagramData.notes.isNotBlank()) {
                        Text(
                            text = diagramData.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ================= Canvas Vector Rendering Logic =================

private fun DrawScope.drawOttoCycle(w: Float, h: Float, curveColor: Color, highlightColor: Color) {
    val p1 = Offset(0.85f * w, (1f - 0.15f) * h)
    val p2 = Offset(0.25f * w, (1f - 0.45f) * h)
    val p3 = Offset(0.25f * w, (1f - 0.88f) * h)
    val p4 = Offset(0.85f * w, (1f - 0.35f) * h)

    // Shaded Area (Net Work W_net)
    val fillPath = Path().apply {
        moveTo(p1.x, p1.y)
        quadraticTo(0.48f * w, (1f - 0.22f) * h, p2.x, p2.y) // 1-2 isentropic
        lineTo(p3.x, p3.y) // 2-3 isochoric
        quadraticTo(0.52f * w, (1f - 0.52f) * h, p4.x, p4.y) // 3-4 isentropic
        close()
    }
    drawPath(fillPath, color = curveColor.copy(alpha = 0.15f))

    // 1-2 Isentropic Compression (Curve)
    val path12 = Path().apply {
        moveTo(p1.x, p1.y)
        quadraticTo(0.48f * w, (1f - 0.22f) * h, p2.x, p2.y)
    }
    drawPath(path12, curveColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

    // 2-3 Constant Volume Heat Addition (Vertical Line)
    drawLine(highlightColor, p2, p3, strokeWidth = 4f, cap = StrokeCap.Round)

    // 3-4 Isentropic Expansion / Power Stroke (Curve)
    val path34 = Path().apply {
        moveTo(p3.x, p3.y)
        quadraticTo(0.52f * w, (1f - 0.52f) * h, p4.x, p4.y)
    }
    drawPath(path34, curveColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

    // 4-1 Constant Volume Heat Rejection (Vertical Line)
    drawLine(Color(0xFFEF4444), p4, p1, strokeWidth = 3f, cap = StrokeCap.Round)

    // Heat Addition Q_in Arrow at 2-3
    drawArrow(Offset(p2.x - 24f, (p2.y + p3.y) / 2f), Offset(p2.x - 2f, (p2.y + p3.y) / 2f), highlightColor)
    // Heat Rejection Q_out Arrow at 4-1
    drawArrow(Offset(p4.x + 2f, (p4.y + p1.y) / 2f), Offset(p4.x + 24f, (p4.y + p1.y) / 2f), Color(0xFFEF4444))
}

private fun DrawScope.drawDieselCycle(w: Float, h: Float, curveColor: Color, highlightColor: Color) {
    val p1 = Offset(0.9f * w, (1f - 0.15f) * h)
    val p2 = Offset(0.2f * w, (1f - 0.65f) * h)
    val p3 = Offset(0.45f * w, (1f - 0.65f) * h)
    val p4 = Offset(0.9f * w, (1f - 0.32f) * h)

    val fillPath = Path().apply {
        moveTo(p1.x, p1.y)
        quadraticTo(0.45f * w, (1f - 0.28f) * h, p2.x, p2.y)
        lineTo(p3.x, p3.y)
        quadraticTo(0.65f * w, (1f - 0.45f) * h, p4.x, p4.y)
        close()
    }
    drawPath(fillPath, color = curveColor.copy(alpha = 0.15f))

    // 1-2
    val path12 = Path().apply {
        moveTo(p1.x, p1.y)
        quadraticTo(0.45f * w, (1f - 0.28f) * h, p2.x, p2.y)
    }
    drawPath(path12, curveColor, style = Stroke(width = 3.5f))

    // 2-3 Isobaric Constant Pressure Heat Addition
    drawLine(highlightColor, p2, p3, strokeWidth = 4f, cap = StrokeCap.Round)

    // 3-4
    val path34 = Path().apply {
        moveTo(p3.x, p3.y)
        quadraticTo(0.65f * w, (1f - 0.45f) * h, p4.x, p4.y)
    }
    drawPath(path34, curveColor, style = Stroke(width = 3.5f))

    // 4-1
    drawLine(Color(0xFFEF4444), p4, p1, strokeWidth = 3f)
}

private fun DrawScope.drawCarnotCycle(w: Float, h: Float, curveColor: Color, highlightColor: Color) {
    // T-S rectangular diagram
    val p1 = Offset(0.2f * w, (1f - 0.8f) * h)
    val p2 = Offset(0.7f * w, (1f - 0.8f) * h)
    val p3 = Offset(0.7f * w, (1f - 0.3f) * h)
    val p4 = Offset(0.2f * w, (1f - 0.3f) * h)

    // Fill Rectangle
    drawRect(
        color = curveColor.copy(alpha = 0.18f),
        topLeft = Offset(p1.x, p1.y),
        size = Size(p2.x - p1.x, p4.y - p1.y)
    )

    // 1-2 Isothermal TH (Top)
    drawLine(highlightColor, p1, p2, strokeWidth = 4f, cap = StrokeCap.Round)
    // 2-3 Isentropic Expansion (Right vertical)
    drawLine(curveColor, p2, p3, strokeWidth = 3.5f, cap = StrokeCap.Round)
    // 3-4 Isothermal TL (Bottom)
    drawLine(Color(0xFFEF4444), p3, p4, strokeWidth = 4f, cap = StrokeCap.Round)
    // 4-1 Isentropic Compression (Left vertical)
    drawLine(curveColor, p4, p1, strokeWidth = 3.5f, cap = StrokeCap.Round)

    // TH and TL Guideline Dashes
    val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    drawLine(Color(0xFF64748B), Offset(0f, p1.y), Offset(p1.x, p1.y), strokeWidth = 1.5f, pathEffect = dash)
    drawLine(Color(0xFF64748B), Offset(0f, p4.y), Offset(p4.x, p4.y), strokeWidth = 1.5f, pathEffect = dash)
}

private fun DrawScope.drawStressStrainCurve(w: Float, h: Float, curveColor: Color, highlightColor: Color) {
    val o = Offset(0f, h)
    val a = Offset(0.12f * w, (1f - 0.40f) * h) // Proportional limit
    val b = Offset(0.18f * w, (1f - 0.46f) * h) // Elastic limit
    val c = Offset(0.24f * w, (1f - 0.54f) * h) // Upper Yield
    val d = Offset(0.32f * w, (1f - 0.48f) * h) // Lower Yield
    val e = Offset(0.65f * w, (1f - 0.88f) * h) // UTS
    val f = Offset(0.90f * w, (1f - 0.65f) * h) // Fracture

    val fullPath = Path().apply {
        moveTo(o.x, o.y)
        lineTo(a.x, a.y) // Linear elastic
        quadraticTo(0.15f * w, (1f - 0.43f) * h, b.x, b.y)
        lineTo(c.x, c.y) // Yield peak
        lineTo(d.x, d.y) // Yield drop
        cubicTo(0.45f * w, (1f - 0.68f) * h, 0.55f * w, (1f - 0.88f) * h, e.x, e.y) // Strain hardening
        quadraticTo(0.80f * w, (1f - 0.78f) * h, f.x, f.y) // Necking to fracture
    }

    // Shaded Toughness Area
    val fillPath = Path().apply {
        addPath(fullPath)
        lineTo(f.x, h)
        lineTo(0f, h)
        close()
    }
    drawPath(fillPath, color = curveColor.copy(alpha = 0.12f))

    // Main Stroke
    drawPath(fullPath, color = curveColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

    // Yield drop highlight
    drawLine(highlightColor, c, d, strokeWidth = 4f, cap = StrokeCap.Round)
}

private fun DrawScope.drawMohrCircle(w: Float, h: Float, curveColor: Color, highlightColor: Color) {
    val centerX = 0.5f * w
    val centerY = (1f - 0.5f) * h
    val radius = 0.35f * w

    // Principal horizontal axis line through center
    drawLine(
        color = Color(0xFF475569),
        start = Offset(0f, centerY),
        end = Offset(w, centerY),
        strokeWidth = 1.5f
    )

    // Shaded Circle interior
    drawCircle(
        color = curveColor.copy(alpha = 0.12f),
        radius = radius,
        center = Offset(centerX, centerY)
    )

    // Mohr Circle boundary
    drawCircle(
        color = curveColor,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 3.5f)
    )

    // Center point
    drawCircle(color = highlightColor, radius = 5f, center = Offset(centerX, centerY))

    // Radius line
    val ptX = Offset(centerX + (radius * 0.7f), centerY - (radius * 0.7f))
    drawLine(highlightColor, Offset(centerX, centerY), ptX, strokeWidth = 2.5f)
}

private fun DrawScope.drawGenericEngineeringGraph(w: Float, h: Float, curveColor: Color, highlightColor: Color) {
    val path = Path().apply {
        moveTo(0f, h / 2f)
        for (i in 0..100) {
            val progress = i / 100f
            val x = progress * w
            val y = (h / 2f) - (kotlin.math.sin(progress * Math.PI * 4).toFloat() * (h * 0.35f))
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
    }
    drawPath(path, color = curveColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawArrow(start: Offset, end: Offset, color: Color) {
    drawLine(color, start, end, strokeWidth = 3f, cap = StrokeCap.Round)
    val dx = end.x - start.x
    val dy = end.y - start.y
    val angle = kotlin.math.atan2(dy, dx)
    val arrowLen = 10f
    val p1 = Offset(
        (end.x - arrowLen * kotlin.math.cos(angle - Math.PI / 6)).toFloat(),
        (end.y - arrowLen * kotlin.math.sin(angle - Math.PI / 6)).toFloat()
    )
    val p2 = Offset(
        (end.x - arrowLen * kotlin.math.cos(angle + Math.PI / 6)).toFloat(),
        (end.y - arrowLen * kotlin.math.sin(angle + Math.PI / 6)).toFloat()
    )
    drawLine(color, end, p1, strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(color, end, p2, strokeWidth = 2.5f, cap = StrokeCap.Round)
}
