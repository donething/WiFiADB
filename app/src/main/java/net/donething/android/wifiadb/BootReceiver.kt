package net.donething.android.wifiadb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    private var prefs: SharedPreferences? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            prefs =
                prefs ?: context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            if (prefs?.getBoolean(MainActivity.SW_ADB, false) == true) {
                MainActivity.switchWiFiAdb(true, context)
            }
        }
    }
}
