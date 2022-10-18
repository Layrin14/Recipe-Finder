package com.layrin.recipefinder.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.layrin.recipefinder.R
import com.layrin.recipefinder.databinding.ActivityMainBinding
import com.layrin.recipefinder.ui.recipe.RecipeViewFragment.Companion.RECIPE_VIEW_TAG
import com.layrin.recipefinder.ui.settings.SettingsFragment.Companion.DARK_THEME_KEY
import com.layrin.recipefinder.ui.settings.SettingsFragment.Companion.LIGHT_THEME_KEY
import com.layrin.recipefinder.ui.settings.SettingsFragment.Companion.SAME_AS_SYSTEM_THEME_KEY
import com.layrin.recipefinder.ui.settings.SettingsFragment.Companion.SELECTED_THEME

class MainActivity : AppCompatActivity(), DrawerController {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    private var toolbarIcon: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setAppTheme()

        installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHost
        navController = navHost.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_saved_recipe, R.id.nav_settings),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val onChildFragmentManager = supportFragmentManager
                    .findFragmentById(
                        binding.appBarMain.contentMain.navHostFragmentContentMain.id
                    )?.childFragmentManager

                val fragments = onChildFragmentManager?.fragments

                if (onChildFragmentManager?.findFragmentByTag(RECIPE_VIEW_TAG) ==
                    fragments?.last()
                ) {
                    onChildFragmentManager?.popBackStack()
                    return
                } else if (onChildFragmentManager?.backStackEntryCount == 0) {
                    finish()
                } else {
                    findNavController(
                        binding.appBarMain.contentMain.navHostFragmentContentMain.id
                    ).popBackStack()
                }
            }
        })

        toolbarIcon = binding.appBarMain.toolbar.navigationIcon
    }

    private fun setAppTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        when (sharedPreferences.getString(SELECTED_THEME, SAME_AS_SYSTEM_THEME_KEY)) {
            LIGHT_THEME_KEY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DARK_THEME_KEY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SAME_AS_SYSTEM_THEME_KEY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val onChildFragmentManager = supportFragmentManager
            .findFragmentById(
                binding.appBarMain.contentMain.navHostFragmentContentMain.id
            )?.childFragmentManager

        return if (onChildFragmentManager?.findFragmentByTag(RECIPE_VIEW_TAG) ==
            onChildFragmentManager?.fragments?.last()
        ) {
            onChildFragmentManager?.popBackStack()
            true
        } else {
            navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }
    }

    override fun setDrawerLocked() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.appBarMain.toolbar.navigationIcon = null
    }

    override fun setDrawerUnlocked() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        binding.appBarMain.toolbar.navigationIcon = toolbarIcon
    }
}