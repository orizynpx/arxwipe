package io.github.orizynpx.arxwipe.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.data.sync.FirebaseSyncManager
import io.github.orizynpx.arxwipe.databinding.FragmentProfileBinding
import io.github.orizynpx.arxwipe.ui.auth.AuthState
import io.github.orizynpx.arxwipe.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var syncManager: FirebaseSyncManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.toolbarProfile.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    if (findNavController().currentDestination?.id == R.id.profileFragment) {
                        findNavController().navigate(R.id.action_navigation_profile_to_settingsFragment)
                    }
                    true
                }
                else -> false
            }
        }

        binding.loginButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.profileFragment) {
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        }

        binding.signOutButton.setOnClickListener {
            syncManager.stopSync()
            authViewModel.logout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: AuthState) {
        when (state) {
            is AuthState.Authenticated -> {
                binding.loginPromptCard.visibility = View.GONE
                binding.authenticatedLayout.visibility = View.VISIBLE
                binding.userEmailText.text = state.userEmail
            }
            else -> {
                binding.loginPromptCard.visibility = View.VISIBLE
                binding.authenticatedLayout.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
