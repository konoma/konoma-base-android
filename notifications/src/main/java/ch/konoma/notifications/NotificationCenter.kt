package ch.konoma.notifications

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.microsoft.windowsazure.messaging.NotificationHub
import java.util.*


class NotificationCenter(val context: Context, val settings: NotificationSettings) {

    constructor(context: Context, key: String, iv: String) : this(context, NotificationSettings.fromManifest(context, key, iv, "ch.konoma.notifications.settings"))
    constructor(context: Context, key: String, iv: String, settingsKey: String) : this(context, NotificationSettings.fromManifest(context, key, iv, settingsKey))

    private val notificationHub = NotificationHub(settings.hubName, settings.connectionString, context)
    private val preferences = context.getSharedPreferences("ch.konoma.notifications", Context.MODE_PRIVATE)

    var requestedChannels: MutableSet<String> = hashSetOf()
        private set(newValue) {
            field = newValue
        }

    init {
        NotificationCenter.registerNotificationCenter(this)
        this.requestedChannels.addAll(this.registeredChannels)
    }


    /// Registering

    fun registerForNotifications(initialChannels: Set<String> = emptySet()) {
        this.requestedChannels.clear()
        if (this.isInitialized) {
            this.requestedChannels.addAll(this.registeredChannels)
            this.registerForNotificationsOnChannels(this.registeredChannels)
        } else {
            this.requestedChannels.addAll(initialChannels)
            this.registerForNotificationsOnChannels(initialChannels)
        }
    }

    fun addRegisteredChannel(channel: String) {
        requestedChannels.add(channel)

        registerForNotificationsOnChannels(requestedChannels)
    }

    fun removeRegisteredChannel(channel: String) {
        requestedChannels.remove(channel)

        registerForNotificationsOnChannels(requestedChannels)
    }

    fun registerForNotificationsOnChannels(channels: Set<String>) {
        logInfo("Registering for notifications on channels ${channels.sorted()}")

        object : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg backgroundParams: Unit) {
                try {
                    val token = FirebaseInstanceId.getInstance().getToken()

                    logInfo("Successfully got FCM token")

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

    fun addNotificationHandler(notificationHandler: NotificationHandler) {
        notificationHandlers.add(notificationHandler)
    }

    fun removeNotificationHandler(notificationHandler: NotificationHandler) {
        notificationHandlers.remove(notificationHandler)
    }

    internal fun notifyMessage(from: String?, data: Bundle?) {
        for (handler in notificationHandlers) {
            handler.onMessageReceived(from, data)
        }
    }


    /// Managing the Channels

    val isInitialized: Boolean
        get() = preferences.getBoolean("initialized", false)

    val registeredChannels: Set<String>
        get() = preferences.getStringSet("channels", emptySet())

    private fun updateRegisteredChannels(channels: Set<String>) {
        preferences.edit()
                .putStringSet("channels", channels)
                .putBoolean("initialized", true)
                .apply()
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
