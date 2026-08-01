package com.reflex.widgethub.ui

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

fun hapticDurationMillis(goalReached: Boolean): Long = if (goalReached) 120L else 24L

fun hapticAmplitude(goalReached: Boolean): Int = if (goalReached) 255 else 160

/**
 * Widget taps run in a background process. Since API 29 the system ignores
 * vibrations with USAGE_UNKNOWN from the background, so we attach alarm/sonification
 * attributes so the tasbeeh click haptic is actually delivered.
 */
fun performWidgetHaptic(context: Context, goalReached: Boolean) {
    val vibrator = resolveVibrator(context) ?: return
    if (!vibrator.hasVibrator()) return

    val duration = hapticDurationMillis(goalReached)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        @Suppress("DEPRECATION")
        vibrator.vibrate(duration)
        return
    }

    val effect = VibrationEffect.createOneShot(duration, hapticAmplitude(goalReached))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val attrs = VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_ALARM)
            .build()
        vibrator.vibrate(effect, attrs)
    } else {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        @Suppress("DEPRECATION")
        vibrator.vibrate(effect, attrs)
    }
}

private fun resolveVibrator(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}
