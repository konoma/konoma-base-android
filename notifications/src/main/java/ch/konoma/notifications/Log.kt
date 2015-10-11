package ch.konoma.notifications

import android.util.Log


internal const val TAG = "Konoma[Notifications]"

internal fun logInfo(message: String) {
    Log.i(TAG, message)
}

internal fun logError(message: String) {
    Log.e(TAG, message)
}
