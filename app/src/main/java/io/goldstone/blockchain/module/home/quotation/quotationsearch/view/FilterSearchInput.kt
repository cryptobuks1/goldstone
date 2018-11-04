package io.goldstone.blockchain.module.home.quotation.quotationsearch.view

import android.content.Context
import android.graphics.PorterDuff
import android.view.Gravity
import android.view.View
import android.widget.*
import com.blinnnk.extension.addCorner
import com.blinnnk.uikit.ScreenSize
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.R
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.EmptyText
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.utils.click
import io.goldstone.blockchain.common.value.*
import org.jetbrains.anko.*

/**
 * @date: 2018/8/28.
 * @author: yanglihai
 * @description:
 */
class FilterSearchInput(context: Context) : LinearLayout(context) {
	private val filterIcon by lazy {
		ImageView(context).apply {
			layoutParams = LinearLayout.LayoutParams(30.uiPX(), matchParent)
			imageResource = R.drawable.icon_filtrate
			scaleType = ImageView.ScaleType.CENTER_INSIDE
			visibility = View.GONE
		}
	}
	
	val editText by lazy {
		EditText(context).apply {
			hint = EmptyText.searchInput
			backgroundTintMode = PorterDuff.Mode.CLEAR
			textSize = fontSize(12)
			textColor = GrayScale.black
			hintTextColor = GrayScale.midGray
			typeface = GoldStoneFont.book(context)
			singleLine = true
			layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
			background = null
		}
	}
	
	private val cancelButton by lazy {
		TextView(context).apply {
			text = CommonText.cancel
			textColor = GrayScale.midGray
			textSize = fontSize(13)
			typeface = GoldStoneFont.book(context)
			layoutParams = RelativeLayout.LayoutParams(70.uiPX(), matchParent)
			gravity = Gravity.CENTER
		}
	}
	
	init {
		id = ElementID.searchInput
		layoutParams = LinearLayout.LayoutParams(matchParent, matchParent)
		leftPadding = 20.uiPX()
		rightPadding = 20.uiPX()
		
		linearLayout {
			gravity = Gravity.CENTER_VERTICAL
			layoutParams = LinearLayout.LayoutParams(ScreenSize.Width - 100.uiPX(), 38.uiPX()).apply {
				gravity = Gravity.CENTER_VERTICAL
			}
			leftPadding = 10.uiPX()
			
			addCorner(CornerSize.default.toInt(), Spectrum.white)
			
			addView(filterIcon)
			addView(editText)
		}
		addView(cancelButton)
		
	}
	
	fun showFilterImage(visible: Boolean) {
		filterIcon.visibility = if(visible) View.VISIBLE else View.GONE
	}
	
	fun setCancelClick(callback: () -> Unit) {
		cancelButton.click { callback() }
	}
	
	fun setFilterClickEvent(callback: () -> Unit) {
		filterIcon.click { callback() }
	}
	
	fun setFiltered(hasFiltered: Boolean) {
		if (hasFiltered) {
			filterIcon.setColorFilter(GrayScale.black)
		} else {
			filterIcon.setColorFilter(GrayScale.lightGray)
		}
	}
	
}