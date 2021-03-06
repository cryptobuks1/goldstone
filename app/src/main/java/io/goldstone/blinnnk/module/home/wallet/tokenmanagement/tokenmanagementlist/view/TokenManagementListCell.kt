package io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenmanagementlist.view

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.Switch
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blinnnk.R
import io.goldstone.blinnnk.common.base.basecell.BaseCell
import io.goldstone.blinnnk.common.component.button.SquareIcon
import io.goldstone.blinnnk.common.component.title.TwoLineTitles
import io.goldstone.blinnnk.common.utils.glideImage
import io.goldstone.blinnnk.common.value.Spectrum
import io.goldstone.blinnnk.crypto.multichain.*
import io.goldstone.blinnnk.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blinnnk.module.home.wallet.walletdetail.model.WalletDetailCellModel
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.switch

/**
 * @date 25/03/2018 5:12 PM
 * @author KaySaith
 */
open class TokenManagementListCell(context: Context) : BaseCell(context) {

	var model: WalletDetailCellModel? by observing(null) {
		model?.apply {
			// 显示默认图判断
			when {
				iconUrl.isBlank() -> icon.image.imageResource = R.drawable.default_token
				contract.isETH() -> icon.image.imageResource = R.drawable.eth_icon
				contract.isETC() -> icon.image.imageResource = R.drawable.etc_icon
				contract.isLTC() -> icon.image.imageResource = R.drawable.ltc_icon
				contract.isBCH() -> icon.image.imageResource = R.drawable.bch_icon
				contract.isEOS() -> icon.image.imageResource = R.drawable.eos_icon
				contract.isBTC() ->
					icon.image.imageResource = R.drawable.btc_icon
				else -> icon.image.glideImage(iconUrl)
			}
			tokenInfo.title.text = symbol.symbol
			tokenInfo.subtitle.text = when {
				contract.isERC20Token() -> "contract address\n" suffix contract.contract.scaleTo(36)
				contract.isEOSToken() -> "code name:" suffix contract.contract
				else -> if (tokenName.isEmpty()) symbol.symbol else tokenName
			}
		}
	}

	var tokenSearchModel: DefaultTokenTable? by observing(null) {
		tokenSearchModel?.apply {
			// 显示默认图判断
			when {
				iconUrl.isBlank() -> icon.image.imageResource = R.drawable.default_token
				TokenContract(this).isETH() -> icon.image.imageResource = R.drawable.eth_icon
				TokenContract(this).isETC() -> icon.image.imageResource = R.drawable.etc_icon
				TokenContract(this).isLTC() -> icon.image.imageResource = R.drawable.ltc_icon
				TokenContract(this).isBCH() -> icon.image.imageResource = R.drawable.bch_icon
				TokenContract(this).isEOS() -> icon.image.imageResource = R.drawable.eos_icon
				TokenContract(this).isBTC() ->
					icon.image.imageResource = R.drawable.btc_icon
				else -> icon.image.glideImage(iconUrl)
			}
			tokenInfo.title.text = symbol
			tokenInfo.subtitle.text = when {
				TokenContract(this).isERC20Token() -> "contract address:\n" suffix contract.scaleTo(36)
				TokenContract(this).isEOSToken() -> "code name:" suffix contract
				else -> if (name.isEmpty()) symbol else name
			}
			switch.isChecked = isUsed
		}
	}

	var switch: Switch
	protected val tokenInfo by lazy { TwoLineTitles(context) }
	protected val icon by lazy { SquareIcon(context, SquareIcon.Companion.Style.Big) }

	init {
		hasArrow = false
		setHorizontalPadding()
		this.addView(icon.apply {
			setGrayStyle()
			y += 10.uiPX()
		})

		this.addView(tokenInfo.apply {
			setBlackTitles()
			x += 10.uiPX()
		})

		switch = switch {
			layoutParams = RelativeLayout.LayoutParams(50.uiPX(), matchParent)
			isDefaultStyle(Spectrum.blue)
		}

		tokenInfo.apply {
			centerInVertical()
			x += 40.uiPX()
		}

		switch.apply {
			centerInVertical()
			alignParentRight()
		}

		setGrayStyle()
	}

	fun showArrow() {
		removeView(switch)
		hasArrow = true
	}

	fun hideIcon() {
		icon.visibility = View.GONE
		tokenInfo.x = 0f
	}
}