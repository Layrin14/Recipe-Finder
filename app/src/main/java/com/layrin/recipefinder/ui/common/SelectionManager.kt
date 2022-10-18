package com.layrin.recipefinder.ui.common

class SelectionManager {

    private val _selectedItem = mutableListOf<Int>()

    val selectedItem get() = _selectedItem.toList()

    val isActive get() = _selectedItem.size > 0

    fun isItemSelected(position: Int): Boolean = _selectedItem.contains(position)

    fun toggleSelectedItem(position: Int, toggleListener: (Int) -> Unit) {
        if (_selectedItem.contains(position)) _selectedItem.remove(position)
        else _selectedItem.add(position)
        toggleListener(_selectedItem.size)
    }

    fun clearSelectedItem(): List<Int> {
        if (!isActive) return emptyList()
        val copy = _selectedItem.toList()
        _selectedItem.clear()
        return copy
    }
}