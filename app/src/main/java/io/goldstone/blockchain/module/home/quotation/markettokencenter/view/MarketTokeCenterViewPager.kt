package io.goldstone.blockchain.module.home.quotation.markettokencenter.view

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import com.blinnnk.base.HoneyBaseFragmentAdapter
import com.blinnnk.base.SubFragment
import io.goldstone.blockchain.common.value.FragmentTag
import io.goldstone.blockchain.common.value.ViewPagerID
import io.goldstone.blockchain.module.home.quotation.markettokendetail.view.MarketTokenDetailFragment
import java.util.*

@SuppressLint("ViewConstructor")
/**
 * @date 2018/8/9 5:03 PM
 * @author KaySaith
 */

class MarketTokeCenterViewPager(val fragment: Fragment) : ViewPager(fragment.context!!) {

	private var fragmentList = ArrayList<SubFragment>()
	private val marketDetail by lazy {
		MarketTokenDetailFragment()
	}
	private val alarmDetail by lazy { Fragment() }

	init {
		id = ViewPagerID.transactions
		fragmentList.apply {
			add(SubFragment(marketDetail, FragmentTag.tokenMarketDetail))
			add(SubFragment(alarmDetail, FragmentTag.alarmDetail))
		}
		adapter = HoneyBaseFragmentAdapter(fragment.childFragmentManager, fragmentList)
	}
}