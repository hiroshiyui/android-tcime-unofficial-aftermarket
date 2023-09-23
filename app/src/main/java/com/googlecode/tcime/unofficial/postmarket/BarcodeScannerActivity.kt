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
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class BarcodeScannerActivity : AppCompatActivity() {
    /**
     * Create an Intent to scan a QR Code as text
     *
     * @see android.app.Activity.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bcScanner = Intent(PACKAGE + ".SCAN")
        if (packageManager.queryIntentActivities(
                bcScanner,
                PackageManager.MATCH_DEFAULT_ONLY
            ).size > 0
        ) {
            setPackage(bcScanner, PACKAGE) // Try setPackage (API 4)
            bcScanner.putExtra("SCAN_MODE", "QR_CODE_MODE")
            Log.d(TAG, "Got Barcode intent, sending...")
            startActivityForResult(bcScanner, 1)
        } else {
            // Prompt the user to install ZXing Barcode Scanner
            AlertDialog.Builder(this)
                .setTitle(R.string.str_notavailable)
                .setMessage(R.string.barcode_missing)
                .setPositiveButton(android.R.string.yes) { di, i ->
                    val uri = Uri.parse("market://search?q=pname:" + PACKAGE)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton(android.R.string.no) { di, i ->
                    di.dismiss()
                    finish()
                }
                .show()
        }
    }

    /**
     * Called when Barcode Scanner got the data.
     *
     * @see android.app.Activity.onActivityResult
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) {  // Handle successful scan
            Log.d(TAG, "SCAN = " + data!!.getStringExtra("SCAN_RESULT"))
            val iResult = Intent()
            iResult.action = AbstractIME.TEXT_GOT
            iResult.putExtra("TEXT_RESULT", data.getStringExtra("SCAN_RESULT"))
            sendBroadcast(iResult) // Make an Intent to broadcast back to IME
        }
        super.onActivityResult(requestCode, resultCode, data)
        finish() // End this Activity
    }

    companion object {
        private const val TAG = "TCIME"
        private const val PACKAGE = "com.google.zxing.client.android"
        private var SET_PACKAGE: Method?

        init {
            val temp: Method? = try {
                Intent::class.java.getMethod(
                    "setPackage", *arrayOf<Class<*>>(
                        String::class.java
                    )
                )
            } catch (ne: NoSuchMethodException) {
                null
            }
            SET_PACKAGE = temp
        }

        /**
         * Intent.setPackage alternative. Before API 4 the method doesn't exist.
         *
         * @param intent Intent object
         * @param pname  Package name
         * @see android.content.Intent.setPackage
         */
        private fun setPackage(intent: Intent, pname: String) {
            SET_PACKAGE?.apply {
                try {
                    this.invoke(intent, pname)
                } catch (ite: InvocationTargetException) {
                    Log.w(TAG, ite.targetException)
                } catch (iae: IllegalAccessException) {
                    Log.w(TAG, iae)
                }
            }
            if (SET_PACKAGE != null) {
                try {
                    SET_PACKAGE!!.invoke(intent, pname)
                } catch (ite: InvocationTargetException) {
                    Log.w(TAG, ite.targetException)
                } catch (iae: IllegalAccessException) {
                    Log.w(TAG, iae)
                }
            }
        }
    }
}