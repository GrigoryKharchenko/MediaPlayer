package mediaplayer.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class MediaPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createPlayerNotification()
    }

    private fun createPlayerNotification() {
        val name = "PlayerNotification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(PLAYER_CHANEL, name, importance)

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    companion object {
        const val PLAYER_CHANEL = "PlayerChanel"
    }
}
