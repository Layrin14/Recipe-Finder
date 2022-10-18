package com.layrin.recipefinder.ui.home

import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AbsListView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.layrin.recipefinder.R
import com.layrin.recipefinder.app.RecipeFinderApplication
import com.layrin.recipefinder.data.model.RecipeResponseState
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.databinding.FragmentHomeBinding
import com.layrin.recipefinder.ui.common.SelectedItemListener
import com.layrin.recipefinder.ui.common.SelectionManager
import com.layrin.recipefinder.ui.common.UiEvent
import com.layrin.recipefinder.ui.recipe.RecipeViewFragment
import com.layrin.recipefinder.ui.recipe.RecipeViewFragment.Companion.RECIPE_VIEW_TAG
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment :
    Fragment(),
    SelectedItemListener,
    ActionMode.Callback {

    private var _binding: FragmentHomeBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Binding not found"
        }

    private val viewModel by viewModels<HomeViewModel> {
        HomeViewModelFactory(
            RecipeRepository(
                (activity?.application as RecipeFinderApplication).database.recipeDao()
            ),
            SelectionManager(),
            activity?.application as RecipeFinderApplication
        )
    }

    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false
    private var isSearching = false
    private var isRandom = true

    private val adapterScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && isLastPage
            val isAtLastItem = firstVisibleItemPos + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPos >= 0
            val isTotalMoreThanVisible = totalItemCount >= 20
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            when {
                shouldPaginate && isRandom -> {
                    viewModel.getRandomRecipes()
                    isScrolling = false
                    binding.pbPaginate.visibility = View.VISIBLE
                }
                shouldPaginate && isSearching -> {
                    viewModel.searchRecipeUrl?.let { viewModel.searchRecipe(it) }
                    isScrolling = false
                    binding.pbPaginate.visibility = View.VISIBLE
                }
            }
        }
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.main_menu, menu)

            val searchItem = menu.findItem(R.id.action_search)
            val searchView = searchItem.actionView as SearchView
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.imeOptions = EditorInfo.IME_ACTION_DONE

            val searchManager =
                requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

            var job: Job? = null

            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    job?.cancel()
                    job = MainScope().launch {
                        delay(500L)
                        if (text != null && text.isNotEmpty()) {
                            viewModel.searchRecipe(text)
                            setViewForSearchRecipe()
                            isSearching = true
                            isRandom = false
                        } else {
                            viewModel.resetSearchQuery()
                            setView()
                        }
                    }
                    return true
                }
            })

            val closeBtn =
                searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            closeBtn.setOnClickListener {
                searchView.setQuery("", false)
                searchView.onActionViewCollapsed()
                searchItem.collapseActionView()
                viewModel.resetSearchQuery()
                isSearching = false
                isRandom = true
                setView()
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return false
        }
    }

    private lateinit var homeAdapter: HomeAdapter

    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setAdapter()
        setView()
        setUiEventFlow()
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.menu_home)
    }

    private fun setViewForSearchRecipe() {
        binding.rvRecipeList.visibility = View.INVISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipeFoundState.collect { state ->
                    handleState(state, viewModel.foodRecipeFoundTotal)
                }
            }
        }
    }

    private fun setView() {
        binding.rvRecipeList.visibility = View.INVISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.randomRecipeState.collect { state ->
                    handleState(state, viewModel.randomRecipeTotal)
                }
            }
        }
    }

    private suspend fun handleState(state: RecipeResponseState, total: Int?) {
        if (state.isLoading && state.recipe == null) {
            toggleProgressBar(true)
            isLoading = true
        } else if (state.isLoading && state.recipe?.isNotEmpty() == true) {
            toggleProgressBar(false)
            isLoading = true
        } else {
            state.recipe?.let { recipeFounds ->
                homeAdapter.submitList(recipeFounds)
                binding.rvRecipeList.visibility = View.VISIBLE
                binding.pbPaginate.visibility = View.GONE
                toggleProgressBar(false)
                isLoading = false
                val totalData = state.recipe.size
                isLastPage = total == totalData
                if (isLastPage) binding.rvRecipeList.setPadding(0, 0, 0, 0)
            }

            when {
                state.recipe?.isEmpty() == true -> {
                    binding.tvNotFound.visibility = View.VISIBLE
                    binding.tvNotFound.text = getText(R.string.not_found)
                }
                state.error != null -> {
                    delay(500L)
                    Snackbar.make(
                        binding.root, state.error, Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
        resetMultiSelectionMode()
    }

    private fun setUiEventFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { uiEvent ->
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

    private fun toggleProgressBar(show: Boolean) {
        if (show) binding.pbLoading.visibility = View.VISIBLE
        else binding.pbLoading.visibility = View.INVISIBLE
    }

    private fun setAdapter() {
        homeAdapter = HomeAdapter(this)
        binding.rvRecipeList.apply {
            adapter = homeAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            addOnScrollListener(this@HomeFragment.adapterScrollListener)
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
            val currentItem = homeAdapter.getCurrentItem(position)
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
        mode?.menuInflater?.inflate(R.menu.multi_selection_menu_home, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val selectedItem = homeAdapter.getSelectedItem(viewModel.selectionManager.selectedItem)
        return when (item?.itemId) {
            R.id.action_add_to_favorite -> {
                viewModel.onEvent(
                    HomeEvent.AddItemToBookmarkEvent(selectedItem)
                )
                actionMode?.finish()
                resetMultiSelectionMode()
                mode?.finish()
                true
            }
            android.R.id.home -> {
                actionMode?.finish()
                resetMultiSelectionMode()
                mode?.finish()
                true
            }
            else -> {
                actionMode?.finish()
                resetMultiSelectionMode()
                mode?.finish()
                true
            }
        }
    }

    private fun resetMultiSelectionMode() {
        viewModel.selectionManager.clearSelectedItem().forEach { position ->
            homeAdapter.notifyItemChanged(position)
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        if (viewModel.selectionManager.isActive) resetMultiSelectionMode()
        actionMode = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}