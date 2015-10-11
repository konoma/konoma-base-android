package ch.konoma.notifications

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.microsoft.windowsazure.messaging.NotificationHub
import java.util.*


public class NotificationCenter(public val context: Context, public val settings: NotificationSettings) {

    constructor(context: Context, key: String, iv: String) : this(context, NotificationSettings.fromManifest(context, key, iv))

    private val notificationHub = NotificationHub(settings.hubName, settings.connectionString, context)
    private val preferences = context.getSharedPreferences("ch.konoma.notifications", Context.MODE_PRIVATE)

    init {
        NotificationCenter.registerNotificationCenter(this)
    }


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
                    val token = InstanceID.getInstance(context).getToken(settings.senderIdentifier, GoogleCloudMessaging.INSTANCE_ID_SCOPE)
                    logInfo("Successfully got GCM token")

                    notificationHub.register(token, *(channels.toTypedArray())).registrationId
                    logInfo("Successfully registered for notifications")

                    updateRegisteredChannels(channels)
                } catch (e: Exception) {
                    Log.e("RVBW", "Error registering for push notifications: $e")
                }
            }
        }.execute()
    }


    /// Notification Handlers

    internal val notificationHandlers: MutableSet<NotificationHandler> = hashSetOf()

    public fun addNotificationHandler(notificationHandler: NotificationHandler) {
        notificationHandlers.add(notificationHandler)
    }

    public fun removeNotificationHandler(notificationHandler: NotificationHandler) {
        notificationHandlers.remove(notificationHandler)
    }

    internal fun notifyMessage(from: String?, data: Bundle ?) {
        for (handler in notificationHandlers) {
            handler.onMessageReceived(from, data)
        }
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


    /// Shared State

    companion object {

        private val allNotificationCenters: MutableSet<NotificationCenter> =
                Collections.newSetFromMap(WeakHashMap<NotificationCenter, Boolean>())

        @Synchronized
        internal fun allRegisteredNotificationCenters(): Set<NotificationCenter> {
            return allNotificationCenters.toSet()
        }

        @Synchronized
        private fun registerNotificationCenter(notificationCenter: NotificationCenter) {
            allNotificationCenters.add(notificationCenter)
        }
    }
}
