package com.layrin.recipefinder.ui.saved

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.layrin.recipefinder.R
import com.layrin.recipefinder.app.RecipeFinderApplication
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.databinding.FragmentSavedRecipeBinding
import com.layrin.recipefinder.ui.common.SelectedItemListener
import com.layrin.recipefinder.ui.common.SelectionManager
import com.layrin.recipefinder.ui.common.UiEvent
import com.layrin.recipefinder.ui.recipe.RecipeViewFragment
import com.layrin.recipefinder.ui.recipe.RecipeViewFragment.Companion.RECIPE_VIEW_TAG
import kotlinx.coroutines.launch

class SavedRecipeFragment :
    Fragment(),
    SelectedItemListener,
    ActionMode.Callback {

    private var _binding: FragmentSavedRecipeBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Binding not found"
        }

    private val viewModel by viewModels<SavedRecipeViewModel> {
        SavedRecipeViewModelFactory(
            RecipeRepository(
                (activity?.application as RecipeFinderApplication).database.recipeDao()
            ),
            SelectionManager()
        )
    }

    private lateinit var savedRecipeAdapter: SavedRecipeAdapter
    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSavedRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEventFlow()
        setAdapter()
    }

    private fun setEventFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.eventFlow.collect { uiEvent ->
                    val event = uiEvent as UiEvent.ShowSnackBar
                    Snackbar.make(
                        binding.root,
                        event.data,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setAdapter() {
        binding.rvSavedRecipe.apply {
            savedRecipeAdapter = SavedRecipeAdapter(this@SavedRecipeFragment)
            adapter = savedRecipeAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getSavedRecipe.collect { state ->
                    if (state.isEmpty()) {
                        binding.rvSavedRecipe.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = getString(R.string.empty_string)
                    } else {
                        savedRecipeAdapter.submitList(state)
                        binding.apply {
                            rvSavedRecipe.visibility = View.VISIBLE
                            tvEmpty.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun toggleSelectedItem(position: Int) =
        viewModel.selectionManager.toggleSelectedItem(position) { size ->
            if (actionMode == null) actionMode = requireActivity().startActionMode(this)

            if (size > 0) actionMode?.title = size.toString()
            else actionMode?.finish()
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onItemClickedListener(itemView: CardView, position: Int) {
        if (viewModel.selectionManager.isActive) {
            toggleSelectedItem(position)
            if (viewModel.selectionManager.isItemSelected(position))
                itemView.setCardBackgroundColor(requireActivity().getColor(R.color.selected_background))
            else
                itemView.setCardBackgroundColor(requireActivity().getColor(R.color.normal_background))
        } else {
            val currentItem = savedRecipeAdapter.getCurrentItem(position)
            currentItem?.let { item ->
                parentFragmentManager.commit {
                    replace(
                        R.id.nav_host_fragment_content_main,
                        RecipeViewFragment.newInstance(item),
                        RECIPE_VIEW_TAG
                    )
                    addToBackStack(RecipeViewFragment::class.java.name)
                }
            }
        }
    }

    override fun onItemLongClickedListener(itemView: CardView, position: Int) {
        toggleSelectedItem(position)
        if (viewModel.selectionManager.isItemSelected(position))
            itemView.setCardBackgroundColor(requireActivity().getColor(R.color.selected_background))
        else
            itemView.setCardBackgroundColor(requireActivity().getColor(R.color.normal_background))
    }

    override fun setItemBackground(itemView: CardView, position: Int) {
        if (viewModel.selectionManager.isItemSelected(position))
            itemView.setCardBackgroundColor(requireActivity().getColor(R.color.selected_background))
        else
            itemView.setCardBackgroundColor(requireActivity().getColor(R.color.normal_background))
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.multi_selection_menu_saved, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, menuItem: MenuItem?): Boolean {
        val selectedItem =
            savedRecipeAdapter.getSelectedItem(viewModel.selectionManager.selectedItem)
        return when (menuItem?.itemId) {
            R.id.action_delete -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getQuantityString(R.plurals.delete_multiple_item, selectedItem.size))
                    .setNegativeButton(getString(R.string.cancel_string)) { dialog, _ ->
                        dialog.dismiss()
                    }.setPositiveButton(getString(R.string.delete_string)) { dialog, _ ->
                        viewModel.onEvent(
                            SavedRecipeEvent.DeleteSavedRecipe(
                                selectedItem
                            )
                        )
                        dialog.dismiss()
                        resetSelectionMode()
                        actionMode?.finish()
                        mode?.finish()
                    }.show()
                true
            }
            android.R.id.home -> {
                resetSelectionMode()
                actionMode?.finish()
                mode?.finish()
                true
            }
            else -> {
                resetSelectionMode()
                actionMode?.finish()
                mode?.finish()
                false
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        if (viewModel.selectionManager.isActive) resetSelectionMode()
        actionMode = null
    }

    private fun resetSelectionMode() {
        viewModel.selectionManager.clearSelectedItem().forEach { position ->
            savedRecipeAdapter.notifyItemChanged(position)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title =
            getText(R.string.menu_saved_recipe)
    }

    override fun onPause() {
        super.onPause()
        resetSelectionMode()
        actionMode?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}