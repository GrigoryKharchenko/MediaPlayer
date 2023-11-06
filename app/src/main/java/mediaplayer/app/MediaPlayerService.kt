package mediaplayer.app

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener {

    private val songs = Songs()

    private var currentSongIndex = FIRST_SONG_INDEX

    private var isForeground = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        if (mediaPlayer == null) {
            createPlayer()
        }
    }

    override fun onBind(p0: Intent?): IBinder {
        stopForeground(STOP_FOREGROUND_REMOVE)
        return MediaPlayerBinder()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        goToNextSong()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIncomingActions(intent)
        if (isForeground) {
            showNotificationPlayer()
        }
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        showNotificationPlayer()
        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        removeNotificationPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        removeNotification()
    }

    fun playSong() {
        mediaPlayer?.start()
    }

    fun stopSong() {
        mediaPlayer?.pause()
        if (isForeground) stopSelf()
    }

    fun goToNextSong() {
        if (currentSongIndex + INDEX_MOVE_STEP > songs.songs.lastIndex) currentSongIndex =
            FIRST_SONG_INDEX
        else currentSongIndex++
        recreatePlayer()
        playSong()
    }

    fun goToPreviousSong() {
        if (currentSongIndex - INDEX_MOVE_STEP < FIRST_SONG_INDEX) currentSongIndex =
            songs.songs.lastIndex
        else currentSongIndex--
        recreatePlayer()
        playSong()
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    private fun showNotificationPlayer() {
        startForeground(NOTIFICATION_PLAYER_ID, createNotification())
        isForeground = true
    }

    private fun createPlayer() {
        mediaPlayer = MediaPlayer.create(this, songs.songs[currentSongIndex].song).apply {
            setOnCompletionListener(this@MediaPlayerService)
        }
    }

    private fun recreatePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        createPlayer()
    }

    private fun playbackAction(actionId: MediaPlayerActions): PendingIntent? =
        actionId.getActionPendingIntent(this)

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        when (playbackAction.action) {
            MediaPlayerActions.PLAY.command -> playSong()
            MediaPlayerActions.PAUSE.command -> stopSong()
            MediaPlayerActions.PREVIOUS.command -> goToPreviousSong()
            MediaPlayerActions.NEXT.command -> goToNextSong()
        }
    }

    private fun createNotification(): Notification {
        val action =
            playbackAction(if (isPlaying()) MediaPlayerActions.PAUSE else MediaPlayerActions.PLAY)
        val iconAction = if (isPlaying()) R.drawable.ic_pause_60 else R.drawable.ic_play_60
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        val largeAlbumCover: Bitmap? = try {
            BitmapFactory.decodeResource(resources, songs.songs[currentSongIndex].albumCover)
        } catch (e: Throwable) {
            null
        }

        return NotificationCompat.Builder(this, MediaPlayerApp.PLAYER_CHANEL).apply {
            setContentTitle(songs.songs[currentSongIndex].nameSong)
            setContentText(songs.songs[currentSongIndex].nameGroup)
            setContentIntent(pendingIntent)
            setOnlyAlertOnce(true)
            largeAlbumCover?.let(::setLargeIcon)
            setSmallIcon(R.drawable.ic_music_note_24)
            addAction(
                R.drawable.ic_previous_42,
                "Previous",
                playbackAction(MediaPlayerActions.PREVIOUS)
            )
            addAction(iconAction, "Play/Pause", action)
            addAction(R.drawable.ic_next_42, "Next", playbackAction(MediaPlayerActions.NEXT))
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }.build()
    }

    private fun removeNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_PLAYER_ID
        )
    }

    private fun removeNotificationPlayer() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForeground = false
    }

    inner class MediaPlayerBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    companion object {
        private const val NOTIFICATION_PLAYER_ID = 101
        private const val INDEX_MOVE_STEP = 1
        private const val FIRST_SONG_INDEX = 0
    }
}
