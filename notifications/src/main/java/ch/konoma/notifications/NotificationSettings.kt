package ch.konoma.notifications

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import org.json.JSONObject
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


data class NotificationSettings(
        val senderIdentifier: String,
        val hubName: String,
        val connectionString: String)
{
    companion object {

        @JvmStatic
        fun fromManifest(context: Context, key: String, iv: String,
                         settingsKey: String): NotificationSettings
        {
            val metaData = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData
            val encryptedSettings = metaData.getString(settingsKey) ?: throw IllegalArgumentException("Could not find meta-data entry $settingsKey")
            val settingsJson = decryptSettings(encryptedSettings, key, iv)

            return settingsFromJsonString(settingsJson)
        }

        private fun decryptSettings(encrypted: String, key: String, iv: String): String {
            val encryptedBytes = Base64.decode(encrypted, Base64.DEFAULT)
            val keyBytes = Base64.decode(key, Base64.DEFAULT)
            val ivBytes = Base64.decode(iv, Base64.DEFAULT)

            val ivSpec = IvParameterSpec(ivBytes)
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

            try {
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                return String(cipher.doFinal(encryptedBytes))
            } catch (ex: Exception) {
                logError("Could not decrypt settings")
                throw ex
            }
        }

        private fun settingsFromJsonString(jsonString: String): NotificationSettings {
            val settingsObject = JSONObject(jsonString)

            fun readKey(key: String): String {
                return settingsObject.optString(key)
                        ?: throw IllegalArgumentException("No value for key $key in notification settings")
            }

            return NotificationSettings(
                    senderIdentifier = readKey("sender"),
                    hubName          = readKey("hubname"),
                    connectionString = readKey("connection_string")
            )
        }
    }
}