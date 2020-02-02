package com.example.musicapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.nio.file.FileSystem
import android.R.attr.start
import java.io.File.separator
import android.R.attr.path
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.media.MediaPlayer
import java.io.File
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Environment.getExternalStorageDirectory
import android.os.IBinder
import android.os.storage.StorageVolume
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.MediaController
import android.widget.TextView
import java.io.FileFilter
import android.widget.Toast
import androidx.core.view.size
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

const val EXTRA_MESSAGE = "com.example.musicapp.MESSAGE"
const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

//class MyRecycler @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
//) : RecyclerView(context) {
//
//}

//class MyRecycler : RecyclerView {
//    constructor(context: Context) : this(context, null)
//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//
//    }
//}


//class SimpleViewModel(simpleText: String) {
//    var simpleText = simpleText;
//}

class SongViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val titleTextView = itemView.findViewById<TextView>(R.id.title_text);
    val artistTextView = itemView.findViewById<TextView>(R.id.artist_text);
    val lengthTextView = itemView.findViewById<TextView>(R.id.length_text);
    val bitrateTextView = itemView.findViewById<TextView>(R.id.bitrate_text);

    private lateinit var model: SongModel

    fun bindData(viewModel: SongModel) {
        model = viewModel

        val seconds = model.lengthMS / 1000
        val minutes = seconds / 60
        val length = "$minutes:${seconds % 60}"
        titleTextView.text = model.title
        artistTextView.text = model.artist
        lengthTextView.text = length
        bitrateTextView.text = "${model.bitrate} kbps"
    }

    fun getTitleView() : TextView {
        return titleTextView
    }
}

class SongAdapter(val models: List<SongModel>, private val act: MainActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return SongViewHolder(view);
    }
    override fun onBindViewHolder( holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SongViewHolder).bindData(models[position])

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                act.onItemClick(v, position)
            }
        })

        holder.itemView.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View) : Boolean {
                return act.onItemLongClick(v, position)
            }
        })
    }
    override fun getItemCount() : Int {
        return models.size;
    }
    override fun getItemViewType(position: Int) : Int {
        return R.layout.item_simple_itemview;
    }
}

class MainActivity : AppCompatActivity(), MediaController.MediaPlayerControl {

    fun onItemClick(v: View, position: Int) {
        Log.e("click", adapter.models[position].title)
        musicSrv.setSong(adapter.models[position]);
        musicSrv.playSong();

        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0)
    }

    fun onItemLongClick(v: View, position: Int): Boolean{
        Log.e("long click", adapter.models[position].title)
        v.findViewById<TextView>(R.id.title_text).isSelected = true
        return true
    }

    private lateinit var musicSrv: MusicService;
    private lateinit var playIntent: Intent;
    private var musicBound: Boolean = false;

    private lateinit var songModels : List<SongModel>
    private lateinit var adapter : SongAdapter
    private lateinit var songList : RecyclerView

    private lateinit var controller: MusicController

    private var paused = false
    private var playbackPaused = false

    //connect to the service
    private var musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder;
            //get service
            musicSrv = binder.service
            musicBound = true;
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false;
        }
    };

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

                Log.d("tag", "denied")
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Log.d("tag", "granted")

        }

        val path = getExternalCardDirectory().toString() + separator + "Music"//getExternalStorageDirectory().toString() //getExternalFilesDir(null).toString()

        songModels = generateSongModelList(path)
        adapter = SongAdapter(songModels, this)
        songList = findViewById<RecyclerView>(R.id.songList)
        songList.setHasFixedSize(true)
        songList.layoutManager = LinearLayoutManager(this)
        songList.adapter = adapter

        setController();

        val songScroll = findViewById<MyScrollBar>(R.id.songScroll)
        songScroll.onChanged = {
            songList.scrollToPosition((adapter.itemCount.toFloat() * it).toInt())

            Log.e("t", it.toString())
        }

//        songList.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
//            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent){
////                super.
//                        return;
//            }
//        })

        songList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val percent = recyclerView.computeVerticalScrollOffset().toFloat() / (recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()).toFloat()
                songScroll.setPercent(percent)
            }
        })

    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getCurrentPosition(): Int {
         if (musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn()
        } else {
            return 0
        }
    }

    override fun getDuration(): Int {
        if (musicBound && musicSrv.isPng()) {
            return musicSrv.getDur()
        } else {
            return 0
        }
    }

    override fun isPlaying(): Boolean {
        return (musicBound && musicSrv.isPng())
    }

    override fun pause() {
        playbackPaused = true
        musicSrv.pausePlayer()
    }

    override fun seekTo(pos: Int) {
        musicSrv.seek(pos)
    }

    override fun start() {
        musicSrv.go()
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false;
        }
    }

    override fun onStop() {
        controller.hide()
        super.onStop()
    }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return super.onOptionsItemSelected(item)
//    }

    override fun getBufferPercentage(): Int {
        return 100 // TODO
    }

    override fun getAudioSessionId(): Int {
        return 1 // TODO
    }

    private fun setController() {
        controller = MusicController(this);
        controller.setPrevNextListeners(object : View.OnClickListener {
            override fun onClick(v: View) {
                playNext();
            }
        }, object : View.OnClickListener {
            override fun onClick(v: View) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById<RecyclerView>(R.id.songList));
        controller.setEnabled(true);
    }

    fun playNext() {

        if( playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    fun playPrev() {

        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    override fun onStart() {
        super.onStart();
        Log.e("start", "start")
        playIntent = Intent(this, MusicService::class.java)
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    override fun onDestroy() {
        Log.e("destroy", "destroy")
        stopService(playIntent);
//        musicSrv=null;
        super.onDestroy();
    }

    private fun generateSongModelList(path: String) : List<SongModel> {
        val songModelList =  ArrayList<SongModel>();

        val files = DirFolder(path)

        for (file in files) {
            // TODO process the file
            val fileName = file.nameWithoutExtension

            val _re_song_info = """(.*?) -(.*)""".toRegex()
            val _re_song_info_2 = """(.*?)-(.*)""".toRegex()
            val match = _re_song_info.matchEntire(fileName) ?: _re_song_info_2.matchEntire(fileName)
            val m : Any? = null;
            val title : String = (match?.groupValues?.get(2) ?: fileName).trim()
            val artist : String = (match?.groupValues?.get(1) ?: "Unknown").trim()

            val lengthMS = (0.. 10 * 60 * 1000).random()

            songModelList.add(SongModel(title, artist, file.path, lengthMS, 128));
        }

        return songModelList;
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("tag", "got access")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("tag", "got denied")

                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    fun DirFolder(path: String): ArrayList<File> {
        val ADir = ArrayList<File>()
        for (inFile in File(path).listFiles()!!) {
            if (inFile.isFile) {
                ADir.add(inFile)
            }
        }
        return ADir
    }

    fun log(s: Any?) {
        Log.d("tag", s.toString())
//        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun getExternalCardDirectory(): File? {
        val storageManager = getSystemService(Context.STORAGE_SERVICE)
        try {
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isRemovable = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(storageManager) as Array<StorageVolume>
            result.forEach {
                if (isRemovable.invoke(it) as Boolean) {
                    return File(getPath.invoke(it) as String)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
//        val editText = findViewById<EditText>(R.id.editText)
//        val message = editText.text.toString()
//        val intent = Intent(this, ScrollingActivity::class.java).apply {
//            putExtra(EXTRA_MESSAGE, message)
//        }
        startActivity(intent)
    }


}
