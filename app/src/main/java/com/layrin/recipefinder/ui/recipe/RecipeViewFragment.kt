package com.layrin.recipefinder.ui.recipe

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.layrin.recipefinder.R
import com.layrin.recipefinder.app.RecipeFinderApplication
import com.layrin.recipefinder.data.model.RecipeData
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.databinding.FragmentRecipeViewBinding
import com.layrin.recipefinder.ui.MainActivity
import com.layrin.recipefinder.ui.common.UiEvent
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class RecipeViewFragment : Fragment() {

    private var _binding: FragmentRecipeViewBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Binding not found"
        }

    private var recipeData: RecipeData? = null

    private val viewModel by viewModels<RecipeViewViewModel> {
        RecipeViewViewModelFactory(
            RecipeRepository(
                (activity?.application as RecipeFinderApplication).database.recipeDao()
            ),
            recipeData
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipeData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arguments?.getParcelable(RECIPE_VIEW_KEY, RecipeData::class.java)
        else arguments?.getParcelable(RECIPE_VIEW_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecipeViewBinding.inflate(inflater, container, false)
        (activity as MainActivity).setDrawerLocked()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            title = getText(R.string.app_name)
        }

        binding.wvRecipe.apply {
            webViewClient = WebViewClient()
            recipeData?.url?.let { link ->
                loadUrl(link)
            }
        }

        binding.fab.setOnClickListener {
            viewModel.onEvent(RecipeViewEvent.SaveRecipe)
        }

        setEventFlow()
    }

    private fun setEventFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).setDrawerUnlocked()
        _binding = null
    }

    companion object {
        const val RECIPE_VIEW_KEY = "recipe_view"
        const val RECIPE_VIEW_TAG = "recipe_view_tag"
        fun newInstance(item: RecipeData): RecipeViewFragment {
            return RecipeViewFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(RECIPE_VIEW_KEY, item)
                }
            }
        }
    }
}