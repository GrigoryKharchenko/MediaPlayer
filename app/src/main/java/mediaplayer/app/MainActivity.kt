package mediaplayer.app

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import mediaplayer.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val songs = Songs()
    private var currentSongIndex = FIRST_SONG_INDEX

    private var mediaPlayerService: MediaPlayerService? = null

    private val serviceConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
                if (binder is MediaPlayerService.MediaPlayerBinder) {
                    mediaPlayerService = binder.getService()
                    initUI()
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                TODO("Not yet implemented")
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            startService(intent)
        }

    private var intent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent = Intent(this, MediaPlayerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission { startService(intent) }
        } else {
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        intent?.let {
            bindService(it, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission(action: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            action.invoke()
        } else {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun initUI() {
        mediaPlayerService?.let { service ->
            onStopMusic(service)
            onStartMusic(service)
            onNextMusic(service)
            onPreviousMusic(service)
            handleStartStop(service)
        }
        binding.ivAlbumCover.setImageResource(songs.songs[currentSongIndex].albumCover)
        binding.tvNameGroup.text = songs.songs[currentSongIndex].nameGroup
        binding.tvNameSong.text = songs.songs[currentSongIndex].nameSong
    }

    private fun onStartMusic(service: MediaPlayerService) {
        binding.ibtnPlay.setOnClickListener {
            service.playSong()
            handleStartStop(service)
        }
    }

    private fun onStopMusic(service: MediaPlayerService) {
        binding.ibtnPause.setOnClickListener {
            service.stopSong()
            handleStartStop(service)
        }
    }

    private fun onNextMusic(service: MediaPlayerService) {
        with(binding) {
            ibtnNext.setOnClickListener {
                if (currentSongIndex + INDEX_MOVE_STEP > songs.songs.lastIndex) {
                    currentSongIndex = FIRST_SONG_INDEX
                    ivAlbumCover.setImageResource(songs.songs[currentSongIndex].albumCover)
                    tvNameGroup.text = songs.songs[currentSongIndex].nameGroup
                    tvNameSong.text = songs.songs[currentSongIndex].nameSong
                } else {
                    currentSongIndex++
                    ivAlbumCover.setImageResource(songs.songs[currentSongIndex].albumCover)
                    tvNameGroup.text = songs.songs[currentSongIndex].nameGroup
                    tvNameSong.text = songs.songs[currentSongIndex].nameSong
                }
                service.goToNextSong()
                handleStartStop(service)
            }
        }
    }

    private fun onPreviousMusic(service: MediaPlayerService) {
        with(binding) {
            ibtnPrevious.setOnClickListener {
                if (currentSongIndex - INDEX_MOVE_STEP < FIRST_SONG_INDEX) {
                    currentSongIndex = songs.songs.lastIndex
                    ivAlbumCover.setImageResource(songs.songs[currentSongIndex].albumCover)
                    tvNameGroup.text = songs.songs[currentSongIndex].nameGroup
                    tvNameSong.text = songs.songs[currentSongIndex].nameSong
                } else {
                    currentSongIndex--
                    ivAlbumCover.setImageResource(songs.songs[currentSongIndex].albumCover)
                    tvNameGroup.text = songs.songs[currentSongIndex].nameGroup
                    tvNameSong.text = songs.songs[currentSongIndex].nameSong
                }
                service.goToPreviousSong()
                handleStartStop(service)
            }
        }
    }

    private fun handleStartStop(service: MediaPlayerService) {
        with(binding) {
            if (service.isPlaying()) {
                ibtnPlay.visibility = View.INVISIBLE
                ibtnPause.visibility = View.VISIBLE
            } else {
                ibtnPlay.visibility = View.VISIBLE
                ibtnPause.visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        private const val INDEX_MOVE_STEP = 1
        private const val FIRST_SONG_INDEX = 0
    }
}
