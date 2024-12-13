package com.dokugo.login.resetpassword

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dokugo.R
import com.dokugo.data.network.RetrofitInstance
import com.dokugo.data.repository.UserRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var email: String
    private lateinit var progressBar: ProgressBar
    private lateinit var overlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        userRepository = UserRepository(RetrofitInstance.api)
        email = intent.getStringExtra("email") ?: ""
        progressBar = findViewById(R.id.progressBar)
        overlay = findViewById(R.id.overlay)

        val otpFields = listOf(
            findViewById<EditText>(R.id.otp_digit1),
            findViewById<EditText>(R.id.otp_digit2),
            findViewById<EditText>(R.id.otp_digit3),
            findViewById<EditText>(R.id.otp_digit4),
            findViewById<EditText>(R.id.otp_digit5),
            findViewById<EditText>(R.id.otp_digit6)
        )

        otpFields.forEachIndexed { index, editText ->
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1 && index < otpFields.size - 1) {
                    otpFields[index + 1].requestFocus()
                } else if (s?.isEmpty() == true && index > 0) {
                    otpFields[index - 1].requestFocus()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

        val btConfOtp: MaterialButton = findViewById(R.id.bt_confotp)
        btConfOtp.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }
            if (otp.length == 6) {
                verifyOtp(email, otp)
            } else {
                Toast.makeText(this, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyOtp(email: String, otp: String) {
        overlay.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = userRepository.verifyOtp(email, otp)
                if (!response.error) {
                    Toast.makeText(this@OtpActivity, "OTP verified successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@OtpActivity, ResetPassActivity::class.java)
                    intent.putExtra("resetToken", response.resetToken)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@OtpActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OtpActivity, "Failed to verify OTP: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                overlay.visibility = View.GONE
                progressBar.visibility = View.GONE
            }
        }
    }
}