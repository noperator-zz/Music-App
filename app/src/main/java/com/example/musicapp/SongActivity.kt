package com.example.musicapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView

const val EXTRA_SONG_PATH = "path"
class SongActivity : AppCompatActivity() {


    private lateinit var musicSrv: MusicService
    private lateinit var playIntent: Intent
    private var musicBound: Boolean = false
    private var currentPath : String = ""

    private lateinit var songSeek : MyScrollBar;

    private fun log(text : String) {
        Log.e("SongActivity", text)
    }

    //connect to the service
    private var musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            log("serivce connected")
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder;
            //get service
            musicSrv = binder.service
            musicBound = true;

            musicSrv.setSong(currentPath);
            musicSrv.playSong();
        }

        override fun onServiceDisconnected(name: ComponentName) {
            log("service disconnected")
            musicBound = false;
        }
    };

    override fun onBackPressed() {
//        finish()
        val intent = Intent(this, ListActivity::class.java).apply {
//            setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
//        moveTaskToBack(true)
//        super.onBackPressed()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    fun onPauseClick(v : View) {
        musicSrv.togglePause()
    }

    fun onNextClick(v : View) {

    }

    fun onPrevClick(v : View) {

    }

//    fun onSeek(position: Float) {
//
//    }

    private fun SetProgress() {
        val position = musicSrv.getPosn().toFloat()
        val duration = musicSrv.getDur().toFloat()
        val percent = position / duration
        log("progress $position / $duration = $percent")
        songSeek.setPercent(percent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("create")
        setContentView(R.layout.activity_song)

        songSeek = findViewById<MyScrollBar>(R.id.songSeek)


        playIntent = Intent(this, MusicService::class.java)
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);


        songSeek.onChanged = {
            log("seek " + it.toString())
            musicSrv.seek((it * musicSrv.getDur().toFloat()).toInt())
            SetProgress()
        }
    }

    override fun onStart() {
        super.onStart();
        log("start")


        val intent = getIntent()
        currentPath = intent.getStringExtra(EXTRA_SONG_PATH)!!
        val songInfo = findViewById<TextView>(R.id.songInfo)
        songInfo.text = currentPath


        songSeek.setPercent(0.0f)
//        songSeek.setMax(600.0f)


    }

    override fun onRestart() {
        super.onRestart();
        log("restart")
    }

    override fun onPause() {
        super.onPause()
        log("pause")
    }

    override fun onResume() {
        super.onResume()
        log("resume")
    }

    override fun onStop() {
        log("stop")
        super.onStop()
    }

    override fun onDestroy() {
        log("destroy")
        stopService(playIntent);
//        musicSrv=null;
        super.onDestroy();
    }
}
