package ch.konoma.notifications

import android.os.Bundle


interface NotificationHandler {

    fun onMessageReceived(from: String?, data: Bundle?)
}