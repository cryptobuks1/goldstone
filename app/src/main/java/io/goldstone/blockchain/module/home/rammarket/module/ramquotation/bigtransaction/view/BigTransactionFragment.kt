package io.goldstone.blockchain.module.home.rammarket.module.ramquotation.bigtransaction.view

import android.os.Bundle
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import io.goldstone.blockchain.common.Language.EOSRAMExchangeText
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerFragment
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerView
import io.goldstone.blockchain.module.home.rammarket.module.ramquotation.bigtransaction.presenter.BigTransactionPresenter
import io.goldstone.blockchain.module.home.rammarket.module.ramtrade.model.TradingInfoModel
import io.goldstone.blockchain.module.home.rammarket.module.ramtransactionsearch.view.RAMTransactionSearchFragment
import io.goldstone.blockchain.module.home.rammarket.view.RAMMarketDetailFragment
import io.goldstone.blockchain.module.home.rammarket.view.RAMMarketOverlayFragment
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * @date: 2018/11/5.
 * @author: yanglihai
 * @description:
 */
class BigTransactionFragment : BaseRecyclerFragment<BigTransactionPresenter, TradingInfoModel>() {
	
	override val pageTitle: String = EOSRAMExchangeText.ramExchange
	override val presenter: BigTransactionPresenter = BigTransactionPresenter(this)
	
	override fun setRecyclerViewAdapter(
		recyclerView: BaseRecyclerView,
		asyncData: ArrayList<TradingInfoModel>?
	) {
		recyclerView.setHasFixedSize(true)
		recyclerView.isNestedScrollingEnabled = false
		
		recyclerView.adapter = BigTransactionsAdapter(asyncData.orEmptyArray().toArrayList()) {
			onClick {
				getParentFragment<RAMMarketDetailFragment> {
					getParentFragment<RAMMarketOverlayFragment> {
						presenter.showTransactionHistoryFragment(model.account)
					}
				}
			}
		}
	}
}