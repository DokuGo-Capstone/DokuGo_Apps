package com.dokugo.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.dokugo.R

class CustomButtonDate @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var defaultBackground: Drawable? = null
    private var selectedBackground: Drawable? = null

    init {
        // Set the default background and selected background images
        defaultBackground = ContextCompat.getDrawable(context, R.drawable.bt_polos)
        selectedBackground = ContextCompat.getDrawable(context, R.drawable.bt_select)

        // Set the default background
        background = defaultBackground

        // Optionally, you can customize the text color
        setTextColor(ContextCompat.getColor(context, android.R.color.white))

        // Optionally, you can set the text size
        textSize = 14f
        setTextColor(ContextCompat.getColor(context, R.color.black))
    }

    fun setSelectedState(isSelected: Boolean) {
        // Change the background image based on the selection state
        if (isSelected) {
            background = selectedBackground
        } else {
            background = defaultBackground
        }
    }
}
