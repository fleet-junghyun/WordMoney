package com.be.hero.wordmoney.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        // 왼쪽 간격
        if (position == 0) {
            outRect.left = space
        }
        // 오른쪽 간격
        if (position == itemCount - 1) {
            outRect.right = space
        }
        // 각 아이템 간의 간격
        outRect.right = space
    }
}