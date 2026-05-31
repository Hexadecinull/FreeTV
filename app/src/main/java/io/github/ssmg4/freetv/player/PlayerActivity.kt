package io.github.ssmg4.freetv.player

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import io.github.ssmg4.freetv.R
import io.github.ssmg4.freetv.R.id
import io.github.ssmg4.freetv.R.layout
import io.github.ssmg4.freetv.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    private lateinit var zoomButton: ImageButton
    private lateinit var pipButton: ImageButton
    private lateinit var screenRotateButton: ImageButton
    private lateinit var playNext: ImageButton
    private lateinit var playPrev: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var videoName: TextView
    private lateinit var unlockButton: ImageButton
    private lateinit var lockButton: ImageButton
    private lateinit var playerUnlockControls: FrameLayout
    private lateinit var playerLockControls: FrameLayout
    private lateinit var playbackSpeedButton: ImageButton
    private lateinit var audioTrackButton: ImageButton
    private lateinit var subtitleTrackButton: ImageButton
    private lateinit var castButton: MediaRouteButton

    private lateinit var gestureDetector: GestureDetector
    private lateinit var volumeProgressBar: ProgressBar
    private lateinit var volumeProgressText: TextView
    private lateinit var brightnessProgressBar: ProgressBar
    private lateinit var brightnessProgressText: TextView
    private val volumeHideHandler = Handler(Looper.getMainLooper())
    private val brightnessHideHandler = Handler(Looper.getMainLooper())

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var localPlayer: ExoPlayer
    private var castPlayer: CastPlayer? = null
    private var activePlayer: Player? = null
    private var currentZoom: VideoZoom = VideoZoom.BEST_FIT
    private var link: String? = ""
    private var channelName: String? = ""
    private var retryCount = 0

    @SuppressLint("UnsafeOptInUsageError")
    private lateinit var trackSelector: DefaultTrackSelector

    private val isPipSupported: Boolean by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channelName = intent.getStringExtra("Name")
        link = intent.getStringExtra("Url")

        // --- Local ExoPlayer ---
        trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setPreferredAudioLanguage("en"))
        }
        localPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()

        // --- Cast setup ---
        try {
            val castContext = CastContext.getSharedInstance(this)
            castPlayer = CastPlayer(castContext).also { cp ->
                cp.setSessionAvailabilityListener(object : SessionAvailabilityListener {
                    override fun onCastSessionAvailable() = switchToPlayer(cp)
                    override fun onCastSessionUnavailable() = switchToPlayer(localPlayer)
                })
            }
            if (castPlayer?.isCastSessionAvailable == true) {
                switchToPlayer(castPlayer!!)
            } else {
                switchToPlayer(localPlayer)
            }
        } catch (e: Exception) {
            // Cast not available (no Play Services etc.)
            switchToPlayer(localPlayer)
        }

        // --- UI binding ---
        volumeProgressBar = binding.root.findViewById(id.volume_progress_bar)
        volumeProgressText = binding.root.findViewById(id.volume_progress_text)
        brightnessProgressBar = binding.root.findViewById(id.brightness_progress_bar)
        brightnessProgressText = binding.root.findViewById(id.brightness_progress_text)
        setupGestureDetector()

        zoomButton = binding.playerView.findViewById(id.btn_video_zoom)
        pipButton = binding.playerView.findViewById(id.btn_pip)
        screenRotateButton = binding.playerView.findViewById(id.screen_rotate)
        playNext = binding.playerView.findViewById(id.btn_play_next)
        playPrev = binding.playerView.findViewById(id.btn_play_prev)
        backButton = binding.playerView.findViewById(id.back_button)
        videoName = binding.playerView.findViewById(id.video_name)
        unlockButton = binding.playerView.findViewById(id.btn_unlock_controls)
        lockButton = binding.playerView.findViewById(id.btn_lock_controls)
        playerUnlockControls = binding.playerView.findViewById(id.player_unlock_controls)
        playerLockControls = binding.playerView.findViewById(id.player_lock_controls)
        playbackSpeedButton = binding.playerView.findViewById(id.btn_playback_speed)
        audioTrackButton = binding.playerView.findViewById(id.btn_audio_track)
        subtitleTrackButton = binding.playerView.findViewById(id.btn_subtitle_track)
        castButton = binding.playerView.findViewById(id.btn_cast)

        CastButtonFactory.setUpMediaRouteButton(applicationContext, castButton)

        // --- System UI ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        if (!isPipSupported) pipButton.visibility = View.GONE
        videoName.text = channelName

        // --- Button listeners ---
        lockButton.setOnClickListener {
            playerUnlockControls.visibility = View.INVISIBLE
            playerLockControls.visibility = View.VISIBLE
        }
        unlockButton.setOnClickListener {
            playerUnlockControls.visibility = View.VISIBLE
            playerLockControls.visibility = View.INVISIBLE
        }
        backButton.setOnClickListener {
            activePlayer?.pause()
            onBackPressedDispatcher.onBackPressed()
        }
        playNext.setOnClickListener { activePlayer?.seekTo((activePlayer?.currentPosition ?: 0) + 5000) }
        playPrev.setOnClickListener { activePlayer?.seekTo(((activePlayer?.currentPosition ?: 0) - 5000).coerceAtLeast(0)) }
        zoomButton.setOnClickListener { toggleVideoZoom() }
        pipButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isPipSupported) {
                enterPictureInPictureMode(updatePictureInPictureParams())
            }
        }
        screenRotateButton.setOnClickListener {
            requestedOrientation = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
        playbackSpeedButton.setOnClickListener {
            PlaybackSpeedControlsDialogFragment(
                currentSpeed = localPlayer.playbackParameters.speed,
                onChange = { localPlayer.setPlaybackSpeed(it) },
            ).show(supportFragmentManager, "PlaybackSpeedSelectionDialog")
        }
        audioTrackButton.setOnClickListener {
            TrackSelectionDialogFragment(
                type = C.TRACK_TYPE_AUDIO,
                tracks = localPlayer.currentTracks,
                onTrackSelected = { localPlayer.switchTrack(C.TRACK_TYPE_AUDIO, it) },
            ).show(supportFragmentManager, "TrackSelectionDialog")
        }
        subtitleTrackButton.setOnClickListener {
            TrackSelectionDialogFragment(
                type = C.TRACK_TYPE_TEXT,
                tracks = localPlayer.currentTracks,
                onTrackSelected = { localPlayer.switchTrack(C.TRACK_TYPE_TEXT, it) },
            ).show(supportFragmentManager, "TrackSelectionDialog")
        }
    }

    private fun switchToPlayer(player: Player) {
        val previousPlayer = activePlayer
        if (previousPlayer == player) return

        val position = previousPlayer?.currentPosition ?: 0L
        previousPlayer?.stop()

        activePlayer = player
        binding.playerView.player = player

        // CastPlayer requires mimeType — infer from URL, default to HLS
        val mimeType = when {
            link!!.contains(".mpd", ignoreCase = true) -> "application/dash+xml"
            link!!.contains(".mp4", ignoreCase = true) -> "video/mp4"
            else -> "application/x-mpegURL" // HLS — covers .m3u8 and plain stream URLs
        }
        val mediaItem = MediaItem.Builder()
            .setUri(link!!)
            .setMimeType(mimeType)
            .build()
        player.setMediaItem(mediaItem)
        player.seekTo(position)
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (retryCount < 3) {
                    retryCount++
                    Handler(Looper.getMainLooper()).postDelayed({
                        player.prepare()
                        player.play()
                    }, 1500L * retryCount)
                } else {
                    retryCount = 0
                    Toast.makeText(
                        this@PlayerActivity,
                        getString(R.string.NotStream),
                        Toast.LENGTH_LONG
                    ).show()
                    (this@PlayerActivity as? Activity)?.finish()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) retryCount = 0
            }
        })
    }

    private val subtitleFileLauncher = registerForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun Player.switchTrack(trackType: @C.TrackType Int, trackIndex: Int?) {
        if (trackIndex == null) return
        if (trackIndex < 0) {
            trackSelectionParameters = trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(trackType, true).build()
        } else {
            val tracks = currentTracks.groups.filter { it.type == trackType }
            if (tracks.isEmpty() || trackIndex >= tracks.size) return
            trackSelectionParameters = trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(trackType, false)
                .setOverrideForType(TrackSelectionOverride(tracks[trackIndex].mediaTrackGroup, 0))
                .build()
        }
    }

    private fun toggleVideoZoom() {
        currentZoom = when (currentZoom) {
            VideoZoom.BEST_FIT -> VideoZoom.STRETCH
            VideoZoom.STRETCH -> VideoZoom.CROP
            VideoZoom.CROP -> VideoZoom.HUNDRED_PERCENT
            VideoZoom.HUNDRED_PERCENT -> VideoZoom.BEST_FIT
        }
        applyVideoZoom(currentZoom)
    }

    private fun applyVideoZoom(zoom: VideoZoom) {
        val (mode, icon) = when (zoom) {
            VideoZoom.BEST_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT to R.drawable.ic_fit_screen
            VideoZoom.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL to R.drawable.ic_aspect_ratio
            VideoZoom.CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM to R.drawable.ic_crop_landscape
            VideoZoom.HUNDRED_PERCENT -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH to R.drawable.ic_width_wide
        }
        binding.playerView.resizeMode = mode
        zoomButton.setImageDrawable(ContextCompat.getDrawable(this, icon))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                e1?.let {
                    if (playerLockControls.visibility != View.VISIBLE) {
                        val half = resources.displayMetrics.widthPixels / 2
                        if (e1.x < half) adjustBrightness(-distanceY) else adjustVolume(-distanceY)
                    }
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (activePlayer?.isPlaying == true) activePlayer?.pause() else activePlayer?.play()
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (binding.playerView.isControllerFullyVisible) binding.playerView.hideController()
                else binding.playerView.showController()
                return super.onSingleTapConfirmed(e)
            }
        })
        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun adjustVolume(distanceY: Float) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val new = (cur - (distanceY * 0.05f).toInt()).coerceIn(0, max)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, new, AudioManager.FLAG_SHOW_UI)
        updateVolumeUI()
    }

    private fun adjustBrightness(distanceY: Float) {
        val lp = window.attributes
        lp.screenBrightness = (lp.screenBrightness - distanceY * 0.002f).coerceIn(0f, 1f)
        window.attributes = lp
        updateBrightnessUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateVolumeUI() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val pct = (am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 100).toInt()
        binding.volumeGestureLayout.visibility = View.VISIBLE
        volumeProgressText.text = "$pct%"
        volumeProgressBar.progress = pct
        volumeHideHandler.removeCallbacksAndMessages(null)
        volumeHideHandler.postDelayed({ binding.volumeGestureLayout.visibility = View.GONE }, 2000)
    }

    @SuppressLint("SetTextI18n")
    private fun updateBrightnessUI() {
        val level = (window.attributes.screenBrightness * 100).toInt()
        binding.brightnessGestureLayout.visibility = View.VISIBLE
        brightnessProgressText.text = "$level%"
        brightnessProgressBar.progress = level
        brightnessHideHandler.removeCallbacksAndMessages(null)
        brightnessHideHandler.postDelayed({ binding.brightnessGestureLayout.visibility = View.GONE }, 2000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePictureInPictureParams(): PictureInPictureParams =
        PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build().also {
            setPictureInPictureParams(it)
        }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        binding.playerView.useController = !isInPictureInPictureMode
    }

    override fun onStop() {
        super.onStop()
        if (!isInPictureInPictureMode) activePlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        localPlayer.release()
        castPlayer?.setSessionAvailabilityListener(null)
        castPlayer?.release()
    }
}
