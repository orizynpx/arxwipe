package io.github.orizynpx.arxwipe.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.FragmentLibraryBinding
import io.github.orizynpx.arxwipe.databinding.ItemCollectionBinding
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import io.github.orizynpx.arxwipe.ui.dialogs.CollectionDialogs

import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var adapter: CollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupFab()
        observeViewModel()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvCollections.layoutManager = LinearLayoutManager(requireContext())
        adapter = CollectionAdapter { collection ->
            findNavController().navigate(
                R.id.action_library_to_collectionDetails,
                bundleOf("collection_id" to collection.collectionId.toString())
            )
        }
        binding.rvCollections.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddCollection.setOnClickListener {
            CollectionDialogs.showCreateEditCollectionDialog(requireContext()) { name ->
                viewModel.createCollection(name)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.collections.collect { collections ->
                    adapter.submitList(collections)
                }
            }
        }
    }

    inner class CollectionAdapter(
        private val onItemClick: (PaperCollection) -> Unit
    ) : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

        private var items = listOf<PaperCollection>()

        fun submitList(newItems: List<PaperCollection>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val collection = items[position]
            holder.bind(collection)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemCollectionBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(collection: PaperCollection) {
                binding.tvCollectionName.text = collection.name
                binding.tvPaperCount.text = getString(R.string.batch_size_format, collection.papers.size)
                
                if (collection.name == itemView.context.getString(R.string.collection_read_later)) {
                    binding.ivCollectionIcon.setImageResource(android.R.drawable.btn_star_big_on)
                    binding.ibMore.visibility = View.GONE
                } else {
                    binding.ivCollectionIcon.setImageResource(android.R.drawable.ic_menu_agenda)
                    binding.ibMore.visibility = View.VISIBLE
                    binding.ibMore.setOnClickListener { view ->
                        val popup = PopupMenu(view.context, view)
                        popup.menuInflater.inflate(R.menu.collection_item_menu, popup.menu)
                        popup.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.mi_edit -> {
                                    CollectionDialogs.showCreateEditCollectionDialog(
                                        view.context,
                                        collection.collectionId.toString(),
                                        collection.name
                                    ) { newName ->
                                        viewModel.updateCollection(collection.collectionId, newName)
                                    }
                                    true
                                }
                                R.id.mi_delete -> {
                                    viewModel.deleteCollection(collection.collectionId)
                                    true
                                }
                                else -> false
                            }
                        }
                        popup.show()
                    }
                }
                
                binding.root.setOnClickListener { onItemClick(collection) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
