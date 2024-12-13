package com.dokugo.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dokugo.R
import com.dokugo.data.repository.UserRepository
import com.dokugo.data.network.RetrofitInstance
import com.dokugo.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import android.util.Log

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userRepository: UserRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var overlay: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userRepository = UserRepository(RetrofitInstance.api)
        progressBar = binding.root.findViewById(R.id.progressBar)
        overlay = binding.root.findViewById(R.id.overlay)

        loadProfile()

        binding.btnSave.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            updateProfile(username, email, phoneNumber)
        }

        return root
    }

    private fun loadProfile() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", null)
        val phoneNumber = sharedPreferences.getString("phone_number", null)

        // Log the phone number to ensure it's being loaded correctly
        Log.d("EditProfileFragment", "Loaded phone number: $phoneNumber")

        if (username != null && email != null && phoneNumber != null) {
            binding.etUsername.setText(username)
            binding.etEmail.setText(email)
            binding.etPhoneNumber.setText(phoneNumber)
        } else {
            getProfile()
        }
    }

    private fun getProfile() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("auth_token", null) ?: ""
                val response = userRepository.getProfile(token)

                // Log the response for debugging
                Log.d("EditProfileFragment", "Response user data: ${response.user}")
                Log.d("EditProfileFragment", "Phone number from response: ${response.user.phoneNumber}")

                // Set values into the UI
                binding.etUsername.setText(response.user.username)
                binding.etEmail.setText(response.user.email)

                // Check phone number is not null or empty before setting it
                if (!response.user.phoneNumber.isNullOrBlank()) {
                    binding.etPhoneNumber.setText(response.user.phoneNumber)
                } else {
                    binding.etPhoneNumber.setText("No phone number provided")
                }

                // Save profile data in SharedPreferences
                val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    putString("username", response.user.username)
                    putString("email", response.user.email)
                    putString("phone_number", response.user.phoneNumber)
                    apply()
                }
            } catch (e: Exception) {
                val errorMessage = parseErrorResponse(e)
                Toast.makeText(requireContext(), "Failed to load profile: $errorMessage", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }



    private fun updateProfile(username: String, email: String, phoneNumber: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("auth_token", null) ?: ""
                val response = userRepository.updateProfile(token, username, email, phoneNumber)

                // Log the response to confirm if the update was successful
                Log.d("EditProfileFragment", "Profile update response: $response")

                if (!response.error) {
                    // Update SharedPreferences with new profile data
                    val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putString("username", username)
                        putString("email", email)
                        putString("phone_number", phoneNumber)
                        apply()
                    }
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()

                    // Navigate back to SettingsFragment
                    findNavController().navigate(R.id.action_editProfileFragment_to_settingsFragment)
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMessage = parseErrorResponse(e)
                Toast.makeText(requireContext(), "Failed to update profile: $errorMessage", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
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

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        overlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
