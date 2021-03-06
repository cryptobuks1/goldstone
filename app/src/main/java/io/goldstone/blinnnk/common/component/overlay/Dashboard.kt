package io.goldstone.blinnnk.common.component.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.GridLayoutManager
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.getRecyclerView
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.blinnnk.base.HoneyBaseAdapter
import com.blinnnk.extension.isNotNull
import com.blinnnk.uikit.uiPX
import io.goldstone.blinnnk.common.language.CommonText
import io.goldstone.blinnnk.common.value.*
import io.goldstone.blinnnk.common.value.ScreenSize
import org.jetbrains.anko.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNCHECKED_CAST")
@SuppressLint("ViewConstructor")
/**
 * @date 2018/7/11 1:32 PM
 * @author KaySaith
 */

class Dashboard(private val context: Context, hold: Dashboard.() -> Unit) {
	private var dialog: MaterialDialog? = null
	init {
		dialog = MaterialDialog(context)
		dialog?.window?.setLayout(ScreenSize.overlayContentWidth, wrapContent)
		val shape = GradientDrawable().apply {
			cornerRadius = CornerSize.small
			shape = GradientDrawable.RECTANGLE
			setSize(matchParent, matchParent)
			setColor(Spectrum.white)
		}
		dialog?.window?.setBackgroundDrawable(shape)
		hold(this)
	}

	fun dismiss() {
		dialog?.dismiss()
		dialog = null
	}

	// 安全非空的 Dialog
	fun getDialog(hold: MaterialDialog.() -> Unit) {
		dialog?.let(hold)
	}

	fun cancelOnTouchOutside() {
		dialog?.cancelOnTouchOutside(false)
	}

	fun <T, C : View> showList(title: String, adapter: HoneyBaseAdapter<T, C>) {
		getDialog {
			title(text = title)
			customListAdapter(adapter)
			negativeButton(text = CommonText.gotIt)
			show()
		}
	}

	fun <T, C : View> showGrid(title: String, adapter: HoneyBaseAdapter<T, C>) {
		getDialog {
			title(text = title)
			customListAdapter(adapter)
			getRecyclerView()?.layoutManager =
				GridLayoutManager(context, 3, 1, false)
			negativeButton(text = CommonText.cancel)
			show()
		}
	}

	fun <T : View> showDashboard(
		title: String,
		message: String,
		customView: T,
		hold: (T) -> Unit,
		cancelAction: () -> Unit
	) {
		getDialog {
			cancelOnTouchOutside(false)
			title(text = title)
			message(text = message)
			customView(view = customView)
			if (hold.isNotNull()) {
				positiveButton(text = CommonText.confirm) {
					hold(customView)
					dialog?.dismiss()
				}
			}
			negativeButton(text = CommonText.cancel) {
				cancelAction()
			}
			show()
		}
	}

	fun <T : View> showAttentionDashboard(
		title: String,
		message: String,
		customView: T,
		cancelAction: () -> Unit
	) {
		getDialog {
			cancelOnTouchOutside(false)
			title(text = title)
			message(text = message)
			customView(view = customView)
			negativeButton(text = CommonText.cancel) {
				cancelAction()
			}
			show()
		}
	}

	fun showAlert(
		title: String,
		message: String,
		positiveButtonTitle: String = CommonText.confirm,
		cancelAction: () -> Unit = {},
		confirmAction: () -> Unit
	) {
		getDialog {
			title(text = title)
			message(text = message)
			positiveButton(text = positiveButtonTitle) {
				confirmAction()
			}
			negativeButton(text = CommonText.cancel) {
				cancelAction()
			}
			show()
		}
	}

	fun showForceAlert(
		title: String,
		message: String,
		positiveButtonTitle: String = CommonText.confirm,
		confirmAction: () -> Unit
	) {
		cancelOnTouchOutside()
		getDialog {
			title(text = title)
			message(text = message)
			positiveButton(text = positiveButtonTitle) {
				confirmAction()
			}
			negativeButton(null, "", null)
			show()
		}
	}

	fun showMultiChoice(
		title: String,
		data: List<String>,
		defaultIndexes: IntArray,
		confirmAction: (List<String>) -> Unit
	) {
		var selectedItems = listOf<String>()
		getDialog {
			title(text = title)
			listItemsMultiChoice(
				items = data,
				initialSelection = defaultIndexes,
				waitForPositiveButton = false
			) { _, _, items ->
				selectedItems = items
				dialog?.setActionButtonEnabled(WhichButton.POSITIVE, items.isNotEmpty())
			}
			positiveButton(text = CommonText.confirm) {
				confirmAction(selectedItems)
			}
			negativeButton(text = CommonText.cancel)
			show()
		}
	}

	fun showAlertView(
		title: String,
		subtitle: String,
		showEditText: Boolean = true,
		cancelAction: () -> Unit = {},
		action: (EditText?) -> Unit
	) {
		with(context) {
			val input = linearLayout {
				lparams(matchParent, matchParent)
				setPadding(20.uiPX(), 10.uiPX(), 20.uiPX(), 20.uiPX())
				editText {
					id = ElementID.passwordInput
					layoutParams = LinearLayout.LayoutParams(matchParent, 50.uiPX())
					hintTextColor = GrayScale.Opacity1Black
					textColor = Spectrum.blue
					inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
					hint = CommonText.enterPassword
					textSize = fontSize(14)
					background.mutate().setColorFilter(Spectrum.blue, PorterDuff.Mode.SRC_ATOP)
				}
			}
			if (showEditText) showDashboard(
				title,
				subtitle,
				input,
				{ action(it.findViewById(ElementID.passwordInput)) },
				cancelAction
			)
			else showAlert(title, subtitle, CommonText.confirm, cancelAction) {
				action(null)
			}
		}
	}
}