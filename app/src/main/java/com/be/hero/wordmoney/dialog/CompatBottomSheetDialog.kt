package com.be.hero.wordmoney.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import com.be.hero.wordmoney.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class CompatBottomSheetDialog : BottomSheetDialogFragment() {
    private val bottomSheet
        get() = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout


    private val keyboard: InputMethodManager by lazy {
        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            if(this is BottomSheetDialog){
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun getTheme() = R.style.BottomSheetDialog

    protected fun showDialog() {
        bottomSheet?.animate()?.alpha(1f)?.duration = 500
    }

    protected fun hideDialog() {
        bottomSheet?.alpha = 0f
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener()
    }

    private var onDismissListener: () -> Unit = {}
    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

}