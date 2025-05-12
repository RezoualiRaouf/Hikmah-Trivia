package com.projetapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(TAG, "SMS received, triggering new question")
            triggerNewQuestion(context, "sms")
        }
    }

    private fun triggerNewQuestion(context: Context, reason: String) {
        val intent = Intent(QuizEvents.ACTION_LOAD_NEW_QUESTION).apply {
            putExtra("reason", reason)
        }
        context.sendBroadcast(intent)
    }
}