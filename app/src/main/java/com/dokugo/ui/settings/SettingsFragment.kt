package com.dokugo.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.bumptech.glide.Glide
import com.dokugo.R
import com.dokugo.data.network.RetrofitInstance
import com.dokugo.data.repository.UserRepository
import com.dokugo.databinding.FragmentSettingsBinding
import com.dokugo.login.signin.SignInActivity
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import androidx.appcompat.app.AlertDialog

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var userRepository: UserRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var overlay: View

    companion object {
        private const val REQUEST_CODE_AVATAR = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        navController = findNavController()
        userRepository = UserRepository(RetrofitInstance.api)
        progressBar = binding.progressBar
        overlay = binding.overlay

        loadProfile()

        binding.editAccountContainer.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("cameFromSettings", true)
            }

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_settings, true)
                .build()

            navController.navigate(R.id.action_settingsFragment_to_editProfileFragment, bundle, navOptions)
        }

        binding.logoutContainer.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val message = getString(R.string.change_message)
            val description = getString(R.string.dialog_description)
            val fullMessage = Html.fromHtml("<b>$message</b><br><i>$description</i>", Html.FROM_HTML_MODE_LEGACY)

            builder.setMessage(fullMessage)
                .setCancelable(false)
                .setPositiveButton("Ya") { _, _ ->
                    logout()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        binding.deleteAccountContainer.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val message = getString(R.string.delete_message)
            val description = getString(R.string.dialog_description)
            val fullMessage = Html.fromHtml("<b>$message</b><br><i>$description</i>", Html.FROM_HTML_MODE_LEGACY)

            builder.setMessage(fullMessage)
                .setCancelable(false)
                .setPositiveButton("Ya") { _, _ ->
                    deleteProfile()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        binding.btnChangePhoto.setOnClickListener {
            val intent = Intent(requireContext(), ProfileAvatarActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_AVATAR)
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_AVATAR && resultCode == Activity.RESULT_OK) {
            val avatarUrl = data?.getStringExtra("avatar_url")
            if (avatarUrl != null) {
                updateProfilePhoto(avatarUrl)
            }
        }
    }

    private fun updateProfilePhoto(avatarUrl: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = getTokenFromSharedPreferences()
                val response = userRepository.updateProfilePhotoUrl(token, avatarUrl)
                if (!response.error) {
                    // Save the new avatar URL in SharedPreferences
                    val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString("avatar_url", avatarUrl).apply()

                    // Update the UI with the new avatar URL
                    Glide.with(this@SettingsFragment).load(avatarUrl).into(binding.imgProfile)
                    Toast.makeText(requireContext(), "Photo updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMessage = parseErrorResponse(e)
                Toast.makeText(requireContext(), "Failed to update photo: $errorMessage", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun getTokenFromSharedPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    private fun loadProfile() {
        showLoading(true)
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", null)
        val phoneNumber = sharedPreferences.getString("phone_number", null)
        val avatarUrl = sharedPreferences.getString("avatar_url", null)

        if (username != null && email != null && avatarUrl != null) {
            binding.tvUsername.text = username
            binding.tvEmail.text = email
            Glide.with(this).load(avatarUrl).into(binding.imgProfile)
            showLoading(false)
        } else {
            getProfile()
        }
    }

    private fun getProfile() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            showLoading(true)
            lifecycleScope.launch {
                try {
                    val response = userRepository.getProfile(token)
                    binding.tvUsername.text = response.user.username
                    binding.tvEmail.text = response.user.email
                    Glide.with(this@SettingsFragment)
                        .load(response.user.avatarUrl)
                        .into(binding.imgProfile)

                    // Save profile data in SharedPreferences
                    sharedPreferences.edit().apply {
                        putString("username", response.user.username)
                        putString("email", response.user.email)
                        putString("phone_number", response.user.phoneNumber)
                        putString("avatar_url", response.user.avatarUrl)
                        apply()
                    }
                } catch (e: Exception) {
                    val errorMessage = parseErrorResponse(e)
                    Toast.makeText(requireContext(), "Failed to load profile: $errorMessage", Toast.LENGTH_SHORT).show()
                } finally {
                    showLoading(false)
                }
            }
        } else {
            Toast.makeText(requireContext(), "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteProfile() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            showLoading(true)
            lifecycleScope.launch {
                try {
                    val response = userRepository.deleteProfile(token)
                    if (!response.error) {
                        Toast.makeText(requireContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show()
                        logout()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    val errorMessage = parseErrorResponse(e)
                    Toast.makeText(requireContext(), "Failed to delete profile: $errorMessage", Toast.LENGTH_SHORT).show()
                } finally {
                    showLoading(false)
                }
            }
        } else {
            Toast.makeText(requireContext(), "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
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

    private fun clearLocalData() {
        // Clear shared preferences
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Clear any other local data if necessary
        // For example, delete files or clear databases
    }

    private fun logout() {
        clearLocalData()
        val intent = Intent(requireContext(), SignInActivity::class.java)
        startActivity(intent)
        activity?.finish()
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