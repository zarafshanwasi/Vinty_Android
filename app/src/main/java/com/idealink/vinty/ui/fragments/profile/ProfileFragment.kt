package com.idealink.vinty.ui.fragments.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.databinding.FragmentProfileBinding
import com.idealink.vinty.ui.fragments.auth.SignInOtpFragment
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.AvatarUploadHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profile Fragment
 * Displays user profile information, statistics, and handles avatar upload
 */
class ProfileFragment : Fragment() {

    // ========================================================================
    // VIEW BINDING
    // ========================================================================

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ========================================================================
    // VIEW MODELS
    // ========================================================================

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    // ========================================================================
    // AVATAR UPLOAD - Activity Result Launchers
    // ========================================================================

    private var currentPhotoUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { handleImageSelected(it) }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showToast("Camera permission is required to take photos")
        }
    }

    // ========================================================================
    // LIFECYCLE METHODS
    // ========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()

        // Load initial data
        userSharedViewModel.loadUserHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ========================================================================
    // SETUP METHODS
    // ========================================================================

    /**
     * Setup all ViewModel observers
     */
    private fun setupObservers() {
        observeUserProfile()
        observeUserHistory()
        observeAvatarUpload()
        observeErrors()
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private fun setupClickListeners() {
        with(binding) {
            userAvtarFrame.setOnClickListener { showImagePickerDialog() }
            settingBtn.setOnClickListener { navigateToSettings() }
            inviteBtn.setOnClickListener { navigateToInvite() }
            historyBtn.setOnClickListener { navigateToUserHistory() }
            textViewVerify.setOnClickListener { navigateToVerification() }
        }
    }

    /**
     * Setup mission update synchronization
     */


    // ========================================================================
    // OBSERVERS - User Profile
    // ========================================================================

    private fun observeUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            userSharedViewModel.user.collect { user ->
                user?.let { updateUserUI(it) }
            }
        }
    }

    private fun updateUserUI(user: com.idealink.vinty.data.model.User) {
        with(binding) {
            // Basic info
            usernameTxtViewProfile.text = user.email
            val level = "Level ${user.level}"
            levelText.text = level

            // Level progress
            val progressPercent = (user.levelProgress * 100).toInt()
            levelProgress.progress = progressPercent

            // Verification status
            val isVerified = user.isVerified
            verifyBox.visibility = if (isVerified) View.GONE else View.VISIBLE
            textView41.visibility = if (isVerified) View.GONE else View.VISIBLE
            verificationBtn.visibility = if (isVerified) View.GONE else View.VISIBLE
            textViewVerify.visibility = if (isVerified) View.GONE else View.VISIBLE

            // Avatar
            if (!user.avatarUrl.isNullOrEmpty()) {
                Glide.with(this@ProfileFragment)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.profile_icon)
                    .into(profileImageView)
            }
        }
    }

    // ========================================================================
    // OBSERVERS - User History
    // ========================================================================

    private fun observeUserHistory() {
        // History loading state
        viewLifecycleOwner.lifecycleScope.launch {
            userSharedViewModel.historyLoading.collect { loading ->
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        // History data
        viewLifecycleOwner.lifecycleScope.launch {
            userSharedViewModel.history.collect { response ->
                response?.summary?.let { summary ->
                    with(binding) {
                        ticketEarned.text = summary.totalTicketsEarned.toString()
                        totalAdWatch.text = summary.totalAdsWatched.toString()
                        totalPrizeWon.text = summary.totalPrize.toString()
                        totalLotteryEntry.text = summary.totalEntries.toString()
                    }
                }
            }
        }
    }

    // ========================================================================
    // OBSERVERS - Avatar Upload
    // ========================================================================

    private fun observeAvatarUpload() {
        // Upload progress
        viewLifecycleOwner.lifecycleScope.launch {
            userSharedViewModel.avatarUploading.collect { uploading ->
                binding.avatarUploadProgress.visibility =
                    if (uploading) View.VISIBLE else View.GONE
                binding.userAvtarFrame.isEnabled = !uploading
            }
        }

        // Upload success
        viewLifecycleOwner.lifecycleScope.launch {
            userSharedViewModel.avatarUploadSuccess.collect { success ->
                if (success) {
                    showToast("Avatar updated successfully!")
                    userSharedViewModel.resetAvatarUploadSuccess()
                }
            }
        }
    }

    // ========================================================================
    // OBSERVERS - Error Handling
    // ========================================================================

    private fun observeErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            userSharedViewModel.error.collect { error ->
                error?.let { showToast(it) }
            }
        }
    }

    // ========================================================================
    // NAVIGATION METHODS
    // ========================================================================

    private fun navigateToSettings() {
        findNavController().navigate(R.id.action_profileFragment_to_settingFragment)
    }

    private fun navigateToInvite() {
        findNavController().navigate(R.id.action_profileFragment_to_inviteCodeFragment)
    }

    private fun navigateToUserHistory() {
        findNavController().navigate(R.id.action_profileFragment_to_userHistoryFragment)
    }

    private fun navigateToVerification() {
        val bundle = Bundle().apply {
            putString("source", SignInOtpFragment.SOURCE_HOME)
            putString("email", userSharedViewModel.user.value?.email)
        }
        findNavController().navigate(
            R.id.action_profileFragment_to_signInOtpFragment2,
            bundle
        )
    }

    // ========================================================================
    // AVATAR UPLOAD - UI Methods
    // ========================================================================

    /**
     * Show bottom sheet dialog to choose between camera and gallery
     */
    private fun showImagePickerDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)

        view.findViewById<View>(R.id.cameraOption).setOnClickListener {
            dialog.dismiss()
            openCamera()
        }

        view.findViewById<View>(R.id.galleryOption).setOnClickListener {
            dialog.dismiss()
            openGallery()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    /**
     * Open camera to take a photo
     */
    private fun openCamera() {
        // Check camera permission first
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, open camera
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show rationale and request permission
                showToast("Camera permission is needed to take photos")
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // Request permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Launch camera after permission is granted
     */
    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(currentPhotoUri)
        } catch (_: IOException) {
            showToast("Error opening camera")
        }
    }

    /**
     * Open gallery to pick an image
     */
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    /**
     * Create a temporary image file for camera
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().cacheDir
        return File.createTempFile("AVATAR_${timeStamp}_", ".jpg", storageDir)
    }

    /**
     * Handle selected image from camera or gallery
     */
    private fun handleImageSelected(imageUri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                imageUri
            )
            val avatarPart = AvatarUploadHelper.prepareAvatarPart(requireContext(), bitmap)

            if (avatarPart != null) {
                userSharedViewModel.uploadAvatar(avatarPart)
            } else {
                showToast("Failed to prepare image")
            }
        } catch (e: Exception) {
            showToast("Error loading image: ${e.message}")
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}