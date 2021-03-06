package com.example.musicapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File.separator
import android.content.Context
import java.io.File
import android.os.storage.StorageVolume
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import kotlin.collections.ArrayList

const val EXTRA_PLAYLIST = "playlist"
const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

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

//    fun getTitleView() : TextView {
//        return titleTextView
//    }
}

class SongAdapter(val models: List<SongModel>, private val act: ListActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

class ListActivity : AppCompatActivity() {//, MediaController.MediaPlayerControl {

    private fun log(text : String) {
        Log.e("ListActivity", text)
    }

    fun onItemClick(v: View, position: Int) {
        log("click" + adapter.models[position].title)

        val intent = Intent(this, SongActivity::class.java).apply {
            putExtra(EXTRA_SONG_PATH, adapter.models[position].path)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)


//        controller.show(0)
    }

    fun onItemLongClick(v: View, position: Int): Boolean{
        log("long click: " + adapter.models[position].title)
        v.findViewById<TextView>(R.id.title_text).isSelected = true
        return true
    }

    private lateinit var songModels : List<SongModel>
    private lateinit var adapter : SongAdapter
    private lateinit var songList : RecyclerView

//    private lateinit var controller: MusicController

    override fun onBackPressed() {
//        finish()
        val intent = Intent(this, MainActivity::class.java).apply {
            //            setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
//        moveTaskToBack(true)
//        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("create")
        setContentView(R.layout.activity_list)

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            log("Permission denied")
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
            log("Permission granted")

        }

        val path = getExternalCardDirectory().toString() + separator + "Music"//getExternalStorageDirectory().toString() //getExternalFilesDir(null).toString()

        songModels = generateSongModelList(path)
        adapter = SongAdapter(songModels, this)
        songList = findViewById<RecyclerView>(R.id.playList)
        songList.setHasFixedSize(true)
        songList.layoutManager = LinearLayoutManager(this)
        songList.adapter = adapter

//        setController();

        val songScroll = findViewById<MyScrollBar>(R.id.songScroll)
        songScroll.onChanged = {
            songList.scrollToPosition((adapter.itemCount.toFloat() * it).toInt())

//            log("t " + it.toString())
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

    override fun onStart() {
        super.onStart();
        log("start")

        val intent = getIntent()
        val playlist = intent.getStringExtra(EXTRA_PLAYLIST)!!
        log(playlist)
//        val songInfo = findViewById<View>(R.id.songInfoMain)
//        val textview = songInfo.findViewById<TextView>(R.id.songInfoTitle)
//        textview.text = playlist
    }

    override fun onRestart() {
        super.onRestart();
        log("restart")
    }

    override fun onPause() {
        super.onPause()
        log("pause")
//        paused = true
    }

    override fun onResume() {
        super.onResume()
        log("resume")
//        if (paused) {
//            setController()
//            paused = false;
//        }
    }

    override fun onStop() {
//        controller.hide()
        log("stop")
        super.onStop()
    }

    override fun onDestroy() {
        log("destroy")

        super.onDestroy();
    }

//    override fun canPause(): Boolean {
//        return true
//    }
//
//    override fun canSeekBackward(): Boolean {
//        return true
//    }
//
//    override fun canSeekForward(): Boolean {
//        return true
//    }
//
//    override fun getCurrentPosition(): Int {
//         if (musicBound && musicSrv.isPng()) {
//            return musicSrv.getPosn()
//        } else {
//            return 0
//        }
//    }
//
//    override fun getDuration(): Int {
//        if (musicBound && musicSrv.isPng()) {
//            return musicSrv.getDur()
//        } else {
//            return 0
//        }
//    }
//
//    override fun isPlaying(): Boolean {
//        return (musicBound && musicSrv.isPng())
//    }
//
//    override fun pause() {
//        playbackPaused = true
//        musicSrv.pausePlayer()
//    }
//
//    override fun seekTo(pos: Int) {
//        musicSrv.seek(pos)
//    }
//
//    override fun start() {
//        musicSrv.go()
//    }


//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return super.onOptionsItemSelected(item)
//    }

//    override fun getBufferPercentage(): Int {
//        return 100 // TODO
//    }
//
//    override fun getAudioSessionId(): Int {
//        return 1 // TODO
//    }

//    private fun setController() {
//        controller = MusicController(this);
//        controller.setPrevNextListeners(object : View.OnClickListener {
//            override fun onClick(v: View) {
//                playNext();
//            }
//        }, object : View.OnClickListener {
//            override fun onClick(v: View) {
//                playPrev();
//            }
//        });
//        controller.setMediaPlayer(this);
//        controller.setAnchorView(findViewById<RecyclerView>(R.id.songList));
//        controller.setEnabled(true);
//    }
//
//    fun playNext() {
//
//        if( playbackPaused) {
//            setController();
//            playbackPaused = false;
//        }
//        controller.show(0);
//    }
//
//    fun playPrev() {
//
//        if (playbackPaused) {
//            setController();
//            playbackPaused = false;
//        }
//        controller.show(0);
//    }



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

//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
//                // If request is cancelled, the result arrays are empty.
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                    Log.d("tag", "got access")
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Log.d("tag", "got denied")
//
//                }
//                return
//            }
//
//            // Add other 'when' lines to check for other
//            // permissions this app might request.
//            else -> {
//                // Ignore all other requests.
//            }
//        }
//    }


    fun DirFolder(path: String): ArrayList<File> {
        val ADir = ArrayList<File>()
        for (inFile in File(path).listFiles()!!) {
            if (inFile.isFile) {
                ADir.add(inFile)
            }
        }
        return ADir
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
}
