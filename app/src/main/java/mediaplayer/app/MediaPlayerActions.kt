package mediaplayer.app

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

enum class MediaPlayerActions(private val id: Int, val command:String ) {
    PLAY(1,"ACTION_PLAY"),
    PAUSE(2,"ACTION_PAUSE"),
    PREVIOUS(3,"ACTION_PREVIOUS"),
    NEXT(4,"ACTION_NEXT");

    fun getActionPendingIntent(context: Context): PendingIntent? =
        Intent(context, MediaPlayerService::class.java).let { action ->
            when (id) {
                PREVIOUS.id -> {
                    action.action = PREVIOUS.command
                    return PendingIntent.getService(
                        context,
                        PREVIOUS.id,
                        action,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                PLAY.id -> {
                    action.action = PLAY.command
                    return PendingIntent.getService(
                        context,
                        PLAY.id,
                        action,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                PAUSE.id -> {
                    action.action = PAUSE.command
                    return PendingIntent.getService(
                        context,
                        PAUSE.id,
                        action,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                NEXT.id -> {
                    action.action = NEXT.command
                    return PendingIntent.getService(
                        context,
                        NEXT.id,
                        action,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                else -> null
            }
        }
}
