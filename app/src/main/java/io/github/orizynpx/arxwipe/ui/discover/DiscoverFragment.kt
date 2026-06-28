package io.github.orizynpx.arxwipe.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.FragmentDiscoverBinding
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import io.github.orizynpx.arxwipe.ui.cardstackview.CardStackLayoutManager
import io.github.orizynpx.arxwipe.ui.cardstackview.CardStackListener
import io.github.orizynpx.arxwipe.ui.cardstackview.Direction
import io.github.orizynpx.arxwipe.ui.cardstackview.Duration
import io.github.orizynpx.arxwipe.ui.cardstackview.RewindAnimationSetting
import io.github.orizynpx.arxwipe.ui.cardstackview.StackFrom
import io.github.orizynpx.arxwipe.ui.cardstackview.SwipeAnimationSetting
import io.github.orizynpx.arxwipe.ui.cardstackview.SwipeableMethod
import io.github.orizynpx.arxwipe.ui.dialogs.CollectionDialogs
import io.github.orizynpx.arxwipe.data.work.TriageSyncWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class DiscoverFragment : Fragment(), CardStackListener {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var manager: CardStackLayoutManager
    private lateinit var adapter: PaperAdapter
    private var isCardStackInitialized = false
    private var currentPaperIds: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        
        setupCardStackView()
        setupButtons()
        observeViewModel()
        observeWorkManager()
        
        return binding.root
    }

    private fun setupCardStackView() {
        manager = CardStackLayoutManager(requireContext(), this).apply {
            setStackFrom(StackFrom.None)
            setVisibleCount(3)
            setTranslationInterval(8.0f)
            setScaleInterval(0.95f)
            setSwipeThreshold(0.3f)
            setMaxDegree(20.0f)
            setDirections(listOf(Direction.Left, Direction.Right))
            setCanScrollHorizontal(true)
            setCanScrollVertical(false)
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setOverlayInterpolator(LinearInterpolator())
        }
        
        adapter = PaperAdapter()
        binding.csvPapers.layoutManager = manager
        binding.csvPapers.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        updateUiState(state)
                    }
                }
                launch {
                    viewModel.collectionsFlow.collect {  }
                }
            }
        }
    }

    private fun observeWorkManager() {
        WorkManager.getInstance(requireContext())
            .getWorkInfosForUniqueWorkLiveData("TriageSyncWork")
            .observe(viewLifecycleOwner) { workInfos ->
                val failedWork = workInfos?.find { it.state == WorkInfo.State.FAILED }
                if (failedWork != null) {
                    val error = failedWork.outputData.getString("error") ?: "Failed to fetch new papers"
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun updateUiState(state: DiscoverUiState) {
        when (state) {
            is DiscoverUiState.Loading -> {
                setViewsVisibility(loading = true)
            }
            is DiscoverUiState.Success -> {
                setViewsVisibility(content = true)
                
                val paperIds = state.papers.map { it.arxivId }
                if (!isCardStackInitialized || paperIds != currentPaperIds) {
                    adapter.setPapers(state.papers)
                    binding.csvPapers.scrollToPosition(state.currentIndex)
                    currentPaperIds = paperIds
                    isCardStackInitialized = true
                }
                
                updateProgressUI(state.currentIndex, state.papers.size, state.progressPercentage)

                binding.llHeader.background = null
            }
            is DiscoverUiState.Exhausted -> {
                isCardStackInitialized = false
                setViewsVisibility(exhausted = true)
            }
            is DiscoverUiState.Error -> {
                setViewsVisibility(exhausted = true)
                val errorMessage = when (state.type) {
                    DiscoverUiState.ErrorType.NETWORK -> getString(R.string.error_network)
                    DiscoverUiState.ErrorType.TIMEOUT -> getString(R.string.error_timeout)
                    DiscoverUiState.ErrorType.RATE_LIMIT -> getString(R.string.error_rate_limit)
                    DiscoverUiState.ErrorType.EMPTY_RESULT -> getString(R.string.error_empty_result)
                    DiscoverUiState.ErrorType.NO_PAPERS_AVAILABLE -> getString(R.string.error_no_papers_available)
                    DiscoverUiState.ErrorType.GENERAL -> state.message
                }
                binding.tvEmptyState.text = errorMessage
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                Timber.e("Error loading papers: ${state.message}")
            }
        }
    }

    private fun setViewsVisibility(
        loading: Boolean = false,
        content: Boolean = false,
        exhausted: Boolean = false
    ) {
        binding.cpiLoading.visibility = if (loading) View.VISIBLE else View.GONE
        binding.llHeader.visibility = if (content) View.VISIBLE else View.GONE
        binding.csvPapers.visibility = if (content) View.VISIBLE else View.GONE
        binding.llButtons.visibility = if (content) View.VISIBLE else View.GONE
        
        
        binding.llExhausted.visibility = if (exhausted && !loading) View.VISIBLE else View.GONE
        
        
        if (loading) {
            binding.btnArrangeBatch.isEnabled = false
            binding.btnArrangeBatch.alpha = 0.5f
        } else {
            binding.btnArrangeBatch.isEnabled = true
            binding.btnArrangeBatch.alpha = 1.0f
        }
    }

    private fun updateProgressUI(current: Int, total: Int, percentage: Int) {
        val displayCurrent = (current + 1).coerceAtMost(total)
        val text = getString(R.string.paper_progress, displayCurrent, total)
        binding.tvProgress.text = text
        binding.tvPercentage.text = getString(R.string.percentage_format, percentage)
        binding.lpiProgress.progress = percentage
    }

    private fun setupButtons() {
        binding.tbDiscover.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {




                R.id.mi_notifications -> {
                    findNavController().navigate(R.id.action_discover_to_notifications)
                    true
                }
                else -> false
            }
        }

        binding.btnArrangeBatch.setOnClickListener {
            val workRequest = OneTimeWorkRequestBuilder<TriageSyncWorker>()
                .addTag("TriageSync")
                .setInputData(workDataOf(TriageSyncWorker.KEY_FORCE to true))
                .build()
            WorkManager.getInstance(requireContext()).enqueueUniqueWork(
                "TriageSyncWork",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        binding.btnDismiss.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(LinearInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            binding.csvPapers.swipe()
        }

        binding.btnReadingList.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(LinearInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            binding.csvPapers.swipe()
        }

        binding.btnUndo.setOnClickListener {
            viewModel.undoLastSwipe()
            val setting = RewindAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(LinearInterpolator())
                .build()
            manager.setRewindAnimationSetting(setting)
            binding.csvPapers.rewind()
        }
    }

    override fun onCardSwiped(direction: Direction) {
        val state = viewModel.uiState.value
        if (state is DiscoverUiState.Success) {
            val paper = state.papers[state.currentIndex]
            val paperId = paper.arxivId
            val swipeType = if (direction == Direction.Right) SwipeType.SAVE else SwipeType.DISMISS

            if (swipeType == SwipeType.SAVE) {
                val snackbar = Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.added_to_read_later,
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction(R.string.move_action) {
                    _binding?.let {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val collections = viewModel.collectionsFlow.first()
                            if (isAdded) {
                                CollectionDialogs.showMultiChoiceCollectionDialog(
                                    requireContext(),
                                    paperId,
                                    collections
                                ) { collectionId, isChecked ->
                                    viewModel.updatePaperCollection(paperId, collectionId, isChecked)
                                }
                            }
                        }
                    }
                }
                snackbar.show()
            }

            viewModel.swipePaper(paperId, swipeType)
        }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        isCardStackInitialized = false
        currentPaperIds = null
        _binding = null
    }
}
