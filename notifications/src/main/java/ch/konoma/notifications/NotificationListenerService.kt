package ch.konoma.notifications

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.gms.gcm.GcmListenerService
import com.google.android.gms.gcm.GcmReceiver


internal class NotificationListenerService : GcmListenerService() {

    override fun onMessageReceived(from: String?, data: Bundle?) {
        logInfo("Received message $data from $from")

        for (notificationCenter in NotificationCenter.allRegisteredNotificationCenters()) {
            notificationCenter.notifyMessage(from, data)
        }
    }

    companion object {

        private var hasRegisteredReceiver = false

        fun registerGcmReceiver(context: Context) {
            if (hasRegisteredReceiver) {
                return
            }

            // This is equivalent to the following declaration in the manifest:
            //
            // <receiver
            //   android:name="com.google.android.gms.gcm.GcmReceiver"
            //   android:exported="true"
            //   android:permission="com.google.android.c2dm.permission.SEND">
            //
            //     <intent-filter>
            //         <action android:name="com.google.android.c2dm.intent.RECEIVE"/>
            //         <action android:name="com.google.android.c2dm.intent.REGISTRATION"/>
            //         <category android:name="${applicationId}"/>
            //     </intent-filter>
            // </receiver>

            val intentFilter = IntentFilter()
            intentFilter.addAction("com.google.android.c2dm.intent.RECEIVE")
            intentFilter.addAction("com.google.android.c2dm.intent.REGISTRATION")
            intentFilter.addCategory(context.packageName)

            val permission = "com.google.android.c2dm.permission.SEND"

            context.registerReceiver(
                    GcmReceiver(),
                    intentFilter,
                    permission,
                    Handler(Looper.getMainLooper())
            )
        }
    }
}
