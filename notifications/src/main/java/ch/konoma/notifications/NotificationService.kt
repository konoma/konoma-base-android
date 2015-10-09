package ch.konoma.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.gcm.GcmListenerService


public class NotificationListenerService : GcmListenerService() {

    override fun onMessageReceived(from: String?, data: Bundle?) {
        logInfo("Received message $data from $from")

        //        val message = data?.getString("alert") ?: ""
        //        val id = data?.getString("messageId")?.toInt() ?: 0
    }

    companion object {

        @JvmStatic
        public fun start(context: Context) {
            context.startService(Intent(context, NotificationListenerService::class.java))
        }
    }
}
