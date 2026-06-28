package io.github.orizynpx.arxwipe.ui.launch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.data.sync.FirebaseSyncManager
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LaunchFragment : Fragment() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var syncManager: FirebaseSyncManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val isOnboardingDone = preferencesRepository.isOnboardingComplete().first()

            if (findNavController().currentDestination?.id != R.id.launchFragment) return@launch

            if (auth.currentUser == null) {
                if (isOnboardingDone) {
                    Timber.d("User not logged in, onboarding done. Navigating to Discover.")
                    findNavController().navigate(R.id.action_launchFragment_to_navigation_discover)
                } else {
                    Timber.d("User not logged in, onboarding NOT done. Navigating to Onboarding.")
                    findNavController().navigate(R.id.action_launchFragment_to_onboardingFragment)
                }
                return@launch
            }

            if (!isOnboardingDone) {
                Timber.d("User logged in, onboarding NOT done. Navigating to Onboarding.")
                findNavController().navigate(R.id.action_launchFragment_to_onboardingFragment)
                return@launch
            }

            
            Timber.d("User logged in, onboarding done. Navigating to Discover.")
            syncManager.startRealTimeSync()
            findNavController().navigate(R.id.action_launchFragment_to_navigation_discover)
        }
    }
}
