package io.goldstone.blinnnk.module.home.wallet.walletsettings.walletsettingslist.view

import android.content.Context
import android.widget.TextView
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blinnnk.common.base.basecell.BaseCell
import io.goldstone.blinnnk.common.language.WalletSettingsText
import io.goldstone.blinnnk.common.utils.GoldStoneFont
import io.goldstone.blinnnk.common.value.GrayScale
import io.goldstone.blinnnk.common.value.Spectrum
import io.goldstone.blinnnk.common.value.fontSize
import io.goldstone.blinnnk.module.home.wallet.walletmanagement.walletlist.view.WalletListCardCell
import io.goldstone.blinnnk.module.home.wallet.walletsettings.walletsettingslist.model.WalletSettingsListModel
import org.jetbrains.anko.textColor

/**
 * @date 25/03/2018 10:16 PM
 * @author KaySaith
 */
class WalletSettingsListCell(context: Context) : BaseCell(context) {

	var model: WalletSettingsListModel by observing(WalletSettingsListModel()) {
		title.text = model.title
		when {
			model.description == WalletSettingsText.safeAttention -> {
				description.textColor = Spectrum.red
				description.text = model.description.setBold().setItalic()
			}

			else -> description.text = WalletListCardCell.getFixedTitleLength(model.description.toString())
		}
	}
	private val title = TextView(context)
	private val description by lazy { TextView(context) }
	private var titleColor = GrayScale.black

	init {
		title.apply {
			textColor = titleColor
			textSize = fontSize(14)
			typeface = GoldStoneFont.medium(context)
		}.into(this)

		description.apply {
			textSize = fontSize(14)
			typeface = GoldStoneFont.medium(context)
			textColor = GrayScale.gray
			x -= 30.uiPX()
		}.into(this)
		setHorizontalPadding()
		setGrayStyle()
		title.centerInVertical()
		description.apply {
			alignParentRight()
			centerInVertical()
		}

		layoutParams.height = 50.uiPX()
	}
}