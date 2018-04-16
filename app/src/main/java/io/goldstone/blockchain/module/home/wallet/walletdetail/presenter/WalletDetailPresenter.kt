package io.goldstone.blockchain.module.home.wallet.walletdetail.presenter

import com.blinnnk.extension.*
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.common.value.ArgumentKey
import io.goldstone.blockchain.common.value.ContainerID
import io.goldstone.blockchain.common.value.WalletSettingsText
import io.goldstone.blockchain.crypto.CryptoUtils
import io.goldstone.blockchain.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.presenter.CreateWalletPresenter
import io.goldstone.blockchain.module.home.wallet.currentwalletdetail.view.CurrentWalletDetailFragment
import io.goldstone.blockchain.module.home.wallet.notifications.notification.view.NotificationFragment
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagement.view.TokenManagementFragment
import io.goldstone.blockchain.module.home.wallet.transactions.transaction.view.TransactionFragment
import io.goldstone.blockchain.module.home.wallet.walletdetail.model.WalletDetailCellModel
import io.goldstone.blockchain.module.home.wallet.walletdetail.view.WalletDetailAdapter
import io.goldstone.blockchain.module.home.wallet.walletdetail.view.WalletDetailFragment
import io.goldstone.blockchain.module.home.wallet.walletdetail.view.WalletDetailHeaderModel
import io.goldstone.blockchain.module.home.wallet.walletdetail.view.WalletDetailHeaderView
import io.goldstone.blockchain.module.home.wallet.walletsettings.walletsettings.view.WalletSettingsFragment

/**
 * @date 23/03/2018 3:45 PM
 * @author KaySaith
 */

class WalletDetailPresenter(
  override val fragment: WalletDetailFragment
) : BaseRecyclerPresenter<WalletDetailFragment, WalletDetailCellModel>() {

  fun updateAllTokensInWalletBy() {
    // Check the count of local wallets
    WalletTable.apply { getAll { walletCount = size } }
    // Check the info of wallet currency list
    WalletDetailCellModel.getModels { it ->
      val newData
        = it.sortedByDescending { it.currency }.toArrayList()
      fragment.asyncData.isNull().isTrue {
        fragment.asyncData = newData
      } otherwise {
        diffAndUpdateAdapterData<WalletDetailAdapter>(newData)
      }
      fragment.updateHeaderValue()
    }
  }

  /**
   * 每次后台到前台更新首页的 `token` 信息
   */
  override fun onFragmentResume() {
    CreateWalletPresenter.updateMyTokensValue {
      updateAllTokensInWalletBy()
    }
  }

  fun showTransactionsFragment() {
    fragment.activity?.addFragment<TransactionFragment>(ContainerID.main)
  }

  fun showWalletListFragment() {
    fragment.activity?.addFragment<CurrentWalletDetailFragment>(ContainerID.main)
  }

  fun showNotificationListFragment() {
    fragment.activity?.addFragment<NotificationFragment>(ContainerID.main)
  }

  fun showTokenManagementFragment() {
    fragment.activity?.addFragment<TokenManagementFragment>(ContainerID.main)
  }

  fun showWalletSettingsFragment() {
    fragment.activity?.addFragmentAndSetArguments<WalletSettingsFragment>(ContainerID.main) {
      putString(ArgumentKey.walletSettingsTitle, WalletSettingsText.walletSettings)
    }
  }

  fun showMyTokenDetailFragment(symbol: String) {
    fragment.activity?.addFragmentAndSetArguments<TokenDetailOverlayFragment>(ContainerID.main) {
      putString(ArgumentKey.tokenDetail, symbol)
    }
  }

  private fun WalletDetailFragment.updateHeaderValue() {
    val totalBalance = fragment.asyncData?.sumByDouble { it.currency }
    // Once the calculation is finished then update `WalletTable`
    WalletTable.current.balance = totalBalance
    recyclerView.getItemViewAtAdapterPosition<WalletDetailHeaderView>(0) {
      model = WalletDetailHeaderModel(
        null,
        WalletTable.current.name,
        CryptoUtils.scaleAddress(WalletTable.current.address),
        totalBalance.toString(),
        WalletTable.walletCount.orZero()
      )
    }
  }
}