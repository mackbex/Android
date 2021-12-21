package com.material.ui.subclass

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.material.R
import com.material.databinding.CustomListitemHomeBinding
import com.material.util.ext.dpToPx
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.domain.model.MaterialItem


class Panels: ConstraintLayout, View.OnClickListener {


    private var _binding: CustomListitemHomeBinding? = null
    private val binding get() = _binding!!

    val tvTitle
        get() = binding.tvTitle

    val iconMenu
        get() = binding.iconMenu

    var itemId:Int = 0

    var subMenu:LinearLayout? = null

            constructor(context: Context) : super(context) {
        init(attrs = null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        _binding = CustomListitemHomeBinding.bind(View.inflate(context, R.layout.custom_listitem_home, this))
        binding.layoutMain.setOnClickListener(this)

        val resources = context.obtainStyledAttributes(attrs,R.styleable.Panels)
        try {
            val title = resources.getString(R.styleable.Panels_title)
            val icon = resources.getResourceId(R.styleable.Panels_icon, 0)

            binding.tvTitle.text = title
            if(icon != 0) {
                binding.iconMenu.setImageDrawable(AppCompatResources.getDrawable(context, icon))
            }

        }
        finally {
            resources.recycle()
        }
    }


    fun setItems(items:List<MaterialItem>) {
        subMenu = LinearLayout(context).apply {
            layoutParams = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topToBottom = binding.tvTitle.id
                startToStart = binding.tvTitle.id
                endToEnd = binding.tvTitle.id
            }

            orientation = LinearLayout.VERTICAL
            visibility = View.GONE

        }
        binding.layoutMain.addView(subMenu)

        items.forEach {  item ->
            subMenu?.addView(TextView(context).apply {
                layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                text = item.title
                transitionName = "${context.getString(R.string.transition_main_item_click)}_${item.title}"
                setOnClickListener(item.clickListener)
                setPadding(20.dpToPx(), 10.dpToPx(), 20.dpToPx(), 10.dpToPx())
                isClickable = true
                isFocusable = true

                val outValue = TypedValue()
                context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    outValue,
                    true
                )
                setBackgroundResource(outValue.resourceId)
            })
        }
    }



    override fun onClick(view: View?) {

        view?.run {
            subMenu?.apply {
                animateViewSize(this, binding.iconArrow)
            }
            val snackBar = Snackbar.make(this, "${binding.tvTitle.text}", Snackbar.LENGTH_LONG)
            snackBar.setAction(R.string.snackbar_action_dismiss) { snackBar.dismiss() }
            snackBar.show()
        }
    }

    private fun animateViewSize(view: View, arrow:View) {
        val duration = 500L
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val height = view.measuredHeight
        var hasAfterProc = false
        val valueAnimator = if(view.isVisible) {
            arrow.animate().rotation(arrow.rotation - 180f).setDuration(duration).start()
            hasAfterProc = true
            ValueAnimator.ofInt(height, 0)
        } else {
            arrow.animate().rotation(arrow.rotation + 180f).setDuration(duration).start()
            view.isVisible = true
            ValueAnimator.ofInt(0, height)
        }

        valueAnimator.duration = 500L
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener {
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = animatedValue
            view.layoutParams = layoutParams
        }
        valueAnimator.start()
        valueAnimator.doOnEnd {
            if(hasAfterProc) view.isVisible = !view.isVisible
        }
    }
}