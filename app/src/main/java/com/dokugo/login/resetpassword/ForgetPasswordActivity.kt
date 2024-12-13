package com.dokugo.login.resetpassword

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dokugo.R
import com.dokugo.data.network.RetrofitInstance
import com.dokugo.data.repository.UserRepository
import com.dokugo.login.signin.SignInActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var overlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        userRepository = UserRepository(RetrofitInstance.api)
        progressBar = findViewById(R.id.progressBar)
        overlay = findViewById(R.id.overlay)

        val icBack: ImageView = findViewById(R.id.ic_back)
        icBack.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        val tvLogin: TextView = findViewById(R.id.tv_textlogin)
        tvLogin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        val btSendEmail: MaterialButton = findViewById(R.id.bt_sendemail)
        btSendEmail.setOnClickListener {
            val emailEditText: TextInputEditText = findViewById(R.id.emailEditText)
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendOtp(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOtp(email: String) {
        overlay.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = userRepository.sendOtp(email)
                if (!response.error) {
                    Toast.makeText(this@ForgetPasswordActivity, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ForgetPasswordActivity, OtpActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@ForgetPasswordActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMessage = parseErrorResponse(e)
                Toast.makeText(this@ForgetPasswordActivity, "Failed to send OTP: $errorMessage", Toast.LENGTH_SHORT).show()
            } finally {
                overlay.visibility = View.GONE
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun parseErrorResponse(exception: Exception): String {
        return try {
            val errorBody = (exception as? HttpException)?.response()?.errorBody()?.string()
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
}