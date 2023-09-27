/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.tcime.unofficial.aftermarket

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Vibrator
import androidx.preference.PreferenceManager

/**
 * Plays sound and motion effect.
 */
class SoundMotionEffect(private val context: Context) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val vibrateKey: String = context.getString(R.string.prefs_vibrate_key)
    private val soundKey: String = context.getString(R.string.prefs_sound_key)
    private var vibrateOn = false
    private var vibrator: Vibrator? = null
    private var soundOn = false
    private var audioManager: AudioManager? = null

    fun reset() {
        vibrateOn = preferences.getBoolean(vibrateKey, false)
        if (vibrateOn && vibrator == null) {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        soundOn = preferences.getBoolean(soundKey, false)
        if (soundOn && audioManager == null) {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }

    fun vibrate() {
        if (vibrateOn && vibrator != null) {
            vibrator!!.vibrate(VIBRATE_DURATION.toLong())
        }
    }

    fun playSound() {
        if (soundOn && audioManager != null) {
            audioManager!!.playSoundEffect(
                AudioManager.FX_KEYPRESS_STANDARD, FX_VOLUME
            )
        }
    }

    companion object {
        private const val VIBRATE_DURATION = 30
        private const val FX_VOLUME = -1.0f
    }
}