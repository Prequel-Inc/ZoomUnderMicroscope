package ru.demin.zoomundermicroscope

import android.graphics.PointF
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.core.view.doOnDetach

fun View.scale(scale: Float) {
    scaleX = scale
    scaleY = scale
}

fun View.setPivot(point: PointF) {
    pivotX = point.x
    pivotY = point.y
}

fun View.animateWithDetach(): ViewPropertyAnimator {
    doOnDetach { it.animate().cancel() }
    return animate()
}