package com.appdev.alarmapp.DeviceAdminManage

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class DeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        // Device admin is enabled
    }

    override fun onDisabled(context: Context, intent: Intent) {
        // Device admin is disabled
    }
}