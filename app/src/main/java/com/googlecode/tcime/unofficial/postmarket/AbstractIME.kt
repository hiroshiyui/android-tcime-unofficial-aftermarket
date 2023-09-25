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
package com.googlecode.tcime.unofficial.postmarket

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.googlecode.tcime.unofficial.postmarket.CandidateView.CandidateViewListener

/**
 * Abstract class extended by ZhuyinIME and CangjieIME.
 */
abstract class AbstractIME : InputMethodService(), OnKeyboardActionListener, CandidateViewListener {
    private var textGot: String? = ""
    @JvmField
    protected var inputView: SoftKeyboardView? = null
    protected var candidatesContainer: CandidatesContainer? = null
    @JvmField
    protected var keyboardSwitch: KeyboardSwitch? = null
    private var editor: Editor? = null
    private var wordDictionary: WordDictionary? = null
    private var phraseDictionary: PhraseDictionary? = null
    private var effect: SoundMotionEffect? = null
    private var orientation = 0
    @JvmField
    protected var hasHardKeyboard = false
    protected var isHardKeyboardShow = false
    private var toastShowedCount = 0
    private var txtReceiver: BroadcastReceiver? = null
    protected abstract fun createKeyboardSwitch(context: Context): KeyboardSwitch?
    protected abstract fun createEditor(): Editor?
    protected abstract fun createWordDictionary(context: Context?): WordDictionary?
    override fun onCreate() {
        super.onCreate()
        keyboardSwitch = createKeyboardSwitch(this)
        editor = createEditor()
        wordDictionary = createWordDictionary(this)
        phraseDictionary = PhraseDictionary(this)
        effect = SoundMotionEffect(this)
        val conf = resources.configuration
        orientation = conf.orientation
        hasHardKeyboard = conf.keyboard != Configuration.KEYBOARD_NOKEYS
        isHardKeyboardShow = conf.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO

        // Create a BroadcastReceiver to catch the TEXT_GOT result
        txtReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, arg1: Intent) {
                textGot = arg1.getStringExtra("TEXT_RESULT")
                Log.d("TCIME", "Broadcast got = $textGot")
                // We can't commitText() on this point. Maybe because the InputConnection is invalid now.
                // Instead, we store it temporarily. Later on onBindInput() we can commit it.
            }
        }
        val iFilter = IntentFilter()
        iFilter.addAction(TEXT_GOT)
        ContextCompat.registerReceiver(
            this,
            txtReceiver,
            iFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        // Use the following line to debug IME service.
        //android.os.Debug.waitForDebugger();
    }

    override fun onDestroy() {
        phraseDictionary!!.close()
        unregisterReceiver(txtReceiver)
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (orientation != newConfig.orientation) {
            // Clear composing text and candidates for orientation change.
            escape()
            orientation = newConfig.orientation
        }
        isHardKeyboardShow = newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
        super.onConfigurationChanged(newConfig)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int,
        newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd,
            candidatesStart, candidatesEnd
        )
        if (candidatesEnd != -1 &&
            (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)
        ) {
            // Clear composing text and its candidates for cursor movement.
            escape()
        }
        // Update the caps-lock status for the current cursor position.
        updateCursorCapsToInputView()
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        outInsets.contentTopInsets = outInsets.visibleTopInsets
    }

    override fun onCreateInputView(): View {
        inputView = layoutInflater.inflate(
            R.layout.input, null
        ) as SoftKeyboardView
        inputView!!.setOnKeyboardActionListener(this)
        return inputView!!
    }

    override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // Android 13 compatibility
        // ref: https://github.com/klausw/hackerskeyboard/commit/c504b79f3783cbf1f6228014fdd0bad288ad0d2c
        super.setCandidatesViewShown(true)
    }

    override fun onCreateCandidatesView(): View {
        candidatesContainer = layoutInflater.inflate(
            R.layout.candidates, null
        ) as CandidatesContainer
        candidatesContainer!!.setCandidateViewListener(this)
        // Android 13 compatibility
        // ref: https://github.com/klausw/hackerskeyboard/commit/c504b79f3783cbf1f6228014fdd0bad288ad0d2c
        super.setCandidatesViewShown(true)
        isExtractViewShown = onEvaluateFullscreenMode()
        return candidatesContainer!!
    }

    override fun onStartInputView(attribute: EditorInfo, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)

        // Reset editor and candidates when the input-view is just being started.
        editor!!.start(attribute.inputType)
        clearCandidates()
        effect!!.reset()
        keyboardSwitch!!.initializeKeyboard(maxWidth)
        // Select a keyboard based on the input type of the editing field.
        keyboardSwitch!!.onStartInput(attribute.inputType)
        bindKeyboardToInputView()
    }

    override fun onFinishInput() {
        // Clear composing as any active composing text will be finished, same as in
        // onFinishInputView, onFinishCandidatesView, and onUnbindInput.
        editor!!.clearComposingText(currentInputConnection)
        super.onFinishInput()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        editor!!.clearComposingText(currentInputConnection)
        super.onFinishInputView(finishingInput)
        // Dismiss any pop-ups when the input-view is being finished and hidden.
        inputView!!.closing()
    }

    override fun onFinishCandidatesView(finishingInput: Boolean) {
        editor!!.clearComposingText(currentInputConnection)
        super.onFinishCandidatesView(finishingInput)
    }

    override fun onBindInput() {
        // If we have textGot, commit it now.
        // This is the workaround solution to call commitText() successfully.
        super.onBindInput()
        if (textGot != "") {
            Log.d("TCIME", "onBindInput commit textGot = $textGot")
            commitText(textGot)
            textGot = ""
        }
    }

    override fun onUnbindInput() {
        editor!!.clearComposingText(currentInputConnection)
        super.onUnbindInput()
    }

    private fun bindKeyboardToInputView() {
        if (inputView != null) {
            // Bind the selected keyboard to the input view.
            inputView!!.keyboard = keyboardSwitch!!.currentKeyboard
            updateCursorCapsToInputView()
        }
    }

    private fun updateCursorCapsToInputView() {
        val ic = currentInputConnection
        if (ic != null && inputView != null) {
            var caps = 0
            val ei = currentInputEditorInfo
            if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
                caps = ic.getCursorCapsMode(ei.inputType)
            }
            inputView!!.updateCursorCaps(caps)
        }
    }

    private fun commitText(text: CharSequence?) {
        if (editor!!.commitText(currentInputConnection, text!!)) {
            // Clear candidates after committing any text.
            clearCandidates()
        }
    }

    override fun onKeyDown(keyCodeParam: Int, event: KeyEvent): Boolean {
        if (keyCodeParam == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            // Handle the back-key to close the pop-up keyboards.
            if (inputView != null && inputView!!.handleBack()) {
                return true
            }
        }
        // Handle DPAD
        if (candidatesContainer != null && candidatesContainer!!.isShown) {
            if (keyCodeParam >= KeyEvent.KEYCODE_DPAD_UP && keyCodeParam <= KeyEvent.KEYCODE_DPAD_CENTER) {
                onKey(keyCodeParam, null)
                return true
            }
        }
        return super.onKeyDown(keyCodeParam, event)
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        if (keyboardSwitch!!.onKey(primaryCode)) {
            escape()
            bindKeyboardToInputView()
            return
        }
        if (handleOption(primaryCode) || handleCapsLock(primaryCode)
            || handleEnter(primaryCode) || handleSpace(primaryCode) || handleDelete(primaryCode)
            || handleDPAD(primaryCode) || handleComposing(primaryCode)
        ) {
            return
        }
        handleKey(primaryCode)
    }

    override fun onText(text: CharSequence) {
        commitText(text)
    }

    override fun onPress(primaryCode: Int) {
        effect!!.vibrate()
        effect!!.playSound()
    }

    override fun onRelease(primaryCode: Int) {
        // no-op
    }

    override fun swipeLeft() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
    }

    override fun swipeRight() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
    }

    override fun swipeUp() {
        // no-op
    }

    override fun swipeDown() {
        requestHideSelf(0)
    }

    override fun onPickCandidate(candidate: String?) {
        // Commit the picked candidate and suggest its following words.
        commitText(candidate)
        setCandidates(
            phraseDictionary!!.getFollowingWords(candidate!![0]), false
        )
    }

    private fun clearCandidates() {
        setCandidates("", false)
    }

    private fun setCandidates(words: String?, highlightDefault: Boolean) {
        if (candidatesContainer != null) {
            setCandidatesViewShown(words!!.isNotEmpty() || editor!!.hasComposingText())
            candidatesContainer!!.setCandidates(words, highlightDefault)
            if (inputView != null) {
                inputView!!.setEscape(candidatesContainer!!.isShown)
            }
        }
    }

    private fun handleOption(keyCode: Int): Boolean {
        if (keyCode == SoftKeyboard.KEYCODE_OPTIONS) {
            // Create a Dialog menu
            val builder = AlertDialog.Builder(this)
                .setTitle(R.string.ime_name)
                .setIcon(android.R.drawable.ic_menu_preferences)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, null)
                .setItems(
                    arrayOf<CharSequence>( //getString(R.string.menu_barcodescan),
                        //getString(R.string.menu_voiceinput),
                        getString(R.string.menu_settings),
                        getString(R.string.menu_switchIME)
                    )
                ) { di, position ->
                    di.dismiss()
                    when (position) {
                        MENU_BARCODESCAN -> {
                            val iBCScan = Intent()
                            iBCScan.setClass(this@AbstractIME, BarcodeScannerActivity::class.java)
                            iBCScan.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(iBCScan)
                        }

                        MENU_VOICEINPUT -> {
                            val iVR = Intent()
                            iVR.setClass(this@AbstractIME, VoiceRecognitionActivity::class.java)
                            iVR.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(iVR)
                        }

                        MENU_SETTINGS -> {
                            val iSetting = Intent()
                            iSetting.setClass(this@AbstractIME, ImePreferenceActivity::class.java)
                            iSetting.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                            startActivity(iSetting)
                        }

                        MENU_SWITCHIME -> (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                            .showInputMethodPicker()
                    }
                }
            val mOptionsDialog = builder.create()
            val window = mOptionsDialog.window
            val lp = window!!.attributes
            lp.token = inputView!!.windowToken
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            window.attributes = lp
            window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            mOptionsDialog.show()
            return true
        }
        return false
    }

    private fun handleCapsLock(keyCode: Int): Boolean {
        return keyCode == Keyboard.KEYCODE_SHIFT && inputView!!.toggleCapsLock()
    }

    private fun handleEnter(keyCode: Int): Boolean {
        if (keyCode == '\n'.code) {
            if (inputView!!.hasEscape()) {
                escape()
            } else if (editor!!.treatEnterAsLinkBreak()) {
                commitText("\n")
            } else {
                sendKeyChar('\n')
            }
            return true
        }
        return false
    }

    private fun handleSpace(keyCode: Int): Boolean {
        if (keyCode == ' '.code) {
            if (candidatesContainer != null && candidatesContainer!!.isShown) {
                // The space key could either pick the highlighted candidate or escape
                // if there's no highlighted candidate and no composing-text.
                if (!candidatesContainer!!.pickHighlighted()
                    && !editor!!.hasComposingText()
                ) {
                    escape()
                }
            } else {
                commitText(" ")
            }
            return true
        }
        return false
    }

    private fun handleDelete(keyCode: Int): Boolean {
        // Handle delete-key only when no composing text.
        if (keyCode == Keyboard.KEYCODE_DELETE && !editor!!.hasComposingText()) {
            if (inputView!!.hasEscape()) {
                escape()
            } else {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            }
            return true
        }
        return false
    }

    private fun handleComposing(keyCode: Int): Boolean {
        if (editor!!.compose(currentInputConnection, keyCode)) {
            // Set the candidates for the updated composing-text and provide default
            // highlight for the word candidates.
            setCandidates(wordDictionary!!.getWords(editor!!.composingText()), true)
            return true
        }
        return false
    }

    private fun handleDPAD(keyCode: Int): Boolean {
        // Handle DPAD keys only
        if (keyCode < KeyEvent.KEYCODE_DPAD_UP || keyCode > KeyEvent.KEYCODE_DPAD_CENTER) {
            return false
        }
        if (candidatesContainer != null && candidatesContainer!!.isShown) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER -> candidatesContainer!!.pickHighlighted()
                KeyEvent.KEYCODE_DPAD_LEFT -> candidatesContainer!!.highlightLeft()
                KeyEvent.KEYCODE_DPAD_RIGHT -> candidatesContainer!!.highlightRight()
                KeyEvent.KEYCODE_DPAD_UP -> candidatesContainer!!.pagePrev()
                KeyEvent.KEYCODE_DPAD_DOWN -> candidatesContainer!!.pageNext()
            }
            return true
        }
        return false
    }

    /**
     * Handles input of SoftKeyboard key code that has not been consumed by
     * other handling-methods.
     */
    private fun handleKey(keyCodeParam: Int) {
        var keyCode = keyCodeParam
        if (isInputViewShown && inputView!!.isShifted) {
            keyCode = keyCode.toChar().uppercaseChar().code
        }
        commitText(keyCode.toChar().toString())
    }

    /**
     * Simulates PC Esc-key function by clearing all composing-text or candidates.
     */
    protected fun escape() {
        editor!!.clearComposingText(currentInputConnection)
        clearCandidates()
    }
    // Hardware Keyboard related methods
    /**
     * Clear the keyboard meta status
     */
    fun clearKeyboardMetaState() {
        val allMetaState = (KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
                or KeyEvent.META_ALT_RIGHT_ON or KeyEvent.META_SHIFT_ON
                or KeyEvent.META_SHIFT_LEFT_ON
                or KeyEvent.META_SHIFT_RIGHT_ON or KeyEvent.META_SYM_ON)
        currentInputConnection.clearMetaKeyStates(allMetaState)
    }

    /**
     * Set the status bar icon
     */
    override fun showStatusIcon(iconResId: Int) {
        if (hasHardKeyboard && isHardKeyboardShow) {
            super.showStatusIcon(iconResId)
        } else {
            hideStatusIcon()
        }
    }

    /**
     * Check if the hard keyboard can be used. To avoid force crash.
     *
     * @param sKB The soft keyboard object. To check if it is ready.
     * @return true if hard keyboard can be used
     */
    fun checkHardKeyboardAvailable(sKB: SoftKeyboard?): Boolean {
        // Hard keyboard is not showed
        if (!isHardKeyboardShow) return false
        if (sKB == null || inputView == null) {
            // Prompt user to close the keyboard and reopen it to initialize
            if (toastShowedCount < 3) {
                Toast.makeText(this, R.string.str_needsreopen, Toast.LENGTH_SHORT)
                    .show()
                ++toastShowedCount
            }
            return false
        }
        return true
    }

    /**
     * Handles the Language Change event (English <-> Chinese).
     * Normally Shift + Space key, or Language Change key
     *
     * @param keyCode
     * @param event
     * @return true if handled
     */
    fun handleLanguageChange(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isShiftPressed && keyCode == KeyEvent.KEYCODE_SPACE || keyCode == 1000) { // 1000 is hard-coded by MS3
            // Clear all meta state
            clearKeyboardMetaState()
            onKey(SoftKeyboard.KEYCODE_MODE_CHANGE_LETTER, null)
            showStatusIcon(keyboardSwitch!!.languageIcon)
            return true
        }
        return false
    }

    companion object {
        const val TEXT_GOT = "com.googlecode.tcime.unofficial.TEXT_GOT"
        private const val MENU_BARCODESCAN = 2 //0
        private const val MENU_VOICEINPUT = 3 //1
        private const val MENU_SETTINGS = 0 //2
        private const val MENU_SWITCHIME = 1 //3
    }
}