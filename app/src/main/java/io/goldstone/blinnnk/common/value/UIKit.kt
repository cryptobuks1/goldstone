package io.goldstone.blinnnk.common.value

import android.content.res.Resources
import android.graphics.Color
import android.view.View
import com.blinnnk.uikit.uiPX
import org.jetbrains.anko.px2sp

/**
 * @date 21/03/2018 7:30 PM
 * @author KaySaith
 * @description
 * Common interface parameters, including color, original size, etc.
 */
object GrayScale {
	@JvmField
	val black = Color.parseColor("#FF000000")
	@JvmField
	val lightGray = Color.parseColor("#FFE5E5E5")
	@JvmField
	val whiteGray = Color.parseColor("#FFF5F5F5")
	@JvmField
	val gray = Color.parseColor("#FFAAAAAA")
	@JvmField
	val midGray = Color.parseColor("#FFCCCCCC")
	@JvmField
	val Opacity1Black = Color.parseColor("#1A000000")
	@JvmField
	val Opacity2Black = Color.parseColor("#33000000")
	@JvmField
	val Opacity5Black = Color.parseColor("#80000000")
	@JvmField
	val Opacity8Black = Color.parseColor("#CC000000")
}

object Spectrum {
	@JvmField
	val white = Color.parseColor("#FFFFFFFF")
	@JvmField
	val blue = Color.parseColor("#FF235682")
	@JvmField
	val backgroundBlue = Color.parseColor("#FF1c4f7b")
	@JvmField
	val deepBlue = Color.parseColor("#FF17446B")
	@JvmField
	val blackBlue = Color.parseColor("#FF084A65")
	@JvmField
	val lightBlue = Color.parseColor("#FF2882D2")
	@JvmField
	val grayBlue = Color.parseColor("#FFA0BBD3")
	@JvmField
	val green = Color.parseColor("#FF1CC881")
	@JvmField
	val darkBlue = Color.parseColor("#FF0863B8")
	@JvmField
	val red = Color.parseColor("#FFFA0D0D")
	@JvmField
	val lightRed = Color.parseColor("#FFFF6464")
	@JvmField
	val yellow = Color.parseColor("#FFFFF988")
	@JvmField
	val darkYellow = Color.parseColor("#FFF3EA3C")
	@JvmField
	val opacity1White = Color.parseColor("#1AFFFFFF")
	@JvmField
	val opacity3White = Color.parseColor("#4DFFFFFF")
	@JvmField
	val opacity2White = Color.parseColor("#33FFFFFF")
	@JvmField
	val opacity5White = Color.parseColor("#80FFFFFF")
}

object WalletColor {
	private val purple = Color.parseColor("#FF3F4E92")
	private val blue = Color.parseColor("#FF2A7EDA")
	private val cyan = Color.parseColor("#FF1BA2A9")
	private val darkPurple = Color.parseColor("#FF603361")
	private val grayYellow = Color.parseColor("#FF717335")
	private val blueGray = Color.parseColor("#FF4B5C6E")
	fun getAll(): List<Int> = listOf(purple, blue, cyan, darkPurple, grayYellow, blueGray)
}

object SocialMediaColor {
	@JvmField
	val facebook = Color.parseColor("#FF3F5A93")
	@JvmField
	val twitter = Color.parseColor("#FF67ABE8")
	@JvmField
	val reddit = Color.parseColor("#FFE16238")
	@JvmField
	val telegram = Color.parseColor("#FF419ACB")
}

object ShadowSize {
	@JvmField
	val Header = 3.uiPX().toFloat()
	@JvmField
	val Card = 2.uiPX().toFloat()
	@JvmField
	val Cell = 4.uiPX().toFloat()
	@JvmField
	val Overlay = 15.uiPX().toFloat()
	@JvmField
	val default = 10.uiPX().toFloat()
}

object PaddingSize {
	@JvmField
	val device = 10.uiPX()
	@JvmField
	val gsCard = 5.uiPX()
	@JvmField
	val content = 15.uiPX()
	@JvmField
	val overlay = 15.uiPX()
	@JvmField
	val card = 30.uiPX()
}

object CornerSize {
	@JvmField
	val small = 3.uiPX().toFloat()
	@JvmField
	val normal = 8.uiPX().toFloat()
	@JvmField
	val default = 10.uiPX().toFloat()
}

object BorderSize {
	@JvmField
	val default = 1.uiPX().toFloat()
	@JvmField
	val bold = 2.uiPX().toFloat()
	@JvmField
	val crude = 3.uiPX().toFloat()
}

object ScreenSize {
	@JvmStatic
	val card = Resources.getSystem().displayMetrics.widthPixels - PaddingSize.gsCard * 2
	@JvmStatic
	val widthWithPadding = Resources.getSystem().displayMetrics.widthPixels - PaddingSize.device * 2
	@JvmStatic
	val overlayContentWidth = Resources.getSystem().displayMetrics.widthPixels - 50.uiPX()
	@JvmStatic
	val fullHeight = Resources.getSystem().displayMetrics.heightPixels
	@JvmStatic
	var heightWithOutHeader = fullHeight - HomeSize.headerHeight
}

object CommonCellSize {
	@JvmField
	val rightPadding = 30.uiPX()
	@JvmField
	val iconPadding = 40.uiPX()
}

object TransactionSize {
	@JvmField
	val headerView = 240.uiPX()
}

object HomeSize {
	@JvmField
	val tabBarHeight = 50.uiPX()
	@JvmField
	val sliderHeaderHeight = 65.uiPX()
	@JvmField
	val headerHeight = 65.uiPX()
	@JvmField
	val menuHeight = 45.uiPX()
}

object AvatarSize {
	@JvmField
	val big = 75.uiPX()
	@JvmField
	val middle = 60.uiPX()
}

object TokenDetailSize {
	@JvmField
	val headerHeight = 240.uiPX()
}

object WalletDetailSize {
	@JvmField
	val headerHeight = 310.uiPX()
}

fun View.fontSize(defaultSize: Int): Float {
	return px2sp((Resources.getSystem().displayMetrics.density * defaultSize).toInt())
}
