package io.github.orizynpx.arxwipe.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.FragmentCollectionDetailsBinding
import io.github.orizynpx.arxwipe.databinding.ItemSavedPaperBinding
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.ui.dialogs.CollectionDialogs
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.uuid.Uuid

@AndroidEntryPoint
class CollectionDetailsFragment : Fragment() {

    private var _binding: FragmentCollectionDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectionDetailsViewModel by viewModels()
    private lateinit var adapter: SavedPaperAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idString = arguments?.getString("collection_id")
        if (idString != null) {
            try {
                viewModel.loadCollection(Uuid.parse(idString), SortOrder.TITLE_AZ)
            } catch (e: Exception) {
                Timber.e(e, "Invalid UUID: $idString")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionDetailsBinding.inflate(inflater, container, false)
        
        binding.tbCollectionDetails.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        
        setupRecyclerView()
        observeViewModel()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvPapers.layoutManager = LinearLayoutManager(requireContext())
        adapter = SavedPaperAdapter()
        binding.rvPapers.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.papers.collect { papers ->
                        adapter.submitList(papers)
                    }
                }
                launch {
                    viewModel.collectionName.collect { name ->
                        binding.tbCollectionDetails.title = name
                    }
                }
                launch {
                    viewModel.collections.collect {  }
                }
            }
        }
    }

    inner class SavedPaperAdapter : RecyclerView.Adapter<SavedPaperAdapter.ViewHolder>() {

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
                binding.tvPaperCategories.text = paper.allCategories.joinToString(" ") { it.categoryId }
                binding.tvPaperTitle.text = paper.title
                binding.tvPaperAuthors.text = paper.authors.joinToString(", ") { it.name }
                
                binding.ibMore.setOnClickListener { view ->
                    val popup = PopupMenu(view.context, view)
                    popup.menuInflater.inflate(R.menu.paper_item_menu, popup.menu)
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.mi_remove -> {
                                viewModel.removePaper(paper.arxivId, viewModel.collectionId ?: return@setOnMenuItemClickListener true)
                                true
                            }
                            R.id.mi_move -> {
                                CollectionDialogs.showMultiChoiceCollectionDialog(
                                    context = view.context,
                                    paperId = paper.arxivId,
                                    collections = viewModel.collections.value,
                                    onSelectionChanged = { collectionId, isChecked ->
                                        if (isChecked) {
                                            viewModel.addPaper(paper.arxivId, collectionId)
                                        } else {
                                            viewModel.removePaper(paper.arxivId, collectionId)
                                        }
                                    }
                                )
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }

                binding.root.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_collectionDetails_to_paperDetails,
                        bundleOf("arxivId" to paper.arxivId)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(collectionId: String) = CollectionDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("collection_id", collectionId)
            }
        }
    }
}
