package io.goldstone.blockchain.module.common.tokenpayment.addressselection.view

import android.content.Context
import android.view.View
import com.blinnnk.base.HoneyBaseAdapterWithHeaderAndFooter
import io.goldstone.blockchain.module.home.profile.contacts.model.ContactsModel

/**
 * @date 28/03/2018 9:25 AM
 * @author KaySaith
 */

class AddressSelectionAdapter(
  override val dataSet: ArrayList<ContactsModel>,
  private val holdCell: AddressSelectionCell.() -> Unit
  ) : HoneyBaseAdapterWithHeaderAndFooter<ContactsModel, AddressSelectionHeaderView, AddressSelectionCell, View>() {

  override fun generateHeader(context: Context) = AddressSelectionHeaderView(context)

  override fun generateCell(context: Context) = AddressSelectionCell(context)

  override fun generateFooter(context: Context) = View(context)

  override fun AddressSelectionCell.bindCell(data: ContactsModel, position: Int) {
    model = data
    holdCell(this)
  }
}