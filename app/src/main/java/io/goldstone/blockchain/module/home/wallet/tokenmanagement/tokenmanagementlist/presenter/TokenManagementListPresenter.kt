package io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.presenter

import com.blinnnk.extension.preventDuplicateClicks
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerPresenter
import io.goldstone.blockchain.kernel.commonmodel.MyTokenTable
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.view.TokenManagementListCell
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.view.TokenManagementListFragment

/**
 * @date 25/03/2018 5:11 PM
 * @author KaySaith
 */

class TokenManagementListPresenter(
  override val fragment: TokenManagementListFragment
  ) : BaseRecyclerPresenter<TokenManagementListFragment, DefaultTokenTable>() {

  override fun updateData(asyncData: ArrayList<DefaultTokenTable>?) {
    DefaultTokenTable.getTokens {
      fragment.asyncData = it
    }
  }

  fun updateMyTokensInfoBy(cell: TokenManagementListCell) {
    cell.apply {
      if (switch.isChecked) {
        // 如果选中状态那么把当前选中的数据插入到 `MyTokenTable` 中
        WalletTable.apply {
          getCurrentWalletAddress {
            MyTokenTable.insertBySymbol(getSymbol(), it?.address.orEmpty())
          }
        }
      } else {
        // 如果是关闭选中那么就在 `MyTokenTable` 中删除这条数据
        MyTokenTable.deleteBySymbol(getSymbol())
      }
      // 在 `DefaultTokenTable` 中更新 `isUsed` 的状态
      DefaultTokenTable.updateUsedStatusBySymbol(getSymbol(), switch.isChecked)
      // 防止重复点击
      cell.switch.preventDuplicateClicks()
    }
  }
}