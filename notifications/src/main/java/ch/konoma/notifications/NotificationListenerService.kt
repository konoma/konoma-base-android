package ch.konoma.notifications

import android.os.Bundle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


internal class NotificationListenerService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val from = message.from
        val data = message.data

        logInfo("Received message $data from $from")

        val bundle = Bundle()
        for (entry in data.entries) {
            bundle.putString(entry.key, entry.value)
        }

        for (notificationCenter in NotificationCenter.allRegisteredNotificationCenters()) {
            notificationCenter.notifyMessage(from, bundle)
        }
    }
}
