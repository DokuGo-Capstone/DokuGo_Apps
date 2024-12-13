package com.dokugo.login.signin

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dokugo.MainActivity
import com.dokugo.R
import com.dokugo.databinding.ActivitySignInBinding
import com.dokugo.login.resetpassword.ForgetPasswordActivity
import com.dokugo.login.signup.SignUpActivity
import com.dokugo.ui.ComingsoonFragment
import com.dokugo.data.repository.UserRepository
import com.dokugo.data.network.RetrofitInstance
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var userRepository: UserRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var overlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        userRepository = UserRepository(RetrofitInstance.api)
        progressBar = findViewById(R.id.progressBar)
        overlay = findViewById(R.id.overlay)

        PlayAnimation()

        val tvSignUp: TextView = findViewById(R.id.tv_signup)
        tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        val BtSignIn: MaterialButton = findViewById(R.id.bt_signin)
        BtSignIn.setOnClickListener {
            val email = binding.emailComponent.editText?.text.toString().trim()
            val password = binding.passwordComponent.editText?.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        val tvForgot: TextView = findViewById(R.id.forgetPass)
        tvForgot.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.google.setOnClickListener {
            openComingSoonFragment()
        }
        binding.facebook.setOnClickListener {
            openComingSoonFragment()
        }
        binding.apple.setOnClickListener {
            openComingSoonFragment()
        }
    }

    private fun loginUser(email: String, password: String) {
        overlay.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = userRepository.loginUser(email, password)
                if (!response.error) {
                    val token = response.token
                    saveToken(token)
                    fetchUserProfile(token)
                    Toast.makeText(this@SignInActivity, "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignInActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMessage = parseErrorResponse(e)
                Toast.makeText(this@SignInActivity, "Login failed: $errorMessage", Toast.LENGTH_SHORT).show()
            } finally {
                overlay.visibility = View.GONE
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun fetchUserProfile(token: String) {
        lifecycleScope.launch {
            try {
                val profileResponse = userRepository.getProfile(token)
                saveUserId(profileResponse.user.id)
                navigateToMainActivity()
            } catch (e: Exception) {
                val errorMessage = parseErrorResponse(e)
                Toast.makeText(this@SignInActivity, "Failed to fetch user profile: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToken(token: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }

    private fun saveUserId(userId: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_id", userId)
        editor.apply()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun parseErrorResponse(exception: Exception): String {
        return try {
            val errorBody = (exception as? retrofit2.HttpException)?.response()?.errorBody()?.string()
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody)
                jsonObject.getString("error")
            } else {
                exception.message ?: "Unknown error"
            }
        } catch (e: Exception) {
            exception.message ?: "Unknown error"
        }
    }

    private fun openComingSoonFragment() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, ComingsoonFragment())
            .addToBackStack(null)
            .commit()

        Handler(Looper.getMainLooper()).postDelayed({
            supportFragmentManager.popBackStack()
        }, 2000)
    }

    private fun PlayAnimation() {
        with(binding) {
            animateView(icon, "alpha", 0f, 1f, 0)
            animateView(tvWelcome, "translationY", 500f, 0f, 200)
            animateView(tvSignin, "translationY", 500f, 0f, 400)
            animateView(tvEmail, "translationY", 500f, 0f, 600)
            animateView(emailComponent, "translationY", 500f, 0f, 600)
            animateView(tvPassword, "translationY", 500f, 0f, 600)
            animateView(passwordComponent, "translationY", 500f, 0f, 600)
            animateView(forgetPass, "translationY", 500f, 0f, 800)
            animateView(btSignin, "alpha", 0f, 1f, 1000)
            animateView(lineWithText, "alpha", 0f, 1f, 1200)
            animateView(apple, "alpha", 0f, 1f, 1400)
            animateView(google, "alpha", 0f, 1f, 1600)
            animateView(facebook, "alpha", 0f, 1f, 1800)
            animateView(noacc, "alpha", 0f, 1f, 2000)
            animateView(tvSignup, "alpha", 0f, 1f, 2200)
        }
    }

    private fun animateView(view: View, property: String, startValue: Float, endValue: Float, delay: Long) {
        view.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view, property, startValue, endValue).apply {
            duration = 1000
            startDelay = delay
            start()
        }
    }
}