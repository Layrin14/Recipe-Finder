package com.layrin.recipefinder.ui.common

import androidx.cardview.widget.CardView

interface SelectedItemListener {
    fun onItemClickedListener(itemView: CardView, position: Int)
    fun onItemLongClickedListener(itemView: CardView, position: Int)
    fun setItemBackground(itemView: CardView, position: Int)
}