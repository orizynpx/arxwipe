package io.github.orizynpx.arxwipe.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.FragmentReceivedNotificationsBinding
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ReceivedNotificationsFragment : Fragment() {

    private var _binding: FragmentReceivedNotificationsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentReceivedNotificationsBinding.inflate(inflater, container, false)
        
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        
        return binding.root
    }

    private fun setupToolbar() {
        binding.tbNotifications.setNavigationOnClickListener {
            Timber.d("Back button clicked in Notifications")
            if (!parentFragmentManager.isStateSaved) {
                parentFragmentManager.popBackStack()
            }
        }
        
        binding.tbNotifications.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_mark_all_read -> {
                    viewModel.markAllAsRead()
                    true
                }
                R.id.action_clear_all -> {
                    viewModel.clearAllNotifications()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            Timber.d("Clicked on notification: ${notification.message}")
            viewModel.markAsRead(notification.id)
        }
        
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notifications.collect { notifications ->
                    if (notifications.isEmpty()) {
                        binding.llEmptyState.visibility = View.VISIBLE
                        binding.notificationsRecyclerView.visibility = View.GONE
                    } else {
                        binding.llEmptyState.visibility = View.GONE
                        binding.notificationsRecyclerView.visibility = View.VISIBLE
                        notificationAdapter.submitList(notifications)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
