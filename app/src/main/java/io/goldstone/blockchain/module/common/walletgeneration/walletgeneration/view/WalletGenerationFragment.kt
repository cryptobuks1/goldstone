package io.goldstone.blockchain.module.common.walletgeneration.walletgeneration.view

import android.view.ViewGroup
import com.blinnnk.extension.getRealScreenHeight
import com.blinnnk.extension.orZero
import com.blinnnk.util.addFragmentAndSetArgument
import io.goldstone.blockchain.common.base.baseoverlayfragment.BaseOverlayFragment
import io.goldstone.blockchain.common.value.CreateWalletText
import io.goldstone.blockchain.common.value.FragmentTag
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.view.CreateWalletFragment
import io.goldstone.blockchain.module.common.walletgeneration.walletgeneration.presenter.WalletGenerationPresenter

/**
 * @date 22/03/2018 9:37 PM
 * @author KaySaith
 */

class WalletGenerationFragment : BaseOverlayFragment<WalletGenerationPresenter>() {

  override val presenter = WalletGenerationPresenter(this)

  override fun setContentHeight() = activity?.getRealScreenHeight().orZero()

  override fun ViewGroup.initView() {
    presenter.showCreateWalletFragment()
  }

}