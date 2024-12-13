package com.dokugo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dokugo.databinding.ActivityMainBinding
import com.dokugo.login.signin.SignInActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up NavController for navigating between fragments
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Set up BottomNavigationView to work with the NavController
        val bottomNavView: BottomNavigationView = binding.navView
        bottomNavView.setupWithNavController(navController)

        // Check token and navigate accordingly
        checkTokenAndNavigate()
    }

    private fun checkTokenAndNavigate() {
        lifecycleScope.launch {
            val token = getTokenFromSharedPreferences()

            withContext(Dispatchers.Main) {
                if (token.isNotEmpty()) {
                } else {
                    // No token, navigate to login page
                    navigateToLoginPage()
                }
            }
        }
    }

    private fun getTokenFromSharedPreferences(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearLocalData() {
        // Clear shared preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Clear any other local data if necessary
        // For example, delete files or clear databases
    }

    private fun logout() {
        clearLocalData()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}