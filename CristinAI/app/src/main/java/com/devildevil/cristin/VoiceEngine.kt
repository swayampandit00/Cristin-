package com.devildevil.cristin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.provider.MediaStore
import android.provider.ContactsContract
import java.util.*

class VoiceEngine(private val context: Context) {

    private var tts: TextToSpeech = TextToSpeech(context) {
        tts.language = Locale.US
    }

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    init {
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

        recognizer.setRecognitionListener(object : SimpleListener() {
            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = data?.get(0)?.lowercase() ?: ""
                handleCommand(command)
                startListening()
            }
        })
    }

    fun startListening() {
        recognizer.startListening(intent)
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun handleCommand(cmd: String) {
        if (cmd.contains("hello cristin")) {
            speak("Yes, how can I help?")
        }

        if (cmd.contains("call")) {
            val name = cmd.replace("call", "").trim()
            val num = getNumberFromName(name)
            if (num != null) {
                speak("Calling $name")
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:$num")
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(callIntent)
            } else speak("I could not find $name")
        }

        if (cmd.contains("play music")) {
            speak("Playing music")
            val intent = Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private fun getNumberFromName(name: String): String? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor = context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val contactName = it.getString(
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
                if (contactName.equals(name, ignoreCase = true)) {
                    return it.getString(
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                }
            }
        }
        return null
    }
}
