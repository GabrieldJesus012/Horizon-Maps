package com.headunit.launcher

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Only here because Android lets an app see which music app is playing (and control it) when the person allows "notification
 * access" for it. It does nothing with notifications: it never reads, stores or sends them.
 */
class MediaListener : NotificationListenerService()

/** What's playing in the music app (Spotify, YouTube Music...), for the car dashboard, with play/pause and skip. */
object NowPlaying {
    var allowed by mutableStateOf(false)
    var title by mutableStateOf("")
    var artist by mutableStateOf("")
    var app by mutableStateOf("")
    var art by mutableStateOf<android.graphics.Bitmap?>(null)
    var playing by mutableStateOf(false)
    var position by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    private val names = mapOf(
        "com.spotify.music" to "Spotify", "com.google.android.apps.youtube.music" to "YouTube Music", "com.deezer.android.app" to "Deezer",
        "com.amazon.mp3" to "Amazon Music", "com.apple.android.music" to "Apple Music", "com.google.android.youtube" to "YouTube",
        "com.soundcloud.android" to "SoundCloud", "br.com.globo.globoplay" to "Globoplay",
    )

    fun isAllowed(ctx: Context): Boolean = try {
        androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)
    } catch (e: Exception) { false }

    private fun controller(ctx: Context): MediaController? = try {
        val m = ctx.getSystemService(MediaSessionManager::class.java)
        val list = m.getActiveSessions(ComponentName(ctx, MediaListener::class.java))
        list.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: list.firstOrNull()
    } catch (e: Exception) { null }

    /** Reads the current song (called about once a second while the dashboard shows). */
    fun refresh(ctx: Context) {
        allowed = isAllowed(ctx)
        if (!allowed) return
        val c = controller(ctx)
        if (c == null) { title = ""; artist = ""; art = null; playing = false; app = ""; position = 0L; duration = 0L; return }
        val md = c.metadata
        title = md?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        artist = (md?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: md?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)).orEmpty()
        val bmp = md?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: md?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: md?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        if (bmp !== art) art = bmp
        duration = md?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val st = c.playbackState
        playing = st?.state == PlaybackState.STATE_PLAYING
        position = if (st == null) 0L else if (playing && st.lastPositionUpdateTime > 0)
            st.position + ((android.os.SystemClock.elapsedRealtime() - st.lastPositionUpdateTime) * st.playbackSpeed).toLong()
        else st.position
        app = names[c.packageName] ?: try {
            ctx.packageManager.getApplicationLabel(ctx.packageManager.getApplicationInfo(c.packageName, 0)).toString()
        } catch (e: Exception) { "Música" }
    }

    fun playPause(ctx: Context) { controller(ctx)?.transportControls?.let { if (playing) it.pause() else it.play() }; playing = !playing }
    fun next(ctx: Context) { controller(ctx)?.transportControls?.skipToNext() }
    fun prev(ctx: Context) { controller(ctx)?.transportControls?.skipToPrevious() }
}
