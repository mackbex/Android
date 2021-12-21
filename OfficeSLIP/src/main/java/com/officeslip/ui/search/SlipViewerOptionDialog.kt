package com.officeslip.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.officeslip.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SlipViewerOptionDialog : BottomSheetDialogFragment() {

    private var callBack: OnClickButton? = null
    interface OnClickButton {
        fun onDialogBtnClick(strTag:String)
    }

    fun setOnClickButtonListener(listener: OnClickButton) {
        this.callBack = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_search_slip, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.layout_bs).touchables.forEach {
            it.setOnClickListener { btn ->
                dismiss()
                callBack?.onDialogBtnClick(btn.tag.toString())
            }
        }
    }
}