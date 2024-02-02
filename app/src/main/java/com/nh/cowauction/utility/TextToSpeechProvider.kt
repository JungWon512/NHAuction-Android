package com.nh.cowauction.utility

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

/**
 * Description : TextToSpeech 나중에 디테일 하게 잡을 예정..
 *
 * Created by hmju on 2021-06-03
 */
interface TextToSpeechProvider {
    fun start(msg: String)
    fun stop()
}

class TextToSpeechProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context
) : TextToSpeechProvider, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech
    private val audioManager: AudioManager by lazy { ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var prevText: String? = null

    init {
        var ttsEngineName: String? = null
        TextToSpeech(ctx, null).apply {
            ttsEngineName = engines.find { it.name == "com.google.android.tts" }?.name
        }
        tts = TextToSpeech(ctx, this, ttsEngineName)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                DLogger.d("onStart $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                DLogger.d("onDone $utteranceId")
            }

            override fun onError(utteranceId: String?) {
                DLogger.d("onError $utteranceId")
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                DLogger.d("onRangeStart $utteranceId")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                DLogger.d("onError $errorCode")
            }
        })
    }

    override fun start(msg: String) {
        // TTS 재생하기전 볼륨
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < audioManager.getStreamMaxVolume(
                AudioManager.STREAM_MUSIC
            ).minus(2)
        ) {
//            audioManager.setStreamVolume(
//                AudioManager.STREAM_MUSIC,
//                Math.min(100, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).minus(2)),
//                AudioManager.FLAG_SHOW_UI
//            )
        }
        prevText = msg
        val param = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, msg) }
        val result = tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, msg)

        // 에러가 아닌경우 PrevText 초기화.
        if (result != TextToSpeech.ERROR) {
            prevText = null
        }

    }

    override fun stop() {
        tts.stop()
        tts.shutdown()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.KOREA)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            } else {
                if (!prevText.isNullOrEmpty()) {
                    tts.speak(prevText, TextToSpeech.QUEUE_FLUSH, null, prevText)
                }
            }
        } else {
        }
    }
}

