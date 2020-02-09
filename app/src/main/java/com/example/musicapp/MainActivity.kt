package com.example.musicapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PlaylistViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val titleTextView = itemView.findViewById<TextView>(R.id.title_text);

    private lateinit var model: PlaylistModel

    fun bindData(viewModel: PlaylistModel) {
        model = viewModel

        titleTextView.text = model.title
    }

//        fun getTitleView() : TextView {
//            return titleTextView
//        }
}

class PlaylistAdapter(val models: List<PlaylistModel>, private val act: MainActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return PlaylistViewHolder(view);
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PlaylistViewHolder).bindData(models[position])

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
        return R.layout.item_playlist_itemview;
    }
}


class MainActivity : AppCompatActivity() {//, MediaController.MediaPlayerControl {

    private fun log(text : String) {
        Log.e("MainActivity", text)
    }

    private lateinit var playlistModels : List<PlaylistModel>
    private lateinit var adapter : PlaylistAdapter
    private lateinit var playlistList : RecyclerView


    fun onItemClick(v: View, position: Int) {
        log("click" + adapter.models[position].title)

        val intent = Intent(this, ListActivity::class.java).apply {
            putExtra(EXTRA_PLAYLIST, adapter.models[position].title)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)

    }

    fun onItemLongClick(v: View, position: Int): Boolean{
        log("long click: " + adapter.models[position].title)
        v.findViewById<TextView>(R.id.title_text).isSelected = true
        return true
    }

    override fun onBackPressed() {
//        finish()
        val homeIntent = Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent)
//        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("create")
        setContentView(R.layout.activity_main)

        playlistModels = generatePlaylistModelList()
        adapter = PlaylistAdapter(playlistModels, this)
        playlistList = findViewById<RecyclerView>(R.id.playList)
        playlistList.setHasFixedSize(true)
        playlistList.layoutManager = LinearLayoutManager(this)
        playlistList.adapter = adapter

    }

    private fun generatePlaylistModelList() : List<PlaylistModel> {
        val playlistModelList =  ArrayList<PlaylistModel>();

        playlistModelList.add(PlaylistModel("Songs"));
        playlistModelList.add(PlaylistModel("Playlist 1"));
        playlistModelList.add(PlaylistModel("Playlist 2"));

        return playlistModelList;
    }

    override fun onStart() {
        super.onStart();
        log("start")
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
    }

    override fun onStop() {
        log("stop")
        super.onStop()
    }

    override fun onDestroy() {
        log("destroy")

        super.onDestroy();
    }

}
