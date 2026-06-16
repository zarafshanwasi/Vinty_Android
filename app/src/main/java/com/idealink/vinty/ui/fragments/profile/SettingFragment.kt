package com.idealink.vinty.ui.fragments.profile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.messaging.FirebaseMessaging
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.model.DeleteAccountRequest
import com.idealink.vinty.databinding.FragmentSettingBinding
import com.idealink.vinty.ui.activities.AccountActivity
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val TAG = "SettingFragment"

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enableNotifications()
        } else {
            binding.switch1.isChecked = false
            showToast("Notification permission denied")
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupNotificationToggle()
        observeProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun observeProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    userSharedViewModel.user.collect { user ->
                        user?.let { binding.profileHeader.updateProfile(it) }
                    }
                }

                launch {
                    userSharedViewModel.badgeStats.collect { response ->
                        response?.data?.overall?.let {
                            binding.profileHeader.updateBadgeStats(
                                unlocked = it.unlocked,
                                locked = it.locked
                            )
                        }
                    }
                }

                launch {
                    userSharedViewModel.isLoading.collect { isLoading ->
                        binding.root.alpha = if (isLoading) 0.5f else 1f
                    }
                }

                launch {
                    userSharedViewModel.error.collect { error ->
                        error?.let { showToast(it) }
                    }
                }
            }
        }
    }


    private fun setupClickListeners() = with(binding) {

        backBtn4.setOnClickListener { findNavController().navigateUp() }

        privacyBtn.setOnClickListener {
            openUrl("https://www.vinty.win/privacy.html")
        }

        termsBtn.setOnClickListener {
            openUrl("https://www.vinty.win/terms.html")
        }

        settingBtn.setOnClickListener {
            openUrl("https://www.vinty.win/support.html")
        }

        faqBtn.setOnClickListener {
            openLocalPdf()
        }

        changePassBtn.setOnClickListener {
            findNavController().navigate(R.id.changePasswordFragment)
        }

        deleteAccountBtn.setOnClickListener {
            showDeleteAccountDialog()
        }

        profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_profileFragment)
        }

        logoutBtn.setOnClickListener {
            showLogoutSheet()
        }
    }


    private fun setupNotificationToggle() {
        val dataManager = DataManager.getInstance()
        binding.switch1.isChecked = dataManager.isNotificationsEnabled()

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    enableNotifications()
                }
            } else {
                disableNotifications()
            }
        }
    }

    private fun enableNotifications() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val dataManager = DataManager.getInstance()
            dataManager.saveFcmToken(token)

            val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
            val appVersion = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName ?: "1.0"

            val request = mapOf(
                "fcmToken" to token,
                "deviceType" to "MOBILE",
                "deviceId" to deviceId,
                "platform" to "Android ${Build.VERSION.RELEASE}",
                "appVersion" to appVersion
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val app = requireActivity().application as App
                    val response = app.repository.registerDevice(request)
                    if (response.isSuccessful) {
                        dataManager.saveNotificationsEnabled(true)
                        Log.d(TAG, "Device registered successfully")
                    } else {
                        dataManager.saveNotificationsEnabled(false)
                        binding.switch1.isChecked = false
                        Log.e(TAG, "Failed to register device: ${response.code()}")
                    }
                } catch (e: Exception) {
                    dataManager.saveNotificationsEnabled(false)
                    binding.switch1.isChecked = false
                    Log.e(TAG, "Failed to register device: ${e.message}")
                }
            }
        }.addOnFailureListener {
            binding.switch1.isChecked = false
            showToast("Failed to get notification token")
        }
    }

    private fun disableNotifications() {
        val dataManager = DataManager.getInstance()
        val token = dataManager.getFcmToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val app = requireActivity().application as App
                val response = app.repository.unregisterDevice(mapOf("fcmToken" to token))
                if (response.isSuccessful) {
                    dataManager.saveNotificationsEnabled(false)
                    Log.d(TAG, "Device unregistered successfully")
                } else {
                    dataManager.saveNotificationsEnabled(true)
                    binding.switch1.isChecked = true
                    Log.e(TAG, "Failed to unregister device: ${response.code()}")
                }
            } catch (e: Exception) {
                dataManager.saveNotificationsEnabled(true)
                binding.switch1.isChecked = true
                Log.e(TAG, "Failed to unregister device: ${e.message}")
            }
        }
    }

    private fun showLogoutSheet() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_bar_logout, null)
        bottomSheet.setContentView(view)

        view.findViewById<TextView>(R.id.confirm_logout_btn).setOnClickListener {
            bottomSheet.dismiss()
            logoutUser()
        }

        view.findViewById<MaterialButton>(R.id.go_back_btn).setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun logoutUser() {
        userSharedViewModel.logout {
            showToast("Logged out")
            navigateToAccountActivity()
        }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val request = DeleteAccountRequest(
            reason = "No longer using the app",
            feedback = "The app was great, just moving on"
        )

        userSharedViewModel.deleteAccount(request) {
            showToast("Account deleted successfully")
            navigateToAccountActivity()
        }
    }


    private fun navigateToAccountActivity() {
        val intent = Intent(requireContext(), AccountActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun openLocalPdf() {
        val assetFileName = "vinty_faq.pdf"
        val context = requireContext()
        val file = File(context.cacheDir, assetFileName)

        try {
            if (!file.exists()) {
                context.assets.open(assetFileName).use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to open PDF", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
