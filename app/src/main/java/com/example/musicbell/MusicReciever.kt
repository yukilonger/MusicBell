package com.example.musicbell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicReciever  : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tempIntent = Intent(context, MusicActivity::class.java)
        tempIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(tempIntent)
    }
}