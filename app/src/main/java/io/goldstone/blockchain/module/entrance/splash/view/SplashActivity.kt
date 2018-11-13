package io.goldstone.blockchain.module.entrance.splash.view

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.WorkerThread
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.blinnnk.extension.addFragment
import com.blinnnk.extension.into
import com.blinnnk.extension.isNull
import com.blinnnk.extension.jump
import io.goldstone.blockchain.common.component.GradientType
import io.goldstone.blockchain.common.component.GradientView
import io.goldstone.blockchain.common.component.container.SplashContainer
import io.goldstone.blockchain.common.error.GoldStoneError
import io.goldstone.blockchain.common.language.HoneyLanguage
import io.goldstone.blockchain.common.language.currentLanguage
import io.goldstone.blockchain.common.sharedpreference.SharedChain
import io.goldstone.blockchain.common.sharedpreference.SharedValue
import io.goldstone.blockchain.common.sharedpreference.SharedWallet
import io.goldstone.blockchain.common.thread.launchUI
import io.goldstone.blockchain.common.utils.NetworkUtil
import io.goldstone.blockchain.common.utils.transparentStatus
import io.goldstone.blockchain.common.value.ApkChannel
import io.goldstone.blockchain.common.value.ContainerID
import io.goldstone.blockchain.common.value.CountryCode
import io.goldstone.blockchain.common.value.currentChannel
import io.goldstone.blockchain.crypto.multichain.*
import io.goldstone.blockchain.crypto.multichain.node.ChainURL
import io.goldstone.blockchain.crypto.utils.getObjectMD5HexString
import io.goldstone.blockchain.kernel.commonmodel.AppConfigTable
import io.goldstone.blockchain.kernel.database.GoldStoneDataBase
import io.goldstone.blockchain.kernel.network.common.GoldStoneAPI
import io.goldstone.blockchain.kernel.receiver.XinGePushReceiver
import io.goldstone.blockchain.kernel.receiver.registerDeviceForPush
import io.goldstone.blockchain.module.common.walletgeneration.createwallet.model.WalletTable
import io.goldstone.blockchain.module.entrance.splash.presenter.SplashPresenter
import io.goldstone.blockchain.module.entrance.splash.presenter.SplashPresenter.Companion.updateAccountInformation
import io.goldstone.blockchain.module.entrance.starting.presenter.StartingPresenter.Companion.updateShareContentFromServer
import io.goldstone.blockchain.module.entrance.starting.view.StartingFragment
import io.goldstone.blockchain.module.home.home.view.MainActivity
import me.itangqi.waveloadingview.WaveLoadingView
import org.jetbrains.anko.doAsync

/**
────────────────────────────────────────────────────────────────────────────────────
Copyright (C) 2018 Pʀᴏᴅᴜᴄᴇ Bʏ Vɪsɪᴏɴ Cᴏʀᴇ Cʀᴏᴘ.
────────────────────────────────────────────────────────────────────────────────────
 */
class SplashActivity : AppCompatActivity() {

	var backEvent: Runnable? = null
	private val container by lazy { SplashContainer(this) }
	private val presenter = SplashPresenter(this)
	private val gradientView by lazy {
		GradientView(this).apply { setStyle(GradientType.Blue) }
	}
	private val waveView by lazy { WaveLoadingView(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// 判断 `SaveInstanceState` 防止旋转屏幕重新创建 `Fragment`
		if (savedInstanceState.isNull()) {
			transparentStatus()
			setContentView(container.apply {
				gradientView.into(this)
				initWaveView()
			})
			presenter.cleanWhenUpdateDatabaseOrElse {
				// WorkThread
				prepareData(it)
			}
		}
	}

	@WorkerThread
	private fun prepareYingYongBaoInReviewStatus(callback: (GoldStoneError) -> Unit) {
		// 如果不是 `YingYongBao` 渠道跳过
		if (!currentChannel.value.equals(ApkChannel.Tencent.value, true)) {
			SharedWallet.updateYingYongBaoInReviewStatus(false)
			callback(GoldStoneError.None)
			return
		}
		// 没有网络直接返回
		if (!NetworkUtil.hasNetwork(this)) {
			callback(GoldStoneError.None)
			return
		}
		// 从服务器获取配置状态
		GoldStoneAPI.getConfigList { serverConfigs, error ->
			if (serverConfigs != null && error.isNone()) {
				val isInReview = serverConfigs.find {
					it.name.equals("inReview", true)
				}?.switch?.toIntOrNull() == 1
				if (isInReview) {
					SharedWallet.updateYingYongBaoInReviewStatus(true)
				} else {
					SharedWallet.updateYingYongBaoInReviewStatus(false)
				}
				callback(error)
			} else callback(error)
		}
	}

	@WorkerThread
	private fun prepareData(allWallet: List<WalletTable>) {
		// WorkThread
		prepareAppConfig config@{
			SharedValue.updatePincodeDisplayStatus(showPincode)
			// 如果本地的钱包数量不为空那么才开始注册设备
			if (allWallet.isNotEmpty()) {
				registerDeviceForPush()
				// 把 `GoldStoneID` 存储到 `SharePreference` 里面
				SharedWallet.updateGoldStoneID(goldStoneID)
			}
			initLaunchLanguage(language)
			findViewById<RelativeLayout>(ContainerID.splash)?.let { it ->
				if (
					supportFragmentManager.fragments.find { it is StartingFragment }.isNull()
				) launchUI {
					addFragment<StartingFragment>(it.id)
				}
			}
			// 错开动画时间再执行数据请求
			// Add currency data from local JSON file
			presenter.apply {
				initSupportCurrencyList {
					// 网络判断是否执行更新网络上数据的方法
					if (NetworkUtil.hasNetwork(activity)) {
						// 更新分享的文案内容
						updateShareContentFromServer()
						// 更新用户条款如果有必要
						updateAgreement()
						// Insert support currency list from local json
						updateCurrencyRateFromServer(this@config)
					}
				}
				initDefaultMarketByNetWork()

				// Init Node List
				initNodeList {
					prepareNodeInfo {
						// Check network to get default toke list
						initDefaultToken {
							prepareYingYongBaoInReviewStatus {
								updateAccountInformation(activity) {
									jump<MainActivity>()
								}
							}
						}
					}
				}
			}
		}
	}

	override fun onBackPressed() {
		if (backEvent.isNull()) {
			super.onBackPressed()
		} else {
			backEvent?.run()
		}
	}

	@WorkerThread
	private fun prepareNodeInfo(callback: () -> Unit) {
		val chainDao = GoldStoneDataBase.database.chainNodeDao()
		val eosTest = chainDao.getTestnetEOSNode()
		val eosMain = chainDao.getMainnetEOSNode()
		val localNode = if (SharedValue.isTestEnvironment()) {
			chainDao.getTestnet()
		} else chainDao.getMainnet()
		localNode.forEach {
			when {
				ChainType(it.chainType).isETH() -> SharedChain.updateCurrentETH(ChainURL(it))
				ChainType(it.chainType).isBTC() -> SharedChain.updateBTCCurrent(ChainURL(it))
				ChainType(it.chainType).isLTC() -> SharedChain.updateLTCCurrent(ChainURL(it))
				ChainType(it.chainType).isBCH() -> SharedChain.updateBCHCurrent(ChainURL(it))
				ChainType(it.chainType).isEOS() -> {
					SharedChain.updateEOSCurrent(ChainURL(it))
					SharedChain.updateEOSTestnet(ChainURL(eosTest))
					SharedChain.updateEOSMainnet(ChainURL(eosMain))
				}
				ChainType(it.chainType).isETC() -> SharedChain.updateETCCurrent(ChainURL(it))
			}
		}
		callback()
	}

	@WorkerThread
	private fun updateAgreement() {
		val configDao = GoldStoneDataBase.database.appConfigDao()
		val config = configDao.getAppConfig()
		config?.apply {
			val md5 = terms.getObjectMD5HexString()
			GoldStoneAPI.getTerms(md5) { term, error ->
				if (!term.isNullOrBlank() && error.isNone()) {
					configDao.updateTerms(terms)
				}
			}
		}
	}

	private fun prepareAppConfig(@WorkerThread callback: AppConfigTable.() -> Unit) {
		doAsync {
			val config =
				GoldStoneDataBase.database.appConfigDao().getAppConfig()
			if (config == null) {
				// 如果本地没有配置过 `Config` 你那么首先更新语言为系统语言
				currentLanguage = HoneyLanguage.getCodeBySymbol(CountryCode.currentLanguageSymbol)
				AppConfigTable.insertAppConfig(callback)
			} else {
				// 如果之前因为失败原因 `netWork`, `Server` 等注册地址失败, 在这里检测并重新注册
				if (config.isRegisteredAddresses) {
					val currentWallet =
						GoldStoneDataBase.database.walletDao().findWhichIsUsing(true)
					XinGePushReceiver.registerAddressesForPush(currentWallet)
				}
				config.let(callback)
			}
		}
	}

	/**
	 * Querying the language type of the current account
	 * set and displaying the interface from the database.
	 */
	private fun initLaunchLanguage(code: Int) {
		currentLanguage = code
		SharedWallet.updateCurrentLanguageCode(code)
	}

	private fun ViewGroup.initWaveView() {
		waveView.apply {
			setShapeType(WaveLoadingView.ShapeType.RECTANGLE)
			progressValue = 35
			waveColor = Color.parseColor("#FF19769D")
			setAnimDuration(30000)
			setAmplitudeRatio(30)
			startAnimation()
		}.into(this)
	}
}