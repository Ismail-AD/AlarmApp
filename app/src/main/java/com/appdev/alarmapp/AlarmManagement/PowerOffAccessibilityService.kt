package com.appdev.alarmapp.AlarmManagement
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.appdev.alarmapp.Repository.RingtoneRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PowerOffAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var ringtoneRepository: RingtoneRepository
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Check if the event type is relevant
        if ((event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) ||
            (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        ) {

            val rootNode = rootInActiveWindow ?: return
            CoroutineScope(Dispatchers.IO).launch {
                ringtoneRepository.getBasicSettings.collect { alarmSet ->
                    if(alarmSet.preventPhoneOff){
                        // Traverse the node tree to find the "Power off" button or similar indicators
                        if (findPowerOffNode(rootNode)) {
                            performGlobalAction(GLOBAL_ACTION_HOME)
                        }
                    }
                }
            }
        }
    }

    private fun findPowerOffNode(node: AccessibilityNodeInfo): Boolean {
        if (node.text?.toString()?.contains("Power off", false) == true) {
            return true
        }

        // Recursively check child nodes
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null && findPowerOffNode(childNode)) {
                return true
            }
        }
        return false
    }

    override fun onInterrupt() {
        // Handle interrupt events if necessary
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("CHKACC","Service connected !")
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        this.serviceInfo = info
    }
}
