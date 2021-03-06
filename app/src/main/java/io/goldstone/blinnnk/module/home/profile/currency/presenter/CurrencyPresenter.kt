package io.goldstone.blinnnk.module.home.profile.currency.presenter

import com.blinnnk.extension.isNull
import com.blinnnk.extension.toArrayList
import io.goldstone.blinnnk.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.thread.launchDefault
import io.goldstone.blinnnk.kernel.commontable.AppConfigTable
import io.goldstone.blinnnk.kernel.commontable.SupportCurrencyTable
import io.goldstone.blinnnk.module.home.profile.currency.view.CurrencyAdapter
import io.goldstone.blinnnk.module.home.profile.currency.view.CurrencyFragment

@Suppress("DEPRECATION")
/**
 * @date 26/03/2018 2:24 PM
 * @author KaySaith
 */
class CurrencyPresenter(
	override val fragment: CurrencyFragment
) : BaseRecyclerPresenter<CurrencyFragment, SupportCurrencyTable>() {

	override fun updateData() {
		SupportCurrencyTable.getSupportCurrencies { currencies ->
			val data =
				currencies.distinctBy { it.currencySymbol }.toArrayList()
			if (fragment.asyncData.isNull()) {
				fragment.asyncData = data
			} else {
				diffAndUpdateSingleCellAdapterData<CurrencyAdapter>(data)
			}
		}
	}

	fun updateCurrency(symbol: String) {
		launchDefault {
			val currencyDao = SupportCurrencyTable.dao
			currencyDao.setCurrentCurrencyUnused()
			currencyDao.setCurrencyInUse(symbol)
			val rate = currencyDao.getCurrencyBySymbol(symbol)?.rate
			AppConfigTable.dao.updateCurrency(symbol)
			rate?.let {
				SharedWallet.updateCurrentRate(it)
			}
			SharedWallet.updateCurrencyCode(symbol)
		}
	}
}