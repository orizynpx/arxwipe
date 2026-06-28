package io.github.orizynpx.arxwipe.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.ItemCollectionBinding
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import kotlin.uuid.Uuid

object CollectionDialogs {

    fun showMultiChoiceCollectionDialog(
        context: Context,
        paperId: String,
        collections: List<PaperCollection>,
        onSelectionChanged: (collectionId: Uuid, isChecked: Boolean) -> Unit
    ) {
        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CollectionSelectionAdapter(paperId, collections, onSelectionChanged)
            setPadding(0, 24, 0, 24)
            clipToPadding = false
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_move_to_collections)
            .setView(recyclerView)
            .setPositiveButton(R.string.dialog_btn_done, null)
            .show()
    }

    private class CollectionSelectionAdapter(
        private val paperId: String,
        private val collections: List<PaperCollection>,
        private val onSelectionChanged: (collectionId: Uuid, isChecked: Boolean) -> Unit
    ) : RecyclerView.Adapter<CollectionSelectionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(collections[position])
        }

        override fun getItemCount(): Int = collections.size

        inner class ViewHolder(private val binding: ItemCollectionBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(collection: PaperCollection) {
                binding.cbCollection.visibility = android.view.View.VISIBLE
                binding.ibMore.visibility = android.view.View.GONE
                
                binding.tvCollectionName.text = collection.name
                binding.tvPaperCount.text = binding.root.context.getString(R.string.batch_size_format, collection.papers.size)
                
                if (collection.name == binding.root.context.getString(R.string.collection_read_later)) {
                    binding.ivCollectionIcon.setImageResource(android.R.drawable.btn_star_big_on)
                } else {
                    binding.ivCollectionIcon.setImageResource(android.R.drawable.ic_menu_agenda)
                }

                val isChecked = collection.papers.any { it.arxivId == paperId }
                binding.cbCollection.setOnCheckedChangeListener(null)
                binding.cbCollection.isChecked = isChecked
                
                binding.cbCollection.setOnCheckedChangeListener { _, checked ->
                    onSelectionChanged(collection.collectionId, checked)
                }
                
                binding.root.setOnClickListener {
                    binding.cbCollection.toggle()
                }
            }
        }
    }

    fun showCreateEditCollectionDialog(
        context: Context, 
        collectionId: String? = null, 
        currentName: String? = null,
        onSave: (String) -> Unit
    ) {
        val input = EditText(context)
        input.setText(currentName)
        input.setPadding(64, 32, 64, 32)

        MaterialAlertDialogBuilder(context)
            .setTitle(if (collectionId == null) R.string.dialog_new_collection else R.string.dialog_edit_collection)
            .setView(input)
            .setPositiveButton(R.string.dialog_btn_save) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    onSave(name)
                }
            }
            .setNegativeButton(R.string.dialog_btn_cancel, null)
            .show()
    }
}
