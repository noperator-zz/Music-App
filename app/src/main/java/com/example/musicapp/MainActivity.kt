package com.example.musicapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.nio.file.FileSystem
import android.R.attr.start
import java.io.File.separator
import android.R.attr.path
import android.content.Context
import android.media.MediaPlayer
import java.io.File
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Environment.getExternalStorageDirectory
import android.os.storage.StorageVolume
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import java.io.FileFilter
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

const val EXTRA_MESSAGE = "com.example.musicapp.MESSAGE"
const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

data class Item(val id: Long, val title: String, val url: String)

class SimpleViewModel(simpleText: String) {
    var simpleText = simpleText;


//    fun getSimpleText() : String {
//        return simpleText;
//    }
//
//    fun setSimpleText(simpleText: String) : Unit {
//        this.simpleText = simpleText;
//    }

}

class SimpleViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    var simpleTextView = itemView.findViewById<TextView>(R.id.simple_text);

    fun bindData(viewModel: SimpleViewModel) {
        simpleTextView.text = viewModel.simpleText
    }
}

class SimpleAdapter(private val models: List<SimpleViewModel>) : RecyclerView.Adapter<SimpleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return SimpleViewHolder(view);
    }
    override fun onBindViewHolder( holder: SimpleViewHolder, position: Int) {
        (holder as SimpleViewHolder).bindData(models[position])
    }
    override fun getItemCount() : Int {
        return models.size;
    }
    override fun getItemViewType(position: Int) : Int {
        return R.layout.item_simple_itemview;
    }
}

class MainActivity : AppCompatActivity() {

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


        val files = DirFolder(path)

        var adapter = SimpleAdapter(generateViewList(files))
        val songList = findViewById<RecyclerView>(R.id.songList)
        songList.setHasFixedSize(true)
        songList.layoutManager = LinearLayoutManager(this)
        songList.adapter = adapter

        songList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.e("DY", "" + dy)
//                    if (dy > 0) {
//                        mFloatingActionMenu.hideMenuButton(true)
//                    } else {
//                        mFloatingActionMenu.showMenuButton(true)
//                    }
            }
        })


        val mp = MediaPlayer()
        try {
            mp.setDataSource(files[0])
            mp.prepare()
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun generateViewList(files : ArrayList<String>) : List<SimpleViewModel> {
        val simpleViewModelList =  ArrayList<SimpleViewModel>();

        for (file in files) {
            simpleViewModelList.add(SimpleViewModel(String.format(Locale.US, file)));
        }

        return simpleViewModelList;
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


    fun DirFolder(path: String): ArrayList<String> {
        val ADir = ArrayList<String>()
        for (inFile in File(path).listFiles()!!) {
            if (inFile.isFile) {
                ADir.add(inFile.toString())
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
        val editText = findViewById<EditText>(R.id.editText)
        val message = editText.text.toString()
        val intent = Intent(this, ScrollingActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }


}
