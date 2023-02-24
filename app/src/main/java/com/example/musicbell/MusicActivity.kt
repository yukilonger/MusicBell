package com.example.musicbell

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FilenameFilter

class MusicActivity : ComponentActivity() {
    val musicFiles = mutableListOf<File>()
    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    var musicBinder: IBinder? = null
    var musicService: MusicService? = null
    var holder: SurfaceHolder? = null
    var videoIndex = 0
    var videoPrevIndex = 0
    var prevY = 0f
    var minMoveY = 10f
    val prevIndexes = mutableListOf<Int>() // 已播放歌曲的索引
    var currentPrevIndex = -1
    var isRandom = true // 播放下一曲为随机模式，播放上一曲后变为有序模式，直到返回到已播放歌曲索引的最后再播放下一曲时恢复随机模式
    var isInitialization: Boolean = true

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            musicBinder = service
            val localBinder = service as MusicService.MyBinder
            musicService = localBinder.getService()
            val html = findViewById<RelativeLayout>(R.id.view_player)
            holder?.let {
                musicService?.initialization(it, html.width, html.height) {
                    playNext()
                }
                musicService?.display(it)
                if(musicService?.isPlaying() == false){
                    playNext()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music)

        // 读取Music文件夹
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

        val dir = Environment.getExternalStorageDirectory().absolutePath
        val musicFolder = File("${dir}/Music")
        if (musicFolder.exists()) {
            val files = musicFolder.listFiles(MusicFilenameFilter())
            for (file in files!!) {
                musicFiles.add(file)
            }
        }

        // 视频播放
        val bindIntent = Intent(this, MusicService::class.java)
        val viewPlayer = findViewById<RelativeLayout>(R.id.view_player)
        val player = findViewById<SurfaceView>(R.id.player)
        holder = player.holder
        holder?.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(p0: SurfaceHolder) {
                musicService?.display(p0)
                if(musicService == null) {
                    startService(bindIntent)
                    bindService(bindIntent, connection, BIND_AUTO_CREATE)
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }

        })

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
                    v.performClick()
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

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        val data = Parcel.obtain()
        musicBinder!!.transact(MusicService.ServiceCode.Destroy.value, data, null, 0)
    }
    private fun playNext(){
        if(!isRandom && videoIndex == prevIndexes.lastIndex) {
            isRandom = true
        }

        if(isRandom) {
            // 随机模式
            val maxIndex = musicFiles.size - 1
            videoIndex = Utility.GetRandomIndex(maxIndex)
            if (videoIndex > maxIndex){
                videoIndex = 0
            }
            prevIndexes.add(videoIndex)
        }
        else {
            // 有序模式
            currentPrevIndex++
            videoIndex = prevIndexes[currentPrevIndex]
        }

        play()
    }

    private fun playPrev(){
        if(isRandom) {
            isRandom = false
            currentPrevIndex = prevIndexes.lastIndex
        }
        else{
            currentPrevIndex--
        }

        if(currentPrevIndex < 0)
            return

        videoIndex = prevIndexes[currentPrevIndex]

        if (videoIndex == videoPrevIndex)
            return

        play()
    }

    private fun play(){
        //发送的数据
        val data = Parcel.obtain()
        //返回的数据
        val reply = Parcel.obtain()

        try {
            data.writeInterfaceToken("MusicService")
            data.writeString(musicFiles[videoIndex].path)
            musicBinder!!.transact(MusicService.ServiceCode.SetPath.value, data, null, 0)
            musicBinder!!.transact(MusicService.ServiceCode.Play.value, data, null, 0)
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    class MusicFilenameFilter: FilenameFilter {
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
}