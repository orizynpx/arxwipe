package io.github.orizynpx.arxwipe.ui.paperdetails


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.FragmentPaperDetailsBinding
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.ui.dialogs.CollectionDialogs
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaperDetailsFragment : Fragment() {

    private var _binding: FragmentPaperDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaperDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaperDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tbDetails.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val arxivId = arguments?.getString("arxivId")
        if (arxivId != null) {
            viewModel.loadPaper(arxivId)
        } else {
            Snackbar.make(binding.root, R.string.error_no_paper_id, Snackbar.LENGTH_LONG).show()
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PaperDetailsUiState.Loading -> {
                            binding.pbLoading.isVisible = true
                            binding.nsvContent.isVisible = false
                            binding.llActions.isVisible = false
                        }
                        is PaperDetailsUiState.Success -> {
                            binding.pbLoading.isVisible = false
                            binding.nsvContent.isVisible = true
                            binding.llActions.isVisible = true
                            bindPaper(state.paper, state.isSaved, state.collections)
                        }
                        is PaperDetailsUiState.Error -> {
                            binding.pbLoading.isVisible = false
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun bindPaper(paper: ArxivPaper, isSaved: Boolean, collections: List<io.github.orizynpx.arxwipe.domain.model.PaperCollection>) {
        binding.tvTitle.text = paper.title
        binding.tvAuthors.text = paper.formattedAuthors
        binding.tvAbstract.text = paper.summary
        binding.tvArxivId.text = getString(R.string.label_arxiv_id, paper.arxivId)
        binding.tvPublishedDate.text = getString(R.string.label_published, paper.publishedAt.toString())
        binding.tvUpdatedDate.text = paper.updatedAt?.let { getString(R.string.label_updated, it.toString()) } ?: ""
        binding.tvUpdatedDate.isVisible = paper.updatedAt != null
        binding.tvComment.text = paper.comment ?: ""
        binding.llCommentContainer.isVisible = !paper.comment.isNullOrBlank()

        binding.cgCategories.removeAllViews()
        paper.allCategories.forEach { category ->
            val chip = Chip(requireContext())
            chip.text = category.categoryId
            binding.cgCategories.addView(chip)
        }

        binding.btnReadPdf.isEnabled = paper.hasPdf
        binding.btnReadPdf.setOnClickListener {
            openReader(paper.arxivId, "PDF")
        }

        binding.btnReadHtml.isEnabled = paper.hasHtml
        binding.btnReadHtml.setOnClickListener {
            openReader(paper.arxivId, "HTML")
        }

        binding.btnSave.isEnabled = !isSaved
        binding.btnSave.setOnClickListener {
            binding.btnSave.isEnabled = false
            viewModel.saveToReadLater(paper.arxivId)
            showMoveSnackbar(paper.arxivId, collections)
        }
    }

    private fun showMoveSnackbar(paperId: String, collections: List<io.github.orizynpx.arxwipe.domain.model.PaperCollection>) {
        val snackbar = Snackbar.make(binding.root, R.string.added_to_read_later, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.move_action) {
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
        snackbar.show()
    }

    private fun openReader(arxivId: String, mode: String) {
        val actionId = when (findNavController().currentDestination?.id) {
            R.id.discoverPaperDetailsFragment -> R.id.action_discoverPaperDetails_to_paperReader
            R.id.searchPaperDetailsFragment -> R.id.action_searchPaperDetails_to_paperReader
            R.id.libraryPaperDetailsFragment -> R.id.action_libraryPaperDetails_to_paperReader
            else -> null
        }

        if (actionId != null) {
            val bundle = Bundle().apply {
                putString("arxiv_id", arxivId)
                putString("reader_mode", mode)
            }
            findNavController().navigate(actionId, bundle)
        } else {
            
            val bundle = Bundle().apply {
                putString("arxiv_id", arxivId)
                putString("reader_mode", mode)
            }
            findNavController().navigate(R.id.paperReaderFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
