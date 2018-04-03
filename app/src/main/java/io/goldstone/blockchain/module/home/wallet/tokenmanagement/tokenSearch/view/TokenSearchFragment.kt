package io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenSearch.view

import com.blinnnk.extension.orEmptyArray
import io.goldstone.blockchain.common.base.BaseRecyclerView
import io.goldstone.blockchain.common.base.baserecyclerfragment.BaseRecyclerFragment
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenSearch.presenter.TokenSearchPresenter
import io.goldstone.blockchain.module.home.wallet.tokenmanagement.tokenmanagementlist.model.DefaultTokenTable

/**
 * @date 27/03/2018 11:22 AM
 * @author KaySaith
 */

class TokenSearchFragment : BaseRecyclerFragment<TokenSearchPresenter, DefaultTokenTable>() {

  override val presenter = TokenSearchPresenter(this)

  override fun setRecyclerViewAdapter(
    recyclerView: BaseRecyclerView,
    asyncData: ArrayList<DefaultTokenTable>?
  ) {
    recyclerView.adapter = TokenSearchAdapter(asyncData.orEmptyArray())
  }

}