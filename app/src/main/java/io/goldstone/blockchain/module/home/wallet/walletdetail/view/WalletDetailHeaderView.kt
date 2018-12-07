package io.goldstone.blockchain.module.home.wallet.walletdetail.view

import android.R
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.FixTextLength
import com.blinnnk.util.observing
import io.goldstone.blockchain.common.component.UnlimitedAvatar
import io.goldstone.blockchain.common.component.button.RoundButtonWithIcon
import io.goldstone.blockchain.common.component.button.StoneButton
import io.goldstone.blockchain.common.language.CommonText
import io.goldstone.blockchain.common.language.WalletText
import io.goldstone.blockchain.common.sharedpreference.SharedWallet
import io.goldstone.blockchain.common.utils.*
import io.goldstone.blockchain.common.value.PaddingSize
import io.goldstone.blockchain.common.value.Spectrum
import io.goldstone.blockchain.common.value.WalletDetailSize
import io.goldstone.blockchain.common.value.fontSize
import io.goldstone.blockchain.crypto.utils.formatCurrency
import io.goldstone.blockchain.module.home.wallet.walletdetail.model.WalletDetailHeaderModel
import me.itangqi.waveloadingview.WaveLoadingView
import org.jetbrains.anko.*

/**
 * @date 23/03/2018 4:21 PM
 * @author KaySaith
 * @rewriteDate 26/07/2018 3:30 PM
 * @reWriter wcx
 * @description 修改获取头像方法 UnlimitedAvatar创建bitmap
 */
class WalletDetailHeaderView(context: Context) : RelativeLayout(context) {
	var model: WalletDetailHeaderModel? by observing(null) {
		model?.apply {
			balanceTitle.text = totalBalance.toDouble().formatCurrency()
			currentAccount.info.title.text = object : FixTextLength() {
				override var text = model?.name.orEmpty()
				override val maxWidth = 26.uiPX().toFloat()
				override val textSize: Float = fontSize(16)
			}.getFixString()

			currentAccount.info.subtitle.text = address
			balanceSubtitle.text = WalletSlideHeader.setBalanceInfo()
			// 钱包一样的话每次刷新不用重新加载图片给内存造成压力
			if (avatar.isNull())
				currentAccount.avatar.glideImage(AvatarManager.getAvatarPath(SharedWallet.getCurrentWalletID()))
			else currentAccount.avatar.glideImage(avatar)
		}
	}
	val addTokenButton = RoundButtonWithIcon(context)
	val currentAccount = CurrentAccountView(context)
	private val waveView = WaveLoadingView(context)
	private var progressBar: ProgressBar? = null
	private val balanceTitle by lazy { TextView(context) }
	private val sectionHeaderHeight = 25.uiPX()
	private lateinit var balanceSubtitle: TextView
	val sendButton by lazy { StoneButton(context) }
	val depositButton by lazy { StoneButton(context) }

	init {
		setWillNotDraw(false)

		layoutParams = RelativeLayout.LayoutParams(matchParent, WalletDetailSize.headerHeight)

		waveView.apply {
			layoutParams =
				RelativeLayout.LayoutParams(matchParent, WalletDetailSize.headerHeight - 50.uiPX())
			setShapeType(WaveLoadingView.ShapeType.RECTANGLE)
			progressValue = 30
			waveColor = Color.parseColor("#FF1c4f7b")
			setAnimDuration(30000)
			setAmplitudeRatio(50)
			startAnimation()
		}.into(this)

		currentAccount.into(this)
		currentAccount.apply {
			centerInHorizontal()
			y += 30.uiPX()
		}

		verticalLayout {
			balanceTitle.apply {
				textSize = fontSize(36)
				typeface = GoldStoneFont.black(context)
				textColor = Spectrum.white
				gravity = Gravity.CENTER_HORIZONTAL
			}.into(this)

			balanceSubtitle = textView {
				textSize = fontSize(12)
				typeface = GoldStoneFont.medium(context)
				textColor = Spectrum.opacity5White
				gravity = Gravity.CENTER_HORIZONTAL
			}.lparams(matchParent, matchParent)
		}.apply {
			centerInParent()
		}

		relativeLayout {
			lparams {
				width = matchParent
				height = 80.uiPX()
				alignParentBottom()
				y -= sectionHeaderHeight
			}

			sendButton.apply {
				text = CommonText.send
			}.into(this)
			sendButton.setMargins<RelativeLayout.LayoutParams> {
				leftMargin = 15.uiPX()
			}

			depositButton.apply { text = CommonText.deposit }.into(this)
			depositButton.setMargins<RelativeLayout.LayoutParams> {
				rightMargin = 15.uiPX()
			}

			depositButton.alignParentRight()
		}

		textView {
			text = WalletText.section.toUpperCase()
			typeface = GoldStoneFont.heavy(context)
			textColor = Spectrum.white
			textSize = fontSize(15)
			y -= 10.uiPX()
		}.apply {
			alignParentBottom()
			x += PaddingSize.device
		}

		addTokenButton.apply {
			setTitle(WalletText.addToken.toUpperCase())
			x -= PaddingSize.device
			y -= 10.uiPX()
		}.into(this)

		addTokenButton.apply {
			removeIcon()
			layoutParams.height = 24.uiPX()
			alignParentRight()
			alignParentBottom()
		}
	}

	fun showLoadingView(status: Boolean) {
		if (status && progressBar.isNull()) {
			progressBar = ProgressBar(
				this.context,
				null,
				R.attr.progressBarStyleInverse
			).apply {
				indeterminateDrawable.setColorFilter(
					Spectrum.white,
					android.graphics.PorterDuff.Mode.SRC_ATOP
				)
				layoutParams = RelativeLayout.LayoutParams(16.uiPX(), 16.uiPX())
				x = WalletText.section.toUpperCase().measureTextWidth(16.uiPX().toFloat()) + 16.uiPX()
				y -= 12.uiPX()
			}
			progressBar?.into(this)
			progressBar?.alignParentBottom()
		} else {
			if (!progressBar.isNull()) {
				removeView(progressBar)
				progressBar = null
			}
		}
	}

	fun clearBitmap() {
	}
}

