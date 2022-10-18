package com.layrin.recipefinder.ui.saved

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

class SavedRecipeAdapter(
    private val listener: SelectedItemListener?,
) : ListAdapter<RecipeData, SavedRecipeAdapter.SavedRecipeViewHolder>(diffCallback) {
    inner class SavedRecipeViewHolder(
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
                tvCalorie.text = this@with.context.getString(R.string.calorie_string, data.calories / 1000)
            }
        }
    }

    fun getCurrentItem(position: Int): RecipeData? = getItem(position)

    fun getSelectedItem(data: List<Int>): List<RecipeData> {
        return mutableListOf<RecipeData>().apply {
            for (pos in data) add(this@SavedRecipeAdapter.getItem(pos) ?: continue)
        }.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedRecipeViewHolder {
        return SavedRecipeViewHolder(
            ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: SavedRecipeViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    companion object {
        private val diffCallback = object : ItemCallback<RecipeData>() {
            override fun areItemsTheSame(oldItem: RecipeData, newItem: RecipeData): Boolean {
                return oldItem.uri == newItem.uri
            }

            override fun areContentsTheSame(oldItem: RecipeData, newItem: RecipeData): Boolean {
                return oldItem == newItem
            }
        }
    }
}