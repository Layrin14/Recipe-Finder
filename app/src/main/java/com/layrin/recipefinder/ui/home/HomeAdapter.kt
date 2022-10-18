package com.layrin.recipefinder.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.layrin.recipefinder.R
import com.layrin.recipefinder.data.model.RecipeData
import com.layrin.recipefinder.databinding.ItemRecipeBinding
import com.layrin.recipefinder.ui.common.SelectedItemListener

class HomeAdapter(
    private val listener: SelectedItemListener?
) : ListAdapter<RecipeData, HomeAdapter.HomeViewHolder>(diffCallback) {
    inner class HomeViewHolder(
        private val binding: ItemRecipeBinding,
        private val listener: SelectedItemListener?
    ) : ViewHolder(binding.root) {
        fun bind(data: RecipeData) = with(itemView) {
            setOnClickListener {
                listener?.onItemClickedListener(binding.root, adapterPosition)
            }

            setOnLongClickListener {
                listener?.onItemLongClickedListener(binding.root, adapterPosition)
                true
            }

            listener?.setItemBackground(binding.root, adapterPosition)

            Glide.with(this).load(data.images).into(binding.ivRecipeImage)
            binding.apply {
                tvRecipeTitle.text = data.label
                tvCookTime.text = this@with.context.getString(R.string.time_string, data.totalTime)
                tvCalorie.text =
                    this@with.context.getString(R.string.calorie_string, data.calories / 1000)
            }
        }
    }

    fun getCurrentItem(position: Int): RecipeData? = getItem(position)

    fun getSelectedItem(list: List<Int>): List<RecipeData> {
        return mutableListOf<RecipeData>().apply {
            for (pos in list) add(this@HomeAdapter.getItem(pos) ?: continue)
        }.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            ItemRecipeBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { item -> holder.bind(item) }
    }

    companion object {
        private val diffCallback = object : ItemCallback<RecipeData>() {
            override fun areItemsTheSame(
                oldItem: RecipeData,
                newItem: RecipeData,
            ): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(
                oldItem: RecipeData,
                newItem: RecipeData,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}