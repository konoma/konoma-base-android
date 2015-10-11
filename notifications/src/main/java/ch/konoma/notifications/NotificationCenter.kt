package ch.konoma.notifications

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.microsoft.windowsazure.messaging.NotificationHub


public class NotificationCenter(val context: Context, val senderIdentifier: String, val hubName: String, val connectionString: String) {

    private val notificationHub = NotificationHub(hubName, connectionString, context)
    private val preferences = context.getSharedPreferences("ch.konoma.notifications", Context.MODE_PRIVATE)


    /// Registering

    public fun registerForNotifications(initialChannels: Set<String> = emptySet()) {
        if (isInitialized) {
            registerForNotificationsOnChannels(registeredChannels)
        } else {
            registerForNotificationsOnChannels(initialChannels)
        }
    }

    public fun addRegisteredChannel(channel: String) {
        val channels = registeredChannels.toMutableSet()
        channels.add(channel)

        registerForNotificationsOnChannels(channels)
    }

    public fun removeRegisteredChannel(channel: String) {
        val channels = registeredChannels.toMutableSet()
        channels.remove(channel)

        registerForNotificationsOnChannels(channels)
    }

    public fun registerForNotificationsOnChannels(channels: Set<String>) {
        logInfo("Registering for notifications on channels ${channels.sorted()}")

        NotificationListenerService.registerGcmReceiver(context)

        object : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg backgroundParams: Unit) {
                try {
                    val token = InstanceID.getInstance(context).getToken(senderIdentifier, GoogleCloudMessaging.INSTANCE_ID_SCOPE)
                    logInfo("Successfully got GCM token: $token")

                    val registrationId = notificationHub.register(token, *(channels.toTypedArray())).registrationId
                    logInfo("Successfully registered for notifications with regID: $registrationId")

                    updateRegisteredChannels(channels)
                } catch (e: Exception) {
                    Log.e("RVBW", "Error registering for push notifications: $e")
                }
            }
        }.execute()
    }


    /// Managing the Channels

    public val isInitialized: Boolean
        get() = preferences.getBoolean("initialized", false)

    public val registeredChannels: Set<String>
        get() = preferences.getStringSet("channels", emptySet())

    private fun updateRegisteredChannels(channels: Set<String>) {
        preferences.edit()
                .putStringSet("channels", channels)
                .putBoolean("initialized", true)
                .commit()
    }
}
