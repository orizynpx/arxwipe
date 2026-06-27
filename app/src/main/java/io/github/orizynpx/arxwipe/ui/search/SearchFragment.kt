package io.github.orizynpx.arxwipe.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.FragmentSearchBinding
import io.github.orizynpx.arxwipe.databinding.ItemSavedPaperBinding
import io.github.orizynpx.arxwipe.databinding.ItemSearchHistoryBinding
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.ui.dialogs.CategoryFilterSheet
import io.github.orizynpx.arxwipe.ui.dialogs.CollectionDialogs

import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: SearchAdapter
    private lateinit var historyAdapter: SearchHistoryAdapter

    
    private var selectedCategoryCodes: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupHistory()
        setupSearch()
        observeViewModel()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter { paper ->
            CollectionDialogs.showMultiChoiceCollectionDialog(
                context = requireContext(),
                paperId = paper.arxivId,
                collections = viewModel.collections.value,
                onSelectionChanged = { collectionId, isChecked ->
                    if (isChecked) {
                        viewModel.addPaperToCollection(paper.arxivId, collectionId)
                    } else {
                        viewModel.removePaperFromCollection(paper.arxivId, collectionId)
                    }
                }
            )
        }
        binding.rvSearchResults.adapter = adapter

        
        binding.rvSearchResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = rv.layoutManager as LinearLayoutManager
                val total = lm.itemCount
                val lastVisible = lm.findLastVisibleItemPosition()
                if (total > 0 && lastVisible >= total - PREFETCH_THRESHOLD) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun setupHistory() {
        binding.rvSearchHistory.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = SearchHistoryAdapter(
            onClick = { query -> runSearch(query) },
            onRemove = { query -> viewModel.deleteHistory(query) }
        )
        binding.rvSearchHistory.adapter = historyAdapter
    }

    private fun setupSearch() {
        
        binding.svPapers.setupWithSearchBar(binding.sbPapers)

        binding.sbPapers.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_filter_categories -> {
                    showFilterSheet()
                    true
                }
                else -> false
            }
        }

        binding.svPapers.editText.setOnEditorActionListener { _, _, _ ->
            runSearch(binding.svPapers.text.toString())
            true
        }

        
        if (viewModel.uiState.value is SearchUiState.Idle) {
            viewModel.search("", selectedCategoryCodes.toList())
        }
    }

    private fun showFilterSheet() {
        CategoryFilterSheet.show(
            context = requireContext(),
            selectedCodes = selectedCategoryCodes,
            onApply = { codes ->
                selectedCategoryCodes = codes
                
                viewModel.search(binding.sbPapers.text.toString(), codes.toList())
            }
        )
    }

    
    private fun runSearch(query: String) {
        binding.sbPapers.setText(query)
        binding.svPapers.hide()
        viewModel.search(query, selectedCategoryCodes.toList())
    }

    private fun openReader(paper: ArxivPaper, mode: String?) {
        val bundle = Bundle().apply {
            putString("arxiv_id", paper.arxivId)
            if (mode != null) putString("reader_mode", mode)
        }
        findNavController().navigate(R.id.action_search_to_paperReader, bundle)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is SearchUiState.Idle -> {
                                binding.lpiSearch.visibility = View.GONE
                                binding.tvNoResults.visibility = View.GONE
                                binding.rvSearchResults.visibility = View.VISIBLE
                                adapter.submitList(emptyList())
                            }
                            is SearchUiState.Loading -> {
                                binding.lpiSearch.visibility = View.VISIBLE
                                binding.tvNoResults.visibility = View.GONE
                                binding.rvSearchResults.visibility = View.GONE
                            }
                            is SearchUiState.Success -> {
                                binding.lpiSearch.visibility = View.GONE
                                binding.tvNoResults.visibility = View.GONE
                                binding.rvSearchResults.visibility = View.VISIBLE
                                adapter.submitList(state.papers)
                            }
                            is SearchUiState.Empty -> {
                                binding.lpiSearch.visibility = View.GONE
                                binding.tvNoResults.text = getString(R.string.no_results_found)
                                binding.tvNoResults.visibility = View.VISIBLE
                                binding.rvSearchResults.visibility = View.GONE
                                adapter.submitList(emptyList())
                            }
                            is SearchUiState.Error -> {
                                binding.lpiSearch.visibility = View.GONE
                                binding.tvNoResults.text = state.message
                                binding.tvNoResults.visibility = View.VISIBLE
                                binding.rvSearchResults.visibility = View.GONE
                                adapter.submitList(emptyList())
                            }
                        }
                    }
                }
                launch {
                    viewModel.isPaginating.collect { paginating ->
                        binding.pbLoadMore.visibility = if (paginating) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.searchHistory.collect { history ->
                        historyAdapter.submitList(history)
                    }
                }
            }
        }
    }

    inner class SearchAdapter(
        private val onBookmarkClick: (ArxivPaper) -> Unit
    ) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        private var items = listOf<ArxivPaper>()

        fun submitList(newItems: List<ArxivPaper>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSavedPaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val paper = items[position]
            holder.bind(paper)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemSavedPaperBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(paper: ArxivPaper) {
                binding.tvPaperTitle.text = paper.title
                binding.tvPaperAuthors.text = paper.formattedAuthors
                binding.ibMore.setOnClickListener { onBookmarkClick(paper) }
                binding.cgTags.visibility = View.GONE

                
                binding.btnReadDefault.setOnClickListener { openReader(paper, "PDF") }
                binding.btnReadDropdown.setOnClickListener { openReader(paper, "HTML") }
                binding.root.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_search_to_paperDetails,
                        bundleOf("arxivId" to paper.arxivId)
                    )
                }
            }
        }
    }

    inner class SearchHistoryAdapter(
        private val onClick: (String) -> Unit,
        private val onRemove: (String) -> Unit
    ) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

        private var items = listOf<String>()

        fun submitList(newItems: List<String>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSearchHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemSearchHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(query: String) {
                binding.tvHistoryQuery.text = query
                binding.root.setOnClickListener { onClick(query) }
                binding.ibRemoveHistory.setOnClickListener { onRemove(query) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val PREFETCH_THRESHOLD = 5
    }
}
