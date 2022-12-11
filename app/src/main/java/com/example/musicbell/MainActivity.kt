package com.example.musicbell

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.musicbell.ui.theme.MusicBellTheme
import java.io.File
import java.io.FilenameFilter

class MainActivity : ComponentActivity() {
    val musicFiles = mutableListOf<File>()
    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val playMode = PlayMode.Single
    var videoIndex = 0
    var videoPrevIndex = 0
    var prevY = 0f
    var minMoveY = 10f

    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val readPermission = ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED
        if (readPermission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                requestPermissions(permissions, 1)
            }
            else
            {
                requestPermissions(permissions, 1)
            }
        }
        // 读取Music文件夹
        val dir = Environment.getExternalStorageDirectory().absolutePath
        val musicFolder = File("${dir}/Music")
        if (musicFolder.exists()) {
            val files = musicFolder.listFiles(MusicFilenameFilter())
            for (file in files!!) {
                musicFiles.add(file)
            }
        }

        // 初始化播放器
        val player = findViewById<VideoView>(R.id.player)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(player)
        player.setMediaController(mediaController)
        player.setOnPreparedListener {

        }
        player.setOnCompletionListener {
            when(playMode) {
                PlayMode.Single -> {
                    player.start()
                }
                PlayMode.Random -> {
                    playNext()
                }
            }
        }
        // 控制按钮
        val buttonAddAlarm = findViewById<ImageView>(R.id.add_alarm)
        val textMusicName = findViewById<TextView>(R.id.text_music_name)
        val buttonMusicControl = findViewById<ImageView>(R.id.button_music_control)
        val buttonMusicList = findViewById<ImageView>(R.id.button_music_list)
        val viewAlarms = findViewById<RelativeLayout>(R.id.view_alarms)
        val viewPlayer = findViewById<RelativeLayout>(R.id.view_player)
        val viewList = findViewById<RelativeLayout>(R.id.view_music_list)

        buttonMusicControl.setOnClickListener{
            if (player.isPlaying) {
                player.pause()
            }
            else {
                player.resume()
            }
        }

        buttonAddAlarm.setOnClickListener {
            viewAlarms.isVisible = true
        }

        textMusicName.setOnClickListener {
            viewPlayer.isVisible = true
            play()
        }

        buttonMusicList.setOnClickListener {
            viewList.isVisible = true
        }

        viewPlayer.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN ->
                {
                    prevY = event.y
                    true
                }
                MotionEvent.ACTION_MOVE ->
                {
                    true
                }
                MotionEvent.ACTION_UP ->
                {
                    val distance = Math.abs(prevY - event.y)
                    if (distance > minMoveY) {
                        if(prevY > event.y){
                            // 下滑
                            playNext()
                        }
                        else if (prevY < event.y){
                            // 上滑
                            playPrev()
                        }
                    }
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    private fun playNext(){
        var maxIndex = musicFiles.size - 1
        videoPrevIndex = videoIndex
        videoIndex = Utility.GetRandomIndex(maxIndex)
        if (videoIndex > maxIndex){
            videoIndex = 0
        }
        play()
    }

    private fun playPrev(){
        if (videoIndex == videoPrevIndex)
            return
        videoIndex = videoPrevIndex
        play()
    }

    private fun play(){
        val player = findViewById<VideoView>(R.id.player)
        player.stopPlayback()
        player.setVideoPath(musicFiles[videoIndex].path)
        player.start()
    }

    override fun onStop(){
        super.onStop()
        play()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MusicBellTheme {
        Greeting("Android")
    }
}

class MusicFilenameFilter(): FilenameFilter {
    override fun accept(dir: File ,name: String) : Boolean {
        val exts = listOf(".mp4", ".mp3", ".avi")
        for (ext in exts) {
            if (name.endsWith(ext, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}