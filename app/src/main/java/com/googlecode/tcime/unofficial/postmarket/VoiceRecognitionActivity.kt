/*
 * Copyright 2011 Scribe Hwang
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
package com.googlecode.tcime.unofficial.postmarket

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class VoiceRecognitionActivity : AppCompatActivity() {
    /**
     * Create an Intent to do Voice Recognition
     *
     * @see android.app.Activity.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val iVR = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        if (packageManager.queryIntentActivities(iVR, PackageManager.MATCH_DEFAULT_ONLY).size > 0) {
            iVR.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            iVR.putExtra("calling_package", "com.googlecode.tcime.unofficial")
            iVR.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5) // Limit 5 possible results
            iVR.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_speaknow))
            Log.d(TAG, "Got Voice intent, sending...")
            startActivityForResult(iVR, 1)
        } else {
            // Prompt the user to install Voice Recognition
            AlertDialog.Builder(this)
                .setTitle(R.string.str_notavailable)
                .setMessage(R.string.voice_missing)
                .setNeutralButton(android.R.string.ok) { di, i ->
                    di.dismiss()
                    finish()
                }
                .show()
        }
    }

    /**
     * Called when Voice Recognition got the data.
     *
     * @see android.app.Activity.onActivityResult
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) {  // Handle successful
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            Log.d(TAG, "Voice length = " + matches!!.size)
            val results = matches.toTypedArray<CharSequence>()
            for (c in results) Log.i(TAG, "Voice = $c")
            AlertDialog.Builder(this)
                .setTitle(R.string.voice_chooseone)
                .setItems(results) { di, item ->
                    val iResult = Intent()
                    iResult.action = AbstractIME.TEXT_GOT
                    iResult.putExtra("TEXT_RESULT", results[item])
                    Log.d(TAG, "Voice = " + results[item])
                    sendBroadcast(iResult) // Make an Intent to broadcast back to IME
                    finish()
                }
                .setOnCancelListener { dialog ->

                    // Handles user cancellation
                    Log.d(TAG, "Voice cancellation")
                    dialog.dismiss()
                    finish()
                }
                .show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val TAG = "TCIME"
    }
}