package io.github.orizynpx.arxwipe.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.ArxwipeApplication
import io.github.orizynpx.arxwipe.databinding.FragmentSettingsBinding
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import io.github.orizynpx.arxwipe.data.work.TestNotificationWorker
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repository: PreferencesRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enqueueTestNotification()
        } else {
            Timber.w("Notification permission denied")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarSettings.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.dynamicColorSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != ArxwipeApplication.isDynamicColorsEnabled) {
                lifecycleScope.launch {
                    repository.saveDynamicColorsPreference(isChecked)
                    ArxwipeApplication.isDynamicColorsEnabled = isChecked
                    activity?.recreate()
                }
            }
        }

        binding.sendNotificationButton.setOnClickListener {
            checkPermissionAndSend()
        }

        binding.resetOnboardingButton.setOnClickListener {
            Timber.d("Reset onboarding clicked")
            lifecycleScope.launch {
                repository.clearPreferences()
                
                
                
                activity?.finishAffinity()
                
                val intent = activity?.packageManager?.getLaunchIntentForPackage(requireContext().packageName)
                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent?.let { startActivity(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.isDynamicColorsEnabled().collect { isEnabled ->
                    binding.dynamicColorSwitch.isChecked = isEnabled
                }
            }
        }
    }

    private fun checkPermissionAndSend() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    enqueueTestNotification()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            enqueueTestNotification()
        }
    }

    private fun enqueueTestNotification() {
        Timber.d("Enqueuing TestNotificationWorker")
        val workRequest = OneTimeWorkRequestBuilder<TestNotificationWorker>().build()
        WorkManager.getInstance(requireContext()).enqueue(workRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
