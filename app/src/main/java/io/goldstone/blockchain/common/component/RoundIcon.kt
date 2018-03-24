package io.goldstone.blockchain.common.component

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import com.blinnnk.extension.addCorner
import com.blinnnk.uikit.uiPX
import com.blinnnk.util.observing
import io.goldstone.blockchain.common.value.Spectrum
import org.jetbrains.anko.imageResource

/**
 * @date 24/03/2018 7:03 PM
 * @author KaySaith
 */

class RoundIcon(context: Context) : ImageView(context) {

  var iconColor: Int by observing(Color.BLACK) {
    addCorner(25.uiPX(), iconColor)
  }

  var src: Int by observing(0) {
    imageResource = src
  }

  var iconSize: Int by observing(50.uiPX()) {
    layoutParams = ViewGroup.LayoutParams(iconSize, iconSize)
  }

  init {
    layoutParams = ViewGroup.LayoutParams(iconSize, iconSize)
    setColorFilter(Spectrum.white)
    scaleType = ScaleType.CENTER_INSIDE
  }

}