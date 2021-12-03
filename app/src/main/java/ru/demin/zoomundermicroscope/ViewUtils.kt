package ru.demin.zoomundermicroscope

import android.graphics.PointF
import android.view.View

fun View.scale(scale: Float) {
    scaleX = scale
    scaleY = scale
}

fun View.setPivot(point: PointF) {
    pivotX = point.x
    pivotY = point.y
}