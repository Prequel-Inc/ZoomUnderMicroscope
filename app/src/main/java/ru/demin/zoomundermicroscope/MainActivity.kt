package ru.demin.zoomundermicroscope

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ScaleGestureDetector
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val exoPlayer by lazy { ExoPlayer.Builder(this).build() }
    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(this, object : ScaleGestureDetector.OnScaleGestureListener {
            var totalScale = 1f

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                totalScale *= detector.scaleFactor
                Log.d("Povarity", "totalScale = $totalScale scaleFactor = ${detector.scaleFactor}")
                player_view.scale(totalScale)
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                player_view.pivotX = detector.focusX
                player_view.pivotY = detector.focusY
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) = Unit
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPlayer()
//        view_touch_handler.setOnTouchListener { _, event -> scaleGestureDetector.onTouchEvent(event) }
        player_view.setOnTouchListener { _, event -> scaleGestureDetector.onTouchEvent(event) }
    }

    private fun setupPlayer() {
        player_view.player = exoPlayer
        exoPlayer.run {
            addMediaItem(MediaItem.fromUri(VIDEO_URI))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
            play()
        }
    }

    companion object {
        private val VIDEO_URI = Uri.parse("asset:///zoomable.mp4")
    }
}