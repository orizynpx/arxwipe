package io.github.orizynpx.arxwipe.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.databinding.FragmentDiscoverBinding
import io.github.orizynpx.arxwipe.ui.cardstackview.CardStackLayoutManager
import io.github.orizynpx.arxwipe.ui.cardstackview.CardStackListener
import io.github.orizynpx.arxwipe.ui.cardstackview.Direction
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiscoverFragment : Fragment(), CardStackListener {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var manager: CardStackLayoutManager
    private lateinit var adapter: PaperAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager = CardStackLayoutManager(requireContext(), this)
        adapter = PaperAdapter()
        binding.cardStackView.layoutManager = manager
        binding.cardStackView.adapter = adapter

        setupButtons()
        observeState()
    }

    private fun setupButtons() {
        binding.btnDismiss.setOnClickListener {
            binding.cardStackView.swipe()
        }
        binding.btnSavePaper.setOnClickListener {
            binding.cardStackView.swipe()
        }
        binding.btnUndo.setOnClickListener {
            viewModel.undo()
            binding.cardStackView.rewind()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DiscoverUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.cardStackView.visibility = View.GONE
                            binding.emptyStateText.visibility = View.GONE
                        }
                        is DiscoverUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.cardStackView.visibility = View.VISIBLE
                            binding.emptyStateText.visibility = View.GONE
                            adapter.setPapers(state.papers)
                            binding.progressText.text = "${state.currentIndex + 1} of ${state.papers.size} papers"
                            val percentage = ((state.currentIndex.toFloat() / state.papers.size) * 100).toInt()
                            binding.progressBar.progress = percentage
                            binding.percentageText.text = "$percentage%"
                        }
                        is DiscoverUiState.Exhausted -> {
                            binding.progressBar.visibility = View.GONE
                            binding.cardStackView.visibility = View.GONE
                            binding.emptyStateText.visibility = View.VISIBLE
                        }
                        is DiscoverUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCardSwiped(direction: Direction?) {
        viewModel.swipe(direction == Direction.Right)
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}