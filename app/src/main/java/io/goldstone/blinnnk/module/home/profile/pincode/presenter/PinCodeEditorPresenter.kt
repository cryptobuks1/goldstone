package io.goldstone.blinnnk.module.home.profile.pincode.presenter

import android.widget.EditText
import android.widget.Switch
import com.blinnnk.extension.isNull
import com.blinnnk.util.load
import com.blinnnk.util.then
import io.goldstone.blinnnk.common.base.basefragment.BasePresenter
import io.goldstone.blinnnk.common.language.CommonText
import io.goldstone.blinnnk.common.language.PincodeText
import io.goldstone.blinnnk.common.sharedpreference.SharedValue
import io.goldstone.blinnnk.common.utils.alert
import io.goldstone.blinnnk.common.value.Count
import io.goldstone.blinnnk.kernel.commontable.AppConfigTable
import io.goldstone.blinnnk.module.home.profile.pincode.view.PinCodeEditorFragment
import kotlinx.coroutines.Dispatchers

/**
 * @date 23/04/2018 2:34 PM
 * @author KaySaith
 */
class PinCodeEditorPresenter(
	override val fragment: PinCodeEditorFragment
) : BasePresenter<PinCodeEditorFragment>() {

	fun setPinCodeDisplayStatus(status: Boolean, callback: (isShow: Boolean) -> Unit) {
		AppConfigTable.getAppConfig(Dispatchers.Main) { config ->
			if (config?.pincode.isNull()) {
				fragment.context?.alert(PincodeText.turnOnAttention)
				callback(false)
			} else AppConfigTable.setShowPinCodeStatus(status) {
				callback(it)
				SharedValue.updatePincodeDisplayStatus(it)
			}
		}
	}

	fun resetPinCode(
		newPinCode: EditText,
		repeatPinCode: EditText,
		switch: Switch
	) {
		if (newPinCode.text.isEmpty()) {
			fragment.context?.alert(PincodeText.countAlert)
			return
		}

		if (newPinCode.text.length > Count.pinCode || repeatPinCode.text.length > Count.pinCode) {
			fragment.context?.alert(PincodeText.countAlert)
			return
		}

		if (newPinCode.text.toString() != repeatPinCode.text.toString()) {
			fragment.context?.alert(PincodeText.verifyAlert)
			return
		}
		load {
			AppConfigTable.dao.updatePincode(newPinCode.text.toString().toInt())
		} then {
			fragment.context?.alert(CommonText.succeed)
			setPinCodeDisplayStatus(true) { hasSet ->
				switch.isChecked = hasSet
				SharedValue.updatePincodeDisplayStatus(hasSet)
			}
		}
	}
}