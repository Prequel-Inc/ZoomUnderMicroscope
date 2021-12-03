package ru.demin.zoomundermicroscope

import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val exoPlayer by lazy { ExoPlayer.Builder(this).build() }
    private val originContentRect by lazy {
        player_view.run {
            val array = IntArray(2)
            getLocationOnScreen(array)
            Rect(array[0], array[1], array[0] + width, array[1] + height)
        }
    }

    private val translationHandler by lazy {
        object : View.OnTouchListener {
            private var prevX = 0f
            private var prevY = 0f
            private var moveStarted = false
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null || (player_view?.scaleX ?: 1f) == 1f) return false

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        prevX = event.x
                        prevY = event.y
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
                        if (event.actionIndex == 0) {
                            try {
                                prevX = event.getX(1)
                                prevY = event.getY(1)
                            } catch (e: Exception) {
                            }
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (event.pointerCount > 1) {
                            prevX = event.x
                            prevY = event.y
                            return false
                        }
                        moveStarted = true
                        player_view?.run {
                            translationX += (event.x - prevX)
                            translationY += (event.y - prevY)
                        }
                        prevX = event.x
                        prevY = event.y
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!moveStarted) return false
                        reset()
                        translateToOriginalRect()
                    }
                }
                return true
            }

            private fun reset() {
                prevX = 0f
                prevY = 0f
                moveStarted = false
            }
        }
    }

    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(this, object : ScaleGestureDetector.OnScaleGestureListener {
            var totalScale = 1f

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                player_view.run {
                    val actualPivot = PointF(
                        (detector.focusX - translationX + pivotX * (totalScale - 1)) / totalScale,
                        (detector.focusY - translationY + pivotY * (totalScale - 1)) / totalScale,
                    )

                    translationX -= (pivotX - actualPivot.x) * (totalScale - 1)
                    translationY -= (pivotY - actualPivot.y) * (totalScale - 1)
                    setPivot(actualPivot)
                }
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                totalScale *= detector.scaleFactor
                totalScale = totalScale.coerceIn(MIN_SCALE_FACTOR, MAX_SCALE_FACTOR)
                player_view.scale(totalScale)
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) = Unit
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPlayer()
        player_view.doOnLayout { originContentRect }
        view_touch_handler.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            translationHandler.onTouch(view, event)
            true
        }
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

    private fun translateToOriginalRect() {
        getContentViewTranslation()?.takeIf { it != PointF(0f, 0f) }?.let { translation ->
            player_view?.let { view ->
                view.animateWithDetach()
                    .translationXBy(translation.x)
                    .translationYBy(translation.y)
                    .apply { duration = CORRECT_LOCATION_ANIMATION_DURATION }
                    .start()
            }
        }
    }

    private fun getContentViewTranslation(): PointF? {
        return player_view?.run {
            originContentRect.let { rect ->
                val array = IntArray(2)
                getLocationOnScreen(array)
                PointF(
                    when {
                        array[0] > rect.left -> rect.left - array[0].toFloat()
                        array[0] + width * scaleX < rect.right -> rect.right - (array[0] + width * scaleX)
                        else -> 0f
                    },
                    when {
                        array[1] > rect.top -> rect.top - array[1].toFloat()
                        array[1] + height * scaleY < rect.bottom -> rect.bottom - (array[1] + height * scaleY)
                        else -> 0f
                    }
                )
            }
        }
    }

    companion object {
        private val VIDEO_URI = Uri.parse("asset:///zoomable.mp4")
        private const val MAX_SCALE_FACTOR = 5f
        private const val MIN_SCALE_FACTOR = 1f
        private const val CORRECT_LOCATION_ANIMATION_DURATION = 300L
    }
}