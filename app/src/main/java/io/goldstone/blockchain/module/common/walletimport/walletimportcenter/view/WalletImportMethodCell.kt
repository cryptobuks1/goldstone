package io.goldstone.blockchain.module.common.walletimport.walletimportcenter.view

import android.content.Context
import android.widget.ImageView
import android.widget.RelativeLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.R
import io.goldstone.blockchain.common.component.GSCard
import io.goldstone.blockchain.common.component.title.TwoLineTitles
import io.goldstone.blockchain.common.language.ImportMethodText
import io.goldstone.blockchain.common.language.ImportWalletText
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.value.*
import io.goldstone.blockchain.common.value.ScreenSize
import org.jetbrains.anko.*


/**
 * @author KaySaith
 * @date  2018/09/06
 */
class WalletImportMethodCell(context: Context) : GSCard(context) {

	private val titles = TwoLineTitles(context).apply {
		setBigWhiteStyle(18)
		title.typeface = GoldStoneFont.heavy(context)
		layoutParams = RelativeLayout.LayoutParams(wrapContent, wrapContent)
	}
	private val iconSize = 30.uiPX()
	private val arrowIcon = ImageView(context).apply {
		imageResource = R.drawable.arrow_icon
		layoutParams = RelativeLayout.LayoutParams(iconSize, iconSize)
		scaleType = ImageView.ScaleType.CENTER_CROP
		alpha = 0.2f
	}

	private val typeIconSize = 70.uiPX()
	private val typeIcon = ImageView(context).apply {
		layoutParams = RelativeLayout.LayoutParams(typeIconSize, typeIconSize)
		setColorFilter(Spectrum.white)
		alpha = 0.2f
	}

	init {
		setCardBackgroundColor(Spectrum.blue)
		setContentPadding(20.uiPX(), 0, 20.uiPX(), 0)
		layoutParams = RelativeLayout.LayoutParams(ScreenSize.card, 110.uiPX())
		relativeLayout {
			titles.into(this)
			arrowIcon.into(this)
			arrowIcon.apply {
				setAlignParentRight()
				setCenterInVertical()
			}
			lparams(matchParent, matchParent)
			typeIcon.into(this)
			typeIcon.setAlignParentBottom()
			typeIcon.y += 45.uiPX()
		}
	}

	fun setMnemonicType() {
		titles.apply {
			title.text = ImportMethodText.mnemonic
			subtitle.text = ImportWalletText.importMnemonicsHint
			setCenterInVertical()
		}
		typeIcon.imageResource = R.drawable.mnemonic_icon
	}

	fun setPrivateKeyType() {
		titles.apply {
			title.text = ImportMethodText.privateKey
			subtitle.text = ImportWalletText.importPrivateKeyHint
			setCenterInVertical()
		}
		typeIcon.imageResource = R.drawable.private_key_import_icon
		typeIcon.y -= 10.uiPX()
	}

	fun setKeystoreType() {
		titles.apply {
			title.text = ImportMethodText.keystore
			subtitle.text = ImportWalletText.importKeystoreHint
			setCenterInVertical()
		}
		typeIcon.imageResource = R.drawable.keystore_import_icon
		typeIcon.y += 10.uiPX()
	}
}