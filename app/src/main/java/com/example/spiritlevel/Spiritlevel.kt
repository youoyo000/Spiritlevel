package com.example.spiritlevel

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpiritLevelView() {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var xTilt by remember { mutableFloatStateOf(0f) }
    var yTilt by remember { mutableFloatStateOf(0f) }

    // Sensor event listener to get the tilt values from the accelerometer.
    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                // Reversing the direction of tilt to match the physical direction
                xTilt = -event.values[0]
                yTilt = -event.values[1]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    LaunchedEffect(Unit) {
        // Register the accelerometer listener when the composable is first launched.
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    DisposableEffect(Unit) {
        // Unregister the listener when the composable is no longer in use.
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Main layout with two bars: one vertical and one horizontal.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Vertical bar with ball
            SpiritLevelBar(
                modifier = Modifier
                    .weight(1f)
                    .width(50.dp),
                tilt = yTilt,
                isVertical = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Horizontal bar with ball
            SpiritLevelBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                tilt = xTilt,
                isVertical = false
            )
        }
    }
}

@Composable
fun SpiritLevelBar(modifier: Modifier, tilt: Float, isVertical: Boolean) {
    val ballSize = 40.dp
    val maxTilt = 4.5f // 45 degree, Maximum tilt angle that moves the ball completely to one side.

    // Animatable values to create smooth animations.
    val offsetAnim = remember { Animatable(0f) }

    var maxOffset: Float
    var barSize: Float

    // Display tilt angle in degrees near the bar.
    val tiltAngle =
        (tilt * 10).toInt()  //because it will be like 2.3827456982374... to convert it to 23 degree

    BoxWithConstraints(
        modifier = modifier
            .background(Color.LightGray.copy(0.4f), RoundedCornerShape(20.dp))
            .padding(2.dp)
    ) {
        barSize = if (isVertical) maxHeight.value else maxWidth.value
        maxOffset = barSize / 2f - ballSize.value / 2f

        // Calculate the target offset based on the tilt and constrain it within the allowed range.
        val targetOffset = (tilt / maxTilt).coerceIn(-1f, 1f) * maxOffset

        LaunchedEffect(targetOffset) {
            // Animate the ball's position smoothly to the new target offset.
            offsetAnim.animateTo(
                targetValue = targetOffset,
                animationSpec = tween(durationMillis = 100) // Adjust duration for smoother animation
            )
        }


        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(ballSize)
                .offset(
                    x = if (!isVertical) offsetAnim.value.dp else 0.dp,
                    y = if (isVertical) -offsetAnim.value.dp else 0.dp // Inverted direction for vertical bar
                )
        ) {
            val ballColor = if (tiltAngle == 0) Color.Green else Color.Red
            drawCircle(color = ballColor)
        }

    }


    Text(
        text = "${tiltAngle}°",
        color = Color.Green,
        fontSize = 25.sp
    )
}