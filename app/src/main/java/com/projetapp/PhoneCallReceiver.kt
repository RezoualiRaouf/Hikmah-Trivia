package com.projetapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class PhoneCallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneCallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Log.d(TAG, "Phone is ringing, triggering new question")
                    triggerNewQuestion(context, "call")
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.d(TAG, "Call is active, may trigger new question when call ends")
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.d(TAG, "Call state is idle, may be returning from a call")
                }
            }
        }
    }

    private fun triggerNewQuestion(context: Context, reason: String) {
        val intent = Intent(QuizEvents.ACTION_LOAD_NEW_QUESTION).apply {
            putExtra("reason", reason)
        }
        context.sendBroadcast(intent)
    }
}