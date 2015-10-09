package ch.konoma.notifications

import android.content.Context
import com.microsoft.windowsazure.messaging.NotificationHub


public class NotificationCenter(val context: Context, val senderIdentifier: String, val hubName: String, val connectionString: String) {

    private val notificationHub = NotificationHub(hubName, connectionString, context)

    public fun registerForRemoteNotifications() {
        logInfo("Hello, World")
    }
}
