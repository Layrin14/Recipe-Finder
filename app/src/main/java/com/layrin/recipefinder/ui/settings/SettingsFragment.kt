package com.layrin.recipefinder.ui.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.layrin.recipefinder.R
import com.layrin.recipefinder.ui.MainActivity

class SettingsFragment : PreferenceFragmentCompat() {

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            return
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                android.R.id.home -> {
                    findNavController().popBackStack()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        val themeList = findPreference<ListPreference>(THEME_KEY)
        themeList?.apply {
            setIcon(when (preferenceManager.sharedPreferences?.getString(SELECTED_THEME,
                SAME_AS_SYSTEM_THEME_KEY)) {
                LIGHT_THEME_KEY -> R.drawable.ic_light_mode
                DARK_THEME_KEY -> R.drawable.ic_dark_mode
                else -> R.drawable.ic_same_as_system
            })
        }
        themeList?.setOnPreferenceChangeListener { preference, selected ->
            val icon = setTheme(selected.toString())
            preference.setIcon(icon)
            preference.sharedPreferences?.edit()
                ?.putString(SELECTED_THEME, selected.toString())
                ?.apply()
            true
        }
    }

    private fun setTheme(key: String): Int {
        return when (key) {
            LIGHT_THEME_KEY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.drawable.ic_light_mode
            }
            DARK_THEME_KEY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                R.drawable.ic_dark_mode
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                R.drawable.ic_same_as_system
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as MainActivity).setDrawerLocked()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).setDrawerUnlocked()
    }

    companion object {
        const val THEME_KEY = "action_theme"
        const val LIGHT_THEME_KEY = "action_light"
        const val DARK_THEME_KEY = "action_dark"
        const val SAME_AS_SYSTEM_THEME_KEY = "action_system"
        const val SELECTED_THEME = "theme"
    }
}