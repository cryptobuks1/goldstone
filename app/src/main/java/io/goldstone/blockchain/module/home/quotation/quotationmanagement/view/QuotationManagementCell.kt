package io.goldstone.blockchain.module.home.quotation.quotationmanagement.view

import android.content.Context
import android.widget.ImageView
import android.widget.RelativeLayout
import com.blinnnk.extension.alignParentRight
import com.blinnnk.extension.centerInVertical
import com.blinnnk.extension.into
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.R
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.view.TokenManagementListCell
import org.jetbrains.anko.imageResource

/**
 * @date 21/04/2018 3:58 PM
 * @author KaySaith
 */

class QuotationManagementCell(context: Context) : TokenManagementListCell(context) {

	private val dragIcon by lazy { ImageView(context) }

	init {
		dragIcon.apply {
			imageResource = R.drawable.drag_icon
			setColorFilter(GrayScale.lightGray)
			layoutParams = RelativeLayout.LayoutParams(30.uiPX(), 30.uiPX())
			x -= 50.uiPX()
		}.into(this)

		dragIcon.centerInVertical()
		dragIcon.alignParentRight()
		switch.isChecked = true
		hideIcon()
	}

}