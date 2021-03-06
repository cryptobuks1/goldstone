package io.goldstone.blinnnk.module.home.dapp.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import com.blinnnk.extension.isNotNull
import com.blinnnk.extension.safeGet
import com.blinnnk.extension.toIntOrZero
import com.blinnnk.extension.toJSONObjectList
import com.blinnnk.util.SystemUtils
import com.blinnnk.util.load
import com.blinnnk.util.then
import io.goldstone.blinnnk.GoldStoneApp
import io.goldstone.blinnnk.common.component.overlay.Dashboard
import io.goldstone.blinnnk.common.component.overlay.LoadingView
import io.goldstone.blinnnk.common.language.CommonText
import io.goldstone.blinnnk.common.language.TransactionText
import io.goldstone.blinnnk.common.sharedpreference.SharedAddress
import io.goldstone.blinnnk.common.sharedpreference.SharedChain
import io.goldstone.blinnnk.common.sharedpreference.SharedValue
import io.goldstone.blinnnk.common.sharedpreference.SharedWallet
import io.goldstone.blinnnk.common.thread.launchUI
import io.goldstone.blinnnk.common.utils.AesCrypto
import io.goldstone.blinnnk.common.utils.ErrorDisplayManager
import io.goldstone.blinnnk.crypto.eos.EOSCodeName
import io.goldstone.blinnnk.crypto.eos.EOSTransactionMethod
import io.goldstone.blinnnk.crypto.eos.account.EOSAccount
import io.goldstone.blinnnk.crypto.eos.account.EOSPrivateKey
import io.goldstone.blinnnk.crypto.eos.base.showDialog
import io.goldstone.blinnnk.crypto.eos.contract.EOSContractCaller
import io.goldstone.blinnnk.crypto.eos.ecc.Sha256
import io.goldstone.blinnnk.crypto.multichain.*
import io.goldstone.blinnnk.kernel.commontable.MyTokenTable
import io.goldstone.blinnnk.kernel.network.common.RequisitionUtil
import io.goldstone.blinnnk.kernel.network.eos.EOSAPI
import io.goldstone.blinnnk.kernel.network.eos.EOSAPI.getStringAccountInfo
import io.goldstone.blinnnk.module.common.tokendetail.eosactivation.accountselection.model.EOSAccountTable
import io.goldstone.blinnnk.module.common.tokenpayment.paymentdetail.presenter.PaymentDetailPresenter
import io.goldstone.blinnnk.module.common.tokenpayment.paymentdetail.presenter.PrivatekeyActionType
import io.goldstone.blinnnk.module.home.profile.profile.presenter.ProfilePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.matchParent
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author KaySaith
 * @date  2018/11/29
 * @Important
 *  兼容 Scatter 的 Dapp 在 地域 API 26 的机器上都无法运行 (除非更新 Chrome )
 */
@Suppress("DEPRECATION")
@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class DAPPBrowser(
	context: Context,
	url: String,
	hold: (progress: Int) -> Unit) : WebView(context
) {
	private val loadingView = LoadingView(context)
	private val jsInterface = JSInterface()
	private val account = SharedAddress.getCurrentEOSAccount()
	private val chainID = SharedChain.getEOSCurrent().chainID

	init {
		webViewClient = WebViewClient()
		settings.javaScriptEnabled = true
		addJavascriptInterface(jsInterface, "control")
		settings.javaScriptCanOpenWindowsAutomatically = true
		settings.domStorageEnabled = true
		settings.databaseEnabled = true
		settings.allowFileAccess = true
		settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
		settings.useWideViewPort = true
		settings.loadWithOverviewMode = true
		setLayerType(View.LAYER_TYPE_HARDWARE, null)
		settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
		settings.setAppCacheEnabled(true)
		settings.setAppCacheMaxSize(15 * 1024 * 1024)
		settings.cacheMode = WebSettings.LOAD_DEFAULT
		this.loadUrl(url)
		layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
		webChromeClient = object : WebChromeClient() {
			override fun onProgressChanged(view: WebView?, newProgress: Int) {
				super.onProgressChanged(view, newProgress)
				hold(newProgress)
				fun evaluateJS() {
					view?.evaluateJavascript("javascript:(function(){" +
						"${SharedValue.getJSCode()};" +
						"event=document.createEvent('HTMLEvents');" +
						"event.initEvent('scatterLoaded',true,true);" +
						"document.dispatchEvent(event);" +
						"})()", null)
				}
				evaluateJS()
				if (newProgress == 100) {
					evaluateJS() // for totally
				}
			}

			override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
				println("GoldStone-DAPP-Browser: ${consoleMessage?.message()}")
				return super.onConsoleMessage(consoleMessage)
			}
		}
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		// 销毁的时候清理用于接收 Promise 而设定的 Interval
		evaluateJavascript("javascript:(function(){clearInterval(window.scatter.interval);})()", null)
		destroy()
	}

	fun refresh() {
		evaluateJavascript("javascript:window.location.reload( true )", null)
	}

	fun backEvent(callback: () -> Unit) {
		jsInterface.backEvent(callback)
	}

	inner class JSInterface {

		/**
		 * Scatter 合约的方法, 有传回 `Code` 这里目前暂时只支持查询了 `EOS Balance`
		 * @Description
		 * 目前还遇到有些 `DAPP` 调用 `Scatter` 的这个方法完全不传参数, 那么就直接返回
		 * 当前用户的 `AccountName`
		 */
		@JavascriptInterface
		fun getEOSAccountBalance(code: String, accountName: String, symbol: String) {
			val finalCode: String
			val finalAccount: String
			val finalSymbol: CoinSymbol
			if (code == "undefined" && symbol == "undefined") {
				finalCode = EOSCodeName.EOSIOToken.value
				finalAccount = account.name
				finalSymbol = CoinSymbol.EOS
			} else {
				finalCode = code
				finalAccount = accountName
				finalSymbol = CoinSymbol(symbol)
			}
			EOSAPI.getCurrencyBalance(
				EOSAccount(finalAccount),
				finalSymbol,
				finalCode
			) { balance, error ->
				launchUI {
					if (balance.isNotNull() && error.isNone()) {
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getCurrencyBalanceEvent',true,true);event.data=$balance;document.dispatchEvent(event)})()", null)
					} else {
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getCurrencyBalanceEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
					}
				}
			}
		}

		@JavascriptInterface
		fun getIdentity(data: String) {
			println("getIdentity $data")
			GlobalScope.launch(Dispatchers.Default) {
				val identity = "{ accounts: [{ \"authority\": \"active\", \"blockchain\": \"eos\", \"name\": \"${account.name}\" }] }"
				delay(1000L)
				launchUI {
					evaluateJavascript("javascript:(function(){scatter.getIdentityResult=${JSONObject(identity)}})()", null)
				}
			}
		}

		/** *
		 * 这里遇到了有些 DAPP 传入的是  {"account_name":"beautifulleo"}
		 * 有些直接传入了 "\"beautifulleo\""
		 */
		@JavascriptInterface
		fun getEOSAccountInfo(account: String) {
			val accountName =
				if (account.contains("{")) JSONObject(account).safeGet("account_name")
				else account.replace("\"", "")
			// Scatter 合约的方法, 有传回 `Code` 这里目前暂时只支持查询了 `EOS Balance`
			getStringAccountInfo(EOSAccount(accountName)) { accountInfo, error ->
				launchUI {
					if (accountInfo.isNotNull() && error.isNone()) {
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getAccountEvent',true,true);event.data=$accountInfo;document.dispatchEvent(event)})()", null)
					}
				}
			}
		}

		@JavascriptInterface
		fun getEOSAccountPermissions() {
			load {
				EOSAccountTable.getPermissions(account, chainID)
			} then { permissions ->
				val list = "[${permissions.joinToString(",") { it.generateObject() }}]"
				callWeb("getPermissions", list)
			}
		}

		@JavascriptInterface
		fun getTableRows(data: String) {
			launchUI {
				val tableObject = try {
					JSONObject(data)
				} catch (error: Exception) {
					evaluateJavascript("javascript:(function(){scatter.tableRow=\"failed\"})()", null)
					println("GoldStone-DAPP Get Table Row ERROR: ${error.message}\n DATA: $data")
					return@launchUI
				}
				val option =
					if (tableObject.safeGet("lower_bound").isNotEmpty()) Pair("lower_bound", tableObject.safeGet("lower_bound"))
					else null
				val limit =
					if (tableObject.safeGet("limit").isNotEmpty()) Pair("limit", tableObject.safeGet("limit").toIntOrZero())
					else null

				val indexPosition =
					if (tableObject.safeGet("index_position").isNotEmpty()) Pair("index_position", tableObject.safeGet("index_position").toIntOrZero())
					else null

				val keyType =
					if (tableObject.safeGet("key_type").isNotEmpty()) Pair("key_type", tableObject.safeGet("key_type"))
					else null

				EOSAPI.getTableRows(
					tableObject.safeGet("scope"),
					tableObject.safeGet("code"),
					tableObject.safeGet("table"),
					option,
					limit,
					indexPosition,
					keyType
				) { result, error ->
					launchUI {
						if (result.isNotNull() && error.isNone()) {
							evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getTableRowsEvent',true,true);event.data=$result;document.dispatchEvent(event)})()", null)
						}
					}
				}
			}
		}

		@JavascriptInterface
		fun getArbSignature(data: String) {
			launchUI {
				Dashboard(context) {
					showAlert(
						TransactionText.signData,
						TransactionText.signDataDescription
					) {
						PaymentDetailPresenter.getPrivatekey(
							context,
							ChainType.EOS,
							PrivatekeyActionType.SignData,
							cancelEvent = {
								evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getArbSignatureEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
							},
							confirmEvent = {
								loadingView.show()
							}
						) { privateKey, error ->
							launchUI {
								loadingView.remove()
								if (privateKey.isNotNull() && error.isNone()) {
									val signature = EOSPrivateKey(privateKey).sign(Sha256.from(data.toByteArray())).toString()
									evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getArbSignatureEvent',true,true);event.data=\"$signature\";document.dispatchEvent(event)})()", null)
								} else {
									evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getArbSignatureEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
								}
							}
						}
					}
				}
			}
		}

		@JavascriptInterface
		fun getInfo() {
			EOSAPI.getStringChainInfo { chainInfo, error ->
				launchUI {
					if (chainInfo.isNotNull() && error.isNone()) {
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getInfo',true,true);event.data=$chainInfo;document.dispatchEvent(event)})()", null)
					} else {
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('getInfo',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
					}
				}
			}
		}

		@JavascriptInterface
		fun transferEOS(action: String) {
			launchUI {
				val actions = try {
					JSONArray(JSONObject(action).safeGet("actions")).toJSONObjectList()
				} catch (error: Exception) {
					println("GoldStone-DAPP Transfer EOS ERROR: $error\n DATA: $action")
					return@launchUI
				}
				if (actions[0].safeGet("name").equals(EOSTransactionMethod.Transfer.value, true)) {
					scatterEOSTransaction(actions)
				} else {
					scatterSignOperation(actions[0])
				}
			}
		}

		private val scatterSignOperation: (action: JSONObject) -> Unit = { action ->
			showOperationDashboard(
				action,
				cancelEvent = {
					loadingView.remove()
					evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
				},
				confirmEvent = {
					PaymentDetailPresenter.getPrivatekey(
						context,
						ChainType.EOS,
						PrivatekeyActionType.SignData,
						cancelEvent = {
							loadingView.remove()
							evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
						},
						confirmEvent = { loadingView.show() }
					) { privateKey, error ->
						if (privateKey.isNotNull() && error.isNone()) {
							EOSContractCaller(action, ChainID.EOS).send(
								EOSPrivateKey(privateKey),
								SharedChain.getEOSCurrent().getURL()
							) { response, pushTransactionError ->
								launchUI {
									loadingView.remove()
									if (response.isNotNull() && pushTransactionError.isNone()) {
										response.showDialog(context)
										evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=${response.result};document.dispatchEvent(event)})()", null)
									} else {
										evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
										ErrorDisplayManager(pushTransactionError).show(context)
									}
								}
							}
						} else launchUI {
							loadingView.remove()
						}
					}
				}
			)
		}

		private val scatterEOSTransaction: (action: List<JSONObject>) -> Unit = { action ->
			showQuickPaymentDashboard(
				action,
				false,
				"",
				cancelEvent = {
					evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
					loadingView.remove()
				},
				confirmEvent = { loadingView.show() }
			) { response, error ->
				launchUI {
					loadingView.remove()
					if (response.isNotNull() && error.isNone()) {
						response.showDialog(context)
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=${response.result};document.dispatchEvent(event)})()", null)
					} else {
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
						if (error.hasError()) ErrorDisplayManager(error).show(context)
					}
				}
			}
		}

		/**
		 * action like {"from":"beautifulleo","to":"betlottoinst","quantity":"0.1000 EOS","memo":"1|1029338"}
		 * @Description
		 * 因为某些 DAPP 要求使用 DAPP 自己的指定节点进行访问, 这里预留了传入自定义 ChainURL 的方法
		 * 等待后续确定方案再实施
		 */
		@JavascriptInterface
		fun simpleTransfer(action: String) {
			// TODO Description DAPP Custom ChainURL
			val dappChainURL = SharedChain.getEOSCurrent().getURL()
			launchUI {
				val actionObject = try {
					JSONObject(action)
				} catch (error: Exception) {
					evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
					println("GoldStone-DAPP Transfer EOS ERROR: $error\n DATA: $action")
					return@launchUI
				}
				showQuickPaymentDashboard(
					listOf(actionObject),
					true,
					dappChainURL,
					cancelEvent = {
						evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
					},
					confirmEvent = { loadingView.show() }
				) { response, error ->
					launchUI {
						loadingView.remove()
						if (response.isNotNull() && error.isNone()) {
							response.showDialog(context)
							evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=${response.result};document.dispatchEvent(event)})()", null)
						} else {
							ErrorDisplayManager(error).show(context)
							evaluateJavascript("javascript:(function(){var event=document.createEvent('Event');event.initEvent('transactionEvent',true,true);event.data=\"failed\";document.dispatchEvent(event)})()", null)
						}
					}
				}
			}
		}

		/**
		 * @Important
		 * 所有 `JSInterface` 的线程发起都在  `Thread JSInterFace` 线程, 所以需要
		 * 在发起的时候就进行 `UI `展示的时候需要首先声明为 `UI` 线程.
		 */
		@JavascriptInterface
		fun toastMessage(message: String) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show()
		}

		@JavascriptInterface
		fun alert(title: String, message: String) {
			launchUI {
				Dashboard(context!!) {
					showAlert(
						title,
						message,
						CommonText.confirm,
						cancelAction = { dismiss() }
					) {
						evaluateJavascript("javascript:showAlert(\"clickedConfirmButton\")", null)
					}
				}
			}
		}

		@JavascriptInterface
		fun getChainID(chainType: Int) {
			load {
				ChainType(chainType)
			} then {
				evaluateJavascript("javascript:getChainID(\"${it.getChainURL().chainID.id}\")", null)
			}
		}

		@JavascriptInterface
		fun getGoldStoneID() {
			load {
				SharedWallet.getGoldStoneID()
			} then {
				evaluateJavascript("javascript:getGoldStoneID(\"$it\")", null)
			}
		}

		@JavascriptInterface
		fun getLanguageCode() {
			load {
				SharedWallet.getCurrentLanguageCode()
			} then {
				evaluateJavascript("javascript:getLanguageCode(\"$it\")", null)
			}
		}

		@JavascriptInterface
		fun getVersionName() {
			load {
				SystemUtils.getVersionCode(context)
			} then {
				callWeb("getVersionName", "$it")
			}
		}

		@JavascriptInterface
		fun encrypt(data: String) {
			load {
				AesCrypto.encrypt(data).orEmpty()
			} then { cryptoData ->
				// 直接返回不成功, 不知道为什么,  这里转换一下就好了
				val result = cryptoData.substring(0, cryptoData.lastIndex)
				evaluateJavascript("javascript:encrypt(\"$result\")", null)
			}
		}

		@JavascriptInterface
		fun decrypt(data: String) {
			load {
				AesCrypto.decrypt(data).orEmpty()
			} then { decryptData ->
				evaluateJavascript("javascript:decrypt(\"${Uri.encode(decryptData)}\")", null)
			}
		}

		@JavascriptInterface
		fun getSignHeader(timeStamp: String) {
			load {
				val goldStoneID = SharedWallet.getGoldStoneID()
				val version = SystemUtils.getVersionCode(GoldStoneApp.appContext).toString()
				RequisitionUtil.getSignHeader(goldStoneID, timeStamp, version)
			} then { signData ->
				evaluateJavascript("javascript:getSignHeader(\"$signData\")", null)
			}
		}

		@JavascriptInterface
		fun getAccountAddress(contract: String, symbol: String, isEOSAccountName: Boolean) {
			load {
				TokenContract(contract, symbol, null).getAddress(isEOSAccountName)
			} then {
				evaluateJavascript("javascript:getAccountAddress(\"$it\")", null)
			}
		}

		@JavascriptInterface
		fun getBalance(contract: String, symbol: String) {
			launchUI {
				val tokenContract = TokenContract(contract, symbol, null)
				loadingView.show()
				MyTokenTable.getBalanceByContract(tokenContract) { balance, error ->
					launchUI {
						loadingView.remove()
						if (balance.isNotNull() && error.isNone()) {
							evaluateJavascript("javascript:getBalance(\"${Uri.encode(balance.toString())}\")", null)
						} else {
							evaluateJavascript("javascript:getBalance(\"${Uri.encode(error.message)}\")", null)
						}
					}
				}
			}
		}

		@JavascriptInterface
		fun getEOSSingedData(data: String) {
			launchUI {
				Dashboard(context) {
					showAlert(
						TransactionText.signData,
						TransactionText.signDataDescription
					) {
						PaymentDetailPresenter.getPrivatekey(
							context,
							ChainType.EOS,
							PrivatekeyActionType.SignData,
							cancelEvent = { loadingView.remove() },
							confirmEvent = { loadingView.show() }
						) { privateKey, error ->
							if (privateKey.isNotNull() && error.isNone()) {
								EOSContractCaller(JSONObject(data)).getPushTransactionObject(EOSPrivateKey(privateKey)) { pushJson, hashError ->
									launchUI {
										loadingView.remove()
										if (pushJson.isNotNull() && hashError.isNone()) {
											val result = Uri.encode("{\"signedData\": $pushJson, \"error\": \"${hashError.message}\"}")
											evaluateJavascript("javascript:getEOSSignedData(\"$result\")", null)
										} else {
											val result = Uri.encode("{\"signedData\": \"undefined\", \"error\": \"${hashError.message}\"}")
											evaluateJavascript("javascript:getEOSSignedData(\"$result\")", null)
										}
									}
								}
							} else launchUI {
								loadingView.remove()
								val result = Uri.encode("{\"signedData\":  \"undefined\", \"error\": \"${error.message}\"}")
								evaluateJavascript("javascript:getEOSSignedData(\"$result\")", null)
							}
						}
					}
				}
			}
		}

		@JavascriptInterface
		fun showShareDashboard(shareContent: String) {
			launchUI {
				ProfilePresenter.showShareChooser(context!!, shareContent)
			}
		}

		@JavascriptInterface
		fun getCurrentWalletType() {
			launchUI {
				val type = SharedWallet.getCurrentWalletType().type
				evaluateJavascript("javascript:getCurrentWalletType(\"$type\")", null)
			}
		}

		@JavascriptInterface
		fun backEvent(callback: () -> Unit) {
			evaluateJavascript("javascript:backEvent()") {
				if (it.equals("\"finished\"", true)) callback()
				else callback()
			}
		}
	}
}

fun WebView.callWeb(methodName: String, value: String) {
	return evaluateJavascript("javascript:$methodName(\"${Uri.encode(value)}\")", null)
}