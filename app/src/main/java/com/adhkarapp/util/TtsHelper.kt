package com.adhkarapp.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

object TtsHelper {
    private var tts: TextToSpeech? = null
    private var initialized = false
    private var onDone: (() -> Unit)? = null
    private var repeatCount = 1
    private var currentRepeat = 0
    private var lastText = ""

    fun init(context: Context, onReady: () -> Unit = {}) {
        if (initialized) {
            onReady()
            return
        }
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ar")
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(0.75f)
                val maleVoice = tts?.voices?.find { v ->
                    v.locale.language == "ar" &&
                    (v.name.contains("male", ignoreCase = true) ||
                     !v.name.contains("female", ignoreCase = true))
                }
                if (maleVoice != null) {
                    tts?.voice = maleVoice
                }
                initialized = true
                onReady()
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                currentRepeat++
                if (currentRepeat < repeatCount) {
                    speak(lastText, repeatCount - currentRepeat)
                } else {
                    onDone?.invoke()
                }
            }
            override fun onError(utteranceId: String?) {}
        })
    }

    fun speak(text: String, repeat: Int = 1, done: (() -> Unit)? = null) {
        if (!initialized || tts == null) return
        lastText = text
        repeatCount = repeat
        currentRepeat = 0
        onDone = done
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dhikr_utterance")
    }

    fun stop() {
        tts?.stop()
        currentRepeat = 0
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking ?: false
}
