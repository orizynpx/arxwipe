package io.github.orizynpx.arxwipe.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import io.github.orizynpx.arxwipe.databinding.ItemCardBinding
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.ui.util.formatAbstract

class PaperAdapter(private var papers: List<ArxivPaper> = emptyList()) : RecyclerView.Adapter<PaperAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(papers[position])
    }

    override fun getItemCount(): Int = papers.size

    fun setPapers(newPapers: List<ArxivPaper>) {
        this.papers = newPapers
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(paper: ArxivPaper) {
            binding.paperTitle.text = paper.title
            binding.paperAuthors.text = paper.authors.joinToString(", ") { it.name }
            binding.paperAbstract.text = paper.summary.formatAbstract()

            binding.chipGroup.removeAllViews()
            val context = binding.root.context

            val primaryChip = Chip(context).apply {
                text = paper.primaryCategory.displayName
                isClickable = false
            }
            binding.chipGroup.addView(primaryChip)
        }
    }
}