package io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.view

import android.support.v4.app.Fragment
import android.view.Gravity
import com.blinnnk.extension.into
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.base.basefragment.BaseFragment
import io.goldstone.blockchain.common.component.SessionTitleView
import io.goldstone.blockchain.common.language.TokenDetailText
import io.goldstone.blockchain.common.value.Config
import io.goldstone.blockchain.common.value.Spectrum
import io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.common.TradingCardView
import io.goldstone.blockchain.module.common.tokendetail.eosresourcetrading.common.basetradingfragment.presenter.BaseTradingPresenter
import org.jetbrains.anko.*
import java.math.BigInteger


/**
 * @author KaySaith
 * @date  2018/09/18
 */
open class BaseTradingFragment : BaseFragment<BaseTradingPresenter>() {

	open val tradingType: TradingType = TradingType.CPU

	private val delegateTitle by lazy {
		SessionTitleView(context!!).setTitle(TokenDetailText.delegateCPUTitle)
	}

	private val refundTitle by lazy {
		SessionTitleView(context!!).setTitle(TokenDetailText.refundCPUTitle)
	}

	private val incomeTradingCard by lazy {
		TradingCardView(context!!).apply {
			setAccountHint(Config.getCurrentEOSName())
			setConfirmClickEvent {
				presenter.gainConfirmEvent { showLoading(false) }
			}
		}
	}

	private val expendTradingCard by lazy {
		TradingCardView(context!!).apply {
			setAccountHint(Config.getCurrentEOSName())
			setConfirmClickEvent {
				System.out.println("hello fuck you")
				presenter.refundOrSellConfirmEvent { showLoading(false) }
			}
		}
	}

	override val presenter = BaseTradingPresenter(this)

	override fun AnkoContext<Fragment>.initView() {
		scrollView {
			lparams(matchParent, matchParent)
			verticalLayout {
				lparams(matchParent, matchParent)
				gravity = Gravity.CENTER_HORIZONTAL
				topPadding = 10.uiPX()
				delegateTitle.into(this)
				delegateTitle.setSubtitle("0.0027 ", "Current Price: 0.0027 EOS/MS/Day", Spectrum.blue)
				incomeTradingCard.into(this)
				refundTitle.into(this)
				refundTitle.setSubtitle("0.0019", "Current Price: 0.0019 EOS/Byte/Day", Spectrum.blue)
				expendTradingCard.into(this)
			}
		}
	}

	fun setProcessUsage(weight: String, available: BigInteger, total: BigInteger) {
		val title = when (tradingType) {
			TradingType.CPU -> TokenDetailText.cpu
			TradingType.NET -> TokenDetailText.net
			TradingType.RAM -> TokenDetailText.ram
		}
		val isTime = when (tradingType) {
			TradingType.CPU -> true
			else -> false
		}
		incomeTradingCard.setProcessValue(title, weight, available, total, isTime)
		expendTradingCard.setProcessValue(title, weight, available, total, isTime)
	}

	fun getInputValue(stakeType: StakeType): Pair<String, Double> {
		return if (stakeType == StakeType.Delegate) incomeTradingCard.getInputValue()
		else expendTradingCard.getInputValue()
	}

	fun showLoading(status: Boolean, stakeType: StakeType) {
		if (stakeType == StakeType.Delegate) incomeTradingCard.showLoading(status)
		else expendTradingCard.showLoading(status)
	}

	fun clearInputValue() {
		incomeTradingCard.clearInput()
		expendTradingCard.clearInput()
	}

}

enum class TradingType {
	CPU, NET, RAM
}

enum class StakeType(val value: String) {
	Delegate("delegatebw"), Refund("undelegatebw")
}