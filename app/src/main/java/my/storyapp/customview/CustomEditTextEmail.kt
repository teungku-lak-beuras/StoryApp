package my.storyapp.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import my.storyapp.R

class CustomEditTextEmail @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatEditText(context, attrs), View.OnTouchListener {
    private var clearButtonImage: Drawable
    private var isEmailValid: Boolean = false
    private var interfaceToFragment: InterfaceToFragment? = null

    init {
        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_close_black_24dp) as Drawable

        setOnTouchListener(this)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    showClearButton()
                }
                else {
                    hideClearButton()
                }

                if (!(s != null && s.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(s).matches())) {
                    error = ContextCompat.getString(context, R.string.forbidden_reason_not_valid_email)
                    isEmailValid = false
                }
                else {
                    isEmailValid = true
                }

                interfaceToFragment?.validateCustomEditText()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showClearButton() {
        setButtonDrawables(endOfText = clearButtonImage)
    }

    private fun hideClearButton() {
        setButtonDrawables()
    }

    private fun setButtonDrawables(
        startOfText: Drawable? = null,
        topOfText: Drawable? = null,
        endOfText: Drawable? = null,
        bottomOfText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfText,
            topOfText,
            endOfText,
            bottomOfText
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (compoundDrawables[2] != null) {
            val clearButtonStart: Float
            val clearButtonEnd: Float
            var isClearButtonClicked = false

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                clearButtonEnd = (clearButtonImage.intrinsicWidth + paddingStart).toFloat()

                when {
                    event?.x!! < clearButtonEnd -> isClearButtonClicked = true
                }
            }
            else {
                clearButtonStart = (width - paddingEnd - clearButtonImage.intrinsicWidth).toFloat()

                when {
                    event?.x!! > clearButtonStart -> isClearButtonClicked = true
                }
            }

            if (isClearButtonClicked) {
                when (event?.action!!) {
                    MotionEvent.ACTION_DOWN -> {
                        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_close_black_24dp) as Drawable

                        showClearButton()
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_close_black_24dp) as Drawable

                        when {
                            text != null -> text?.clear()
                        }

                        hideClearButton()
                        return true
                    }

                    else -> return false
                }
            }
            else {
                return false
            }
        }
        else {
            return false
        }
    }

    fun isValid() = isEmailValid

    fun registerInterfaceToFragment(interfaceToFragment: InterfaceToFragment) {
        this.interfaceToFragment = interfaceToFragment
    }
}
