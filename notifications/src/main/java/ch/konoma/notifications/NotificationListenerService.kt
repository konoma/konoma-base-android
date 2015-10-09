package ch.konoma.notifications

import android.os.Bundle
import com.google.android.gms.gcm.GcmListenerService


public class NotificationListenerService : GcmListenerService() {

    override fun onMessageReceived(from: String?, data: Bundle?) {
        logInfo("Received message $data from $from")

        //        val message = data?.getString("alert") ?: ""
        //        val id = data?.getString("messageId")?.toInt() ?: 0
    }
}
