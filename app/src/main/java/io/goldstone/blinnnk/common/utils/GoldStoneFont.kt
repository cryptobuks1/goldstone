package io.goldstone.blinnnk.common.utils

import android.content.Context
import android.graphics.Typeface

/**
 * @date 21/03/2018 11:37 PM
 * @author KaySaith
 */

object GoldStoneFont {
	val light: Context.() -> Typeface = { Typeface.createFromAsset(assets, "font/light.ttf") }
	val book: Context.() -> Typeface = { Typeface.createFromAsset(assets, "font/book.ttf") }
	val medium: Context.() -> Typeface = { Typeface.createFromAsset(assets, "font/medium.ttf") }
	val heavy: Context.() -> Typeface = { Typeface.createFromAsset(assets, "font/heavy.ttf") }
	val black: Context.() -> Typeface = { Typeface.createFromAsset(assets, "font/black.ttf") }
}