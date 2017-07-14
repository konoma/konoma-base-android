package ch.konoma.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 *  This class can be used to register the notification library as BroadCastReceiver. That enables
 *  the Notification to be processed also when the application is closed.
 *  Add the following xml to the applications AndroidManifest.xml
 *
 *  <receiver android:name="ch.konoma.notifications.NotificationsBroadcastReceiver"
 *      android:permission="com.google.android.c2dm.permission.SEND">
 *    <intent-filter>
 *      <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 *        <category android:name="${applicationId}" />
 *    </intent-filter>
 *  </receiver>
 */
class NotificationsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        logInfo("Received message on BroadcastReceiver")

        for (notificationCenter in NotificationCenter.allRegisteredNotificationCenters()) {
            notificationCenter.notifyMessage(null, intent.extras)
        }
    }
}
