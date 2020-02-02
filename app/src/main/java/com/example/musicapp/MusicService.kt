package com.example.musicapp

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.PowerManager
import android.os.Binder
import com.example.musicapp.MusicService.MusicBinder
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioFocusRequest
import android.app.NotificationChannel
import android.graphics.Color
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener  {

    //media player
    private lateinit var player: MediaPlayer
    private val musicBind = MusicBinder()
    private lateinit var currentSong: SongModel;
    private val NOTIFY_ID = 1
    private val NOTIFICATION_CHANNEL_ID = "com.example.musicapp"
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    fun setSong(path: SongModel) {
        currentSong = path
    }

    fun playSong() {
        player.reset();
        try {
            player.setDataSource(currentSong.path)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync();
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playNext() {

    }

    fun playPrev() {

    }

    override fun onCreate() {
        //create the service
        super.onCreate()
        //create player
        player = MediaPlayer()
        initMusicPlayer();
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        requestAudioFocus(this)
    }

    override fun onDestroy() {
        stopForeground(true);
    }

    fun requestAudioFocus(audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .setAudioAttributes(audioAttributes)
            .build()

        audioManager.requestAudioFocus(audioFocusRequest)
    }

    fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Log.e("focus", focusChange.toString())
        if ((AudioManager.AUDIOFOCUS_LOSS == focusChange) ||
         (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT == focusChange)) {
            pausePlayer()
        } else if (AudioManager.AUDIOFOCUS_GAIN == focusChange) {
            go()
        }
    }

    fun initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    override fun onBind(intent: Intent): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onPrepared(mp: MediaPlayer) {
        //start playback
        mp.start()

        val notIntent = Intent(this, MainActivity::class.java);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        val pendInt = PendingIntent.getActivity(this, 0,
        notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "My service",
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        val not = builder.setContentIntent(pendInt)
            .setSmallIcon(R.drawable.play)
            .setTicker(currentSong.title)
            .setOngoing(true)
            .setContentTitle("Playing")
            .setContentText(currentSong.title)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE).build()

        startForeground(NOTIFY_ID, not);
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (player.currentPosition > 0) {
            mp.reset()
            playNext()
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }
}
