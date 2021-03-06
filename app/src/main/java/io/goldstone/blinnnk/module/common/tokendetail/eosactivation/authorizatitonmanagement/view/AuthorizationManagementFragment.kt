package io.goldstone.blinnnk.module.common.tokendetail.eosactivation.authorizatitonmanagement.view

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.blinnnk.extension.*
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.getParentFragment
import io.goldstone.blinnnk.common.base.basecell.BaseRadioCell
import io.goldstone.blinnnk.common.base.basecell.radioCell
import io.goldstone.blinnnk.common.base.baserecyclerfragment.BaseRecyclerView
import io.goldstone.blinnnk.common.base.gsfragment.GSRecyclerFragment
import io.goldstone.blinnnk.common.component.cell.graySquareCell
import io.goldstone.blinnnk.common.component.edittext.WalletEditText
import io.goldstone.blinnnk.common.component.overlay.Dashboard
import io.goldstone.blinnnk.common.component.overlay.LoadingView
import io.goldstone.blinnnk.common.error.AccountError
import io.goldstone.blinnnk.common.language.EOSAccountText
import io.goldstone.blinnnk.common.sharedpreference.SharedAddress
import io.goldstone.blinnnk.common.thread.launchUI
import io.goldstone.blinnnk.common.utils.ErrorDisplayManager
import io.goldstone.blinnnk.common.utils.click
import io.goldstone.blinnnk.common.value.PaddingSize
import io.goldstone.blinnnk.crypto.eos.EOSWalletUtils
import io.goldstone.blinnnk.crypto.eos.account.EOSPrivateKey
import io.goldstone.blinnnk.crypto.eos.accountregister.EOSActor
import io.goldstone.blinnnk.crypto.eos.base.showDialog
import io.goldstone.blinnnk.crypto.multichain.ChainType
import io.goldstone.blinnnk.module.common.tokendetail.eosactivation.authorizatitonmanagement.contract.AuthorizationManagementContract
import io.goldstone.blinnnk.module.common.tokendetail.eosactivation.authorizatitonmanagement.model.AuthorizationManagementModel
import io.goldstone.blinnnk.module.common.tokendetail.eosactivation.authorizatitonmanagement.model.AuthorizationObserverModel
import io.goldstone.blinnnk.module.common.tokendetail.eosactivation.authorizatitonmanagement.model.AuthorizationType
import io.goldstone.blinnnk.module.common.tokendetail.eosactivation.authorizatitonmanagement.presenter.AuthorizationManagementPresenter
import io.goldstone.blinnnk.module.common.tokendetail.tokendetailoverlay.view.TokenDetailOverlayFragment
import io.goldstone.blinnnk.module.common.tokenpayment.paymentdetail.presenter.PaymentDetailPresenter
import io.goldstone.blinnnk.module.common.tokenpayment.paymentdetail.presenter.PrivatekeyActionType
import io.goldstone.blinnnk.module.entrance.splash.view.SplashActivity
import io.goldstone.blinnnk.module.home.home.view.MainActivity
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.wrapContent


/**
 * @author KaySaith
 * @date  2018/12/26
 */
class AuthorizationManagementFragment
	: GSRecyclerFragment<AuthorizationManagementModel>(), AuthorizationManagementContract.GSView {

	override val pageTitle: String = EOSAccountText.permissionManagement
	private val overlayFragment by lazy {
		getParentFragment<TokenDetailOverlayFragment>()
	}
	private val loadingView by lazy {
		LoadingView(context!!)
	}

	override fun setRecyclerViewAdapter(
		recyclerView: BaseRecyclerView,
		asyncData: ArrayList<AuthorizationManagementModel>?
	) {
		recyclerView.adapter = AuthorizationManagementAdapter(
			asyncData.orEmptyArray(),
			editAction = { publicKey, actor ->
				updatePermissionKey(publicKey, actor, AuthorizationType.Edit)
			},
			deleteAction = { publickey, actor ->
				presenter.showAlertBeforeDeleteLastKey(publickey) { isFinalDelete ->
					if (isFinalDelete) Dashboard(context!!) {
						showAlert(
							EOSAccountText.lostPermissionAlertTitle,
							EOSAccountText.lostPermissionAlertDescription,
							confirmAction = {
								deletePermissionKey(publickey, actor, isFinalDelete)
							}
						)
					} else deletePermissionKey(publickey, actor, isFinalDelete)
				}
			}
		)
	}

	override lateinit var presenter: AuthorizationManagementContract.GSPresenter

	override fun showError(error: Throwable) {
		ErrorDisplayManager(error).show(context)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		asyncData = arrayListOf()
		overlayFragment?.showAddButton(true, false) {
			updatePermissionKey("", EOSActor.Active, AuthorizationType.Add)
		}
		presenter = AuthorizationManagementPresenter(this)
		presenter.start()
	}

	override fun updateData(data: ArrayList<AuthorizationManagementModel>) = launchUI {
		updateAdapterDataSet<AuthorizationManagementAdapter>(data)
	}

	override fun setBackEvent(mainActivity: MainActivity?) {
		overlayFragment?.presenter?.popFragmentFrom<AuthorizationManagementFragment>()
	}

	private fun deletePermissionKey(defaultPublicKey: String, actor: EOSActor, isFinalDelete: Boolean) {
		Dashboard(context!!) {
			showAlert(
				EOSAccountText.deleteCurrentLastPermissionAlertTitle,
				EOSAccountText.deleteCurrentLastPermissionAlertDescription,
				cancelAction = {
					dismiss()
				},
				confirmAction = {
					PaymentDetailPresenter.getPrivatekey(
						context!!,
						ChainType.EOS,
						PrivatekeyActionType.SignData,
						cancelEvent = { loadingView.remove() },
						confirmEvent = { loadingView.show() }
					) { privateKey, error ->
						launchUI {
							loadingView.remove()
							if (privateKey.isNotNull() && error.isNone()) {
								presenter.updateAuthorization(
									defaultPublicKey,
									"",
									actor,
									EOSPrivateKey(privateKey),
									AuthorizationType.Delete
								) { response, responseError ->
									dismiss()
									launchUI {
										if (response.isNotNull() && responseError.isNone()) {
											response.showDialog(context!!)
											if (isFinalDelete) Dashboard(context!!) {
												showForceAlert(
													EOSAccountText.lostPermissionAlertTitle,
													EOSAccountText.lostPermissionAlertDescription,
													confirmAction = {
														presenter.deleteAccount { activity?.jump<SplashActivity>() }
													}
												)
											} else presenter.refreshAuthList(
												AuthorizationObserverModel(defaultPublicKey, true)
											)
										} else showError(responseError)
									}
								}
							} else showError(error)
						}
					}
				}
			)
		}
	}

	private fun updatePermissionKey(
		defaultPublickey: String,
		actor: EOSActor,
		actionType: AuthorizationType
	) {
		showAddKeyDashboard(defaultPublickey, actor) { publicKey, eosActor ->
			PaymentDetailPresenter.getPrivatekey(
				context!!,
				ChainType.EOS,
				PrivatekeyActionType.SignData,
				cancelEvent = {
					loadingView.remove()
				},
				confirmEvent = {
					loadingView.show()
				}
			) { privateKey, error ->
				launchUI {
					loadingView.remove()
					if (privateKey.isNotNull() && error.isNone()) {
						presenter.updateAuthorization(
							publicKey,
							defaultPublickey,
							eosActor,
							EOSPrivateKey(privateKey),
							actionType
						) { response, responseError ->
							launchUI {
								if (response.isNotNull() && responseError.isNone()) {
									response.showDialog(context!!)
									presenter.refreshAuthList(AuthorizationObserverModel(publicKey, false))
								} else showError(responseError)
							}
						}
					} else showError(error)
				}
			}
		}
	}

	private fun showAddKeyDashboard(
		publicKey: String,
		actor: EOSActor,
		confirmAction: (publicKey: String, actor: EOSActor) -> Unit
	) {
		var selectedPermission = EOSActor.Active
		val activeCell: BaseRadioCell
		val ownerCell: BaseRadioCell
		val keyInput: WalletEditText
		val editorView = LinearLayout(context).apply {
			orientation = LinearLayout.VERTICAL
			layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
			padding = PaddingSize.content
			graySquareCell {
				layoutParams = LinearLayout.LayoutParams(matchParent, 45.uiPX())
				setTitle(EOSAccountText.account)
				setSubtitle(SharedAddress.getCurrentEOSAccount().name)
			}
			keyInput = WalletEditText(context).apply {
				hint = if (publicKey.isNotEmpty()) publicKey else EOSAccountText.enterPublicKey
				layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
			}
			keyInput.into(this)
			keyInput.setMargins<LinearLayout.LayoutParams> {
				topMargin = 15.uiPX()
				bottomMargin = 15.uiPX()
			}

			ownerCell = radioCell {
				setTitle(EOSActor.Owner.displayName)
			}

			activeCell = radioCell {
				setTitle(EOSActor.Active.displayName)
				setSwitchStatusBy(true)
			}

			ownerCell.click {
				activeCell.setSwitchStatusBy(false)
				it.setSwitchStatusBy(true)
				selectedPermission = EOSActor.Owner
			}

			if (actor.isOwner()) {
				activeCell.setSwitchStatusBy(false)
				ownerCell.setSwitchStatusBy(true)
				selectedPermission = EOSActor.Owner
			}

			activeCell.click {
				ownerCell.setSwitchStatusBy(false)
				it.setSwitchStatusBy(true)
				selectedPermission = EOSActor.Active
			}
		}

		Dashboard(context!!) {
			showDashboard(
				EOSAccountText.addNewPermissionTitle,
				EOSAccountText.addNewPermissionDescription,
				editorView,
				hold = {
					if (!EOSWalletUtils.isValidAddress(keyInput.getContent())) {
						showError(AccountError.InvalidAddress)
					} else {
						presenter.checkIsExistedOrElse(keyInput.getContent(), selectedPermission) { existedInChain ->
							launchUI {
								if (existedInChain) {
									showError(Throwable(EOSAccountText.duplicatedKey(selectedPermission.toString())))
								} else confirmAction(keyInput.getContent(), selectedPermission)
							}
						}
					}
					dismiss()
				}
			) {
				dismiss()
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		overlayFragment?.showAddButton(false) {}
		presenter.removeObserver()
	}
}