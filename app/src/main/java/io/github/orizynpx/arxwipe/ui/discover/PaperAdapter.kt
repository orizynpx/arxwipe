package io.github.orizynpx.arxwipe.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.ItemCardBinding
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper

class PaperAdapter(private var papers: List<ArxivPaper> = emptyList()) : RecyclerView.Adapter<PaperAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paper = papers[position]
        holder.bind(paper)
    }

    override fun getItemCount(): Int = papers.size

    fun setPapers(newPapers: List<ArxivPaper>) {
        val diffCallback = PaperDiffCallback(papers, newPapers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.papers = newPapers
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(paper: ArxivPaper) {
            binding.paperTitle.text = paper.title
            binding.paperAuthors.text = paper.authors.joinToString(", ") { it.name }
            binding.paperAuthors.maxLines = 2
            binding.paperAuthors.setOnClickListener {
                if (binding.paperAuthors.maxLines == 2) {
                    binding.paperAuthors.maxLines = Int.MAX_VALUE
                } else {
                    binding.paperAuthors.maxLines = 2
                }
            }
            binding.paperAbstract.text = paper.summary

            binding.chipGroup.removeAllViews()
            val context = binding.root.context

            
            val primaryChip = Chip(context)
            primaryChip.text = paper.primaryCategory.categoryId
            primaryChip.setChipBackgroundColorResource(R.color.md_theme_primaryContainer)
            primaryChip.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onPrimaryContainer))
            primaryChip.chipStrokeWidth = 0f
            binding.chipGroup.addView(primaryChip)

            
            paper.allCategories
                .distinctBy { it.categoryId }
                .filter { it.categoryId != paper.primaryCategory.categoryId }
                .forEach { tag ->
                    val chip = Chip(context)
                    chip.text = tag.categoryId
                    chip.setChipBackgroundColorResource(android.R.color.transparent)
                    chip.setChipStrokeColorResource(R.color.md_theme_outlineVariant)
                    chip.chipStrokeWidth = 1f
                    binding.chipGroup.addView(chip)
                }

            binding.root.setOnClickListener {
                it.findNavController().navigate(
                    R.id.action_discover_to_paperDetails,
                    bundleOf("arxivId" to paper.arxivId)
                )
            }
        }
    }

    private class PaperDiffCallback(
        private val oldList: List<ArxivPaper>,
        private val newList: List<ArxivPaper>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].arxivId == newList[newItemPosition].arxivId
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}