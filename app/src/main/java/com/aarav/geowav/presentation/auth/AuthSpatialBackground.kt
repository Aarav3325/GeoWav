package com.aarav.geowav.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthSpatialBackground(
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val routeColor = colorScheme.primary.copy(alpha = 0.08f)
    val routeAccentColor = colorScheme.secondary.copy(alpha = 0.07f)
    val anchorColor = colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val quietLineColor = colorScheme.onSurfaceVariant.copy(alpha = 0.045f)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        val width = size.width
        val height = size.height
        val stroke = 1.dp.toPx()

        drawLine(
            color = quietLineColor,
            start = Offset(width * 0.12f, height * 0.16f),
            end = Offset(width * 0.88f, height * 0.08f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = quietLineColor,
            start = Offset(width * 0.08f, height * 0.78f),
            end = Offset(width * 0.92f, height * 0.62f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = routeColor,
            start = Offset(width * 0.18f, height * 0.28f),
            end = Offset(width * 0.78f, height * 0.48f),
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = routeAccentColor,
            start = Offset(width * 0.32f, height * 0.92f),
            end = Offset(width * 0.84f, height * 0.72f),
            strokeWidth = 1.1.dp.toPx(),
            cap = StrokeCap.Round
        )

        listOf(
            Offset(width * 0.18f, height * 0.28f),
            Offset(width * 0.78f, height * 0.48f),
            Offset(width * 0.32f, height * 0.92f),
            Offset(width * 0.84f, height * 0.72f)
        ).forEach { anchor ->
            drawCircle(
                color = anchorColor,
                radius = 3.dp.toPx(),
                center = anchor
            )
            drawCircle(
                color = anchorColor.copy(alpha = 0.04f),
                radius = 13.dp.toPx(),
                center = anchor
            )
        }
    }
}
