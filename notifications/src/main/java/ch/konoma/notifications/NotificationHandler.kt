package ch.konoma.notifications

import android.os.Bundle


public interface NotificationHandler {

    public fun onMessageReceived(from: String?, data: Bundle?)
}