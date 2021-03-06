package io.goldstone.blinnnk.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.blinnnk.extension.isTrue
import com.blinnnk.extension.orFalse
import com.blinnnk.extension.otherwise
import com.blinnnk.extension.toMillisecond
import com.blinnnk.uikit.ScreenSize
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.TinyNumberUtils
import com.blinnnk.util.getSystemModel
import io.goldstone.blinnnk.GoldStoneApp
import io.goldstone.blinnnk.common.language.WalletText
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.value.DeviceName
import io.goldstone.blinnnk.common.value.Spectrum
import java.text.SimpleDateFormat


/**
 * @date 21/03/2018 9:07 PM
 * @author KaySaith
 * @rewriteDate 26/07/2018 5:47 PM
 * @reWriter wcx
 * @description 調整头像对应名称的顺序 , 删除generateAvatar方法
 */
object UIUtils {

	// easy to set gradient color
	fun setGradientColor(
		startColor: Int,
		endColor: Int,
		width: Float = ScreenSize.Width.toFloat(),
		height: Float = ScreenSize.Height.toFloat()
	) =
		LinearGradient(
			0f, 0f, width, height, startColor, endColor, Shader.TileMode.CLAMP
		)

	fun subtractThenHalf(
		first: Int,
		second: Int
	) = (first - second) / 2

	fun generateDefaultName(): String {
		val walletID =
			if (SharedWallet.getMaxWalletID() == 100) 1
			else SharedWallet.getMaxWalletID() + 1
		return "${WalletText.wallet} $walletID"
	}
}

object TimeUtils {

	// 将时间戳转化为界面显示的时间格式的工具
	fun formatDate(timeStamp: Long): String {
		val time = timeStamp.toMillisecond()
		return DateUtils.formatDateTime(
			GoldStoneApp.appContext, time, DateUtils.FORMAT_SHOW_YEAR
		) + " " + DateUtils.formatDateTime(
			GoldStoneApp.appContext, time, DateUtils.FORMAT_SHOW_TIME
		)
	}

	@SuppressLint("SimpleDateFormat")
		/**
		 * @date: 2018/8/22
		 * @author: yanglihai
		 * @description: 把日期转换成月日，例如 8/15
		 */
	fun formatMdDate(date: Long): String {
		val simpleDateFormat = SimpleDateFormat("M/d")
		return simpleDateFormat.format(java.util.Date(date))
	}

	@SuppressLint("SimpleDateFormat")
		/**
		 * @date: 2018/8/22
		 * @author: yanglihai
		 * @description: 把日期转换成日期+时间，例如8-25 12:00
		 */
	fun formatMdHmDate(date: Long): String {
		val simpleDateFormat = SimpleDateFormat("M-d HH:mm")
		return simpleDateFormat.format(java.util.Date(date))
	}
}

fun Long.toMillisecond(): Long {
	return toString().toMillisecond()
}

fun Activity.transparentStatus() {
	TinyNumberUtils.allFalse(
		try {
			packageManager.hasSystemFeature("com.oppo.feature.screen.heteromorphism")
		} catch (error: Exception) {
			Log.e("hasSystemFeature", error.message)
			false
		},
		hasNotchInScreen(),
		isTargetDevice(DeviceName.nokiaX6).orFalse(),
		isTargetDevice(DeviceName.xiaomi8).orFalse(),
		detectNotchScreenInAndroidP().orFalse()
	) isTrue {
		SharedWallet.updateNotchScreenStatus(false)
		setTransparentStatusBar()
	} otherwise {
		SharedWallet.updateNotchScreenStatus(true)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window.statusBarColor = Spectrum.deepBlue
		}
	}
}

@SuppressLint("ObsoleteSdkInt")
fun Activity.setTransparentStatusBar() {
	if (Build.VERSION.SDK_INT >= 19) {
		window.decorView.systemUiVisibility =
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	}
	if (Build.VERSION.SDK_INT >= 21) {
		window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
		window.statusBarColor = Color.TRANSPARENT
	}
}

fun isTargetDevice(name: String): Boolean? {
	return try {
		val deviceName = getSystemModel()
		name.contains(deviceName, true)
	} catch (error: Exception) {
		null
	}
}

private fun Activity.detectNotchScreenInAndroidP(): Boolean? {
	return if (Build.VERSION.SDK_INT >= 28) {
		try {
			View(this).rootWindowInsets.displayCutout?.safeInsetTop ?: 0 > 30.uiPX()
		} catch (error: Exception) {
			Log.e("detectNotch", error.message)
			null
		}
	} else {
		null
	}
}

// 华为适配齐刘海的判断
fun Activity.hasNotchInScreen(): Boolean {
	var ret = false
	try {
		val cl = classLoader
		val hwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil")
		val get = hwNotchSizeUtil.getMethod("hasNotchInScreen")
		ret = get.invoke(hwNotchSizeUtil) as Boolean
	} catch (e: ClassNotFoundException) {
		Log.e("test", "hasNotchInScreen ClassNotFoundException")
	} catch (e: NoSuchMethodException) {
		Log.e("test", "hasNotchInScreen NoSuchMethodException")
	} catch (e: Exception) {
		Log.e("test", "hasNotchInScreen Exception")
	} finally {
		return ret
	}
}