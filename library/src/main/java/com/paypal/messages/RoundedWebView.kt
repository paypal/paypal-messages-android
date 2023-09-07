package com.paypal.messages

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.webkit.WebView

internal class RoundedWebView: WebView {
	private var radius: Int = 50
	private var path: Path = Path()

	constructor(context: Context): super(context)
	constructor(context: Context, attrs: AttributeSet): super(context, attrs)
	constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

	override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
		super.onSizeChanged(width, height, oldWidth, oldHeight)

		path.reset()
		val left = 0f
		val top = 0f
		val right = width.toFloat()
		val bottom = height.toFloat() + radius.toFloat()
		val rFloat = radius.toFloat()
		val direction = Path.Direction.CW

		path.addRoundRect(left, top, right, bottom, rFloat, rFloat, direction)
	}

	override fun onDraw(canvas: Canvas) {
		canvas.clipPath(path)

		super.onDraw(canvas)
	}
}
