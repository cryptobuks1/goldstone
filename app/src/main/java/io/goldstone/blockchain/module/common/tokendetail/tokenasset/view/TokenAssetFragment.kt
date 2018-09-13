package io.goldstone.blockchain.module.common.tokendetail.tokenasset.view

import android.graphics.Bitmap
import android.os.Build
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.blinnnk.extension.into
import com.blinnnk.extension.scaleTo
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.clickToCopy
import io.goldstone.blockchain.R
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.GrayCardView
import io.goldstone.blockchain.common.component.ProgressView
import io.goldstone.blockchain.common.component.SessionTitleView
import io.goldstone.blockchain.common.component.cell.GraySquareCell
import io.goldstone.blockchain.common.component.cell.GraySquareCellWithButtons
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.TokenDetailText
import io.goldstone.blockchain.common.utils.GoldStoneFont
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.GrayScale
import io.goldstone.blockchain.common.value.ScreenSize
import io.goldstone.blockchain.common.value.fontSize
import io.goldstone.blockchain.module.common.tokendetail.tokenasset.presenter.TokenAssetPresenter
import io.goldstone.blockchain.module.common.tokendetail.tokeninfo.contract.TokenInfoViewInterface
import io.goldstone.blockchain.module.common.tokendetail.tokeninfo.view.TokenInfoView
import org.jetbrains.anko.*


/**
 * @author KaySaith
 * @date  2018/09/10
 */

class TokenAssetFragment : BaseFragment<TokenAssetPresenter>(), TokenInfoViewInterface {

	private val tokenInfoView by lazy { TokenInfoView(context!!) }

	private val balanceCell by lazy {
		GraySquareCell(context!!).apply {
			setTitle(TokenDetailText.balance)
			setSubtitle(CommonText.calculating)
		}
	}

	private val transactionCountCell by lazy {
		GraySquareCell(context!!).apply {
			setTitle(TokenDetailText.transactionCount)
			setSubtitle(CommonText.calculating)
		}
	}

	private val authorizationCell by lazy {
		GraySquareCell(context!!).apply {
			showArrow()
			setTitle(TokenDetailText.authority)
			setSubtitle(Config.getCurrentEOSName())
		}
	}

	private val accountAddress by lazy {
		GraySquareCellWithButtons(context!!).apply {
			showOnlyCopyButton {
				context?.clickToCopy(Config.getCurrentEOSAddress())
			}
			setTitle(TokenDetailText.address)
			setSubtitle(Config.getCurrentEOSAddress().scaleTo(20))
		}
	}

	private val assetCard by lazy {
		GrayCardView(context!!).apply {
			setCardParams(ScreenSize.widthWithPadding, 255.uiPX())
		}
	}

	private val ramAssetCell by lazy {
		ProgressView(context!!).apply {
			setTitle(TokenDetailText.ram)
			setSubtitle(CommonText.calculating)
		}
	}

	private val cpuAssetCell by lazy {
		ProgressView(context!!).apply {
			setTitle(TokenDetailText.cpu)
			setSubtitle(CommonText.calculating)
		}
	}

	private val netAssetCell by lazy {
		ProgressView(context!!).apply {
			setTitle(TokenDetailText.net)
			setSubtitle(CommonText.calculating)
		}
	}

	override val presenter = TokenAssetPresenter(this)
	override fun AnkoContext<Fragment>.initView() {
		scrollView {
			lparams(matchParent, matchParent)
			verticalLayout {
				lparams(matchParent, wrapContent)
				bottomPadding = 20.uiPX()
				gravity = Gravity.CENTER_HORIZONTAL
				tokenInfoView.into(this)
				showTransactionCells()
				showAccountManagementCells()
				showAssetDashboard()
				SessionTitleView(context).setTitle(TokenDetailText.assetTools).into(this)
				linearLayout {
					lparams(ScreenSize.widthWithPadding, wrapContent)
					generateMethodCards()
				}
			}
		}
	}

	override fun setTokenInfo(qrCode: Bitmap?, title: String, subtitle: String, icon: Int, action: () -> Unit) {
		tokenInfoView.setData(qrCode, title, subtitle, icon, action)
	}

	override fun updateLatestActivationDate(date: String) {
		tokenInfoView.updateLatestActivationDate(date)
	}

	fun setEOSBalance(balance: String) {
		balanceCell.setSubtitle(balance)
	}

	fun setResourcesValue(
		ramAvailable: Long,
		ramTotal: Long,
		cpuAvailable: Long,
		cpuTotal: Long,
		cpuWeight: String,
		netAvailable: Long,
		netTotal: Long,
		netWeight: String
	) {
		ramAssetCell.setLeftValue(ramAvailable, TokenDetailText.available)
		ramAssetCell.setRightValue(ramTotal, TokenDetailText.total)
		cpuAssetCell.setSubtitle(cpuWeight)
		cpuAssetCell.setLeftValue(
			cpuAvailable,
			TokenDetailText.available,
			true
		)
		cpuAssetCell.setRightValue(
			cpuTotal,
			TokenDetailText.total,
			true
		)
		netAssetCell.setSubtitle(netWeight)
		netAssetCell.setLeftValue(
			netAvailable,
			TokenDetailText.available
		)
		netAssetCell.setRightValue(
			netTotal,
			TokenDetailText.total
		)
	}

	fun setTransactionCount(count: String) {
		transactionCountCell.setSubtitle(count)
	}

	private fun ViewGroup.showAccountManagementCells() {
		SessionTitleView(context).setTitle(TokenDetailText.accountManagement).into(this)
		authorizationCell.into(this)
		accountAddress.into(this)
	}

	private fun ViewGroup.showTransactionCells() {
		SessionTitleView(context).setTitle(TokenDetailText.balance).into(this)
		balanceCell.into(this)
		transactionCountCell.into(this)
	}

	private fun ViewGroup.showAssetDashboard() {
		SessionTitleView(context).setTitle(TokenDetailText.resources).into(this)
		assetCard.apply {
			addView(ramAssetCell)
			addView(cpuAssetCell)
			addView(netAssetCell)
		}.into(this)
	}

	private fun ViewGroup.generateMethodCards() {
		listOf(
			Pair(R.drawable.cpu_icon, TokenDetailText.delegateCPU),
			Pair(R.drawable.net_icon, TokenDetailText.delegateNET),
			Pair(R.drawable.ram_icon, TokenDetailText.tradeRAM)
		).forEachIndexed { index, pair ->
			generateCardView(index, pair)
		}
	}

	private fun ViewGroup.generateCardView(position: Int, info: Pair<Int, String>) {
		val cardWidth = (ScreenSize.widthWithPadding - 10.uiPX()) / 3
		GrayCardView(context).apply {
			x = 5.uiPX() * position * 1f
			setCardParams(cardWidth, 130.uiPX())
			getContainer().apply {
				imageView {
					setColorFilter(GrayScale.gray)
					scaleType = ImageView.ScaleType.CENTER_INSIDE
					imageResource = info.first
					layoutParams = RelativeLayout.LayoutParams(cardWidth, 80.uiPX())
				}
				textView(info.second) {
					textSize = fontSize(11)
					textColor = GrayScale.midGray
					typeface = GoldStoneFont.black(context)
					layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
					gravity = Gravity.CENTER_HORIZONTAL
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
						lineHeight = 13.uiPX()
					}
				}
			}
		}.into(this)
	}
}