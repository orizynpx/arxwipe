package io.github.orizynpx.arxwipe.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.data.sync.FirebaseSyncManager
import io.github.orizynpx.arxwipe.databinding.FragmentOnboardingNewBinding
import io.github.orizynpx.arxwipe.databinding.ItemOnboardingCheckboxBinding
import io.github.orizynpx.arxwipe.databinding.ItemOnboardingHeaderBinding
import io.github.orizynpx.arxwipe.domain.model.MainField
import io.github.orizynpx.arxwipe.domain.model.PaperCategory
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingNewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by viewModels()

    private lateinit var mainFieldAdapter: MainFieldAdapter
    private lateinit var nestedCategoryAdapter: NestedCategoryAdapter

    @Inject
    lateinit var syncManager: FirebaseSyncManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        mainFieldAdapter = MainFieldAdapter { field ->
            viewModel.toggleMainField(field)
        }
        binding.mainFieldRecyclerView.adapter = mainFieldAdapter

        nestedCategoryAdapter = NestedCategoryAdapter(
            onCategoryToggle = { id -> viewModel.toggleCategory(id) },
            onHeaderToggle = { field, isChecked -> viewModel.toggleFieldWithCategories(field, isChecked) }
        )
        binding.subcategoryRecyclerView.adapter = nestedCategoryAdapter

        binding.btnNext.setOnClickListener {
            if (binding.stepFlipper.displayedChild < 2) {
                binding.stepFlipper.showNext()
                updateStepUI()
            } else {
                viewModel.completeOnboarding()
                syncManager.startRealTimeSync()
                if (findNavController().currentDestination?.id == R.id.onboardingFragment) {
                    findNavController().navigate(R.id.action_onboardingFragment_to_navigation_discover)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            if (binding.stepFlipper.displayedChild > 0) {
                binding.stepFlipper.showPrevious()
                updateStepUI()
            }
        }

        binding.batchSizeSlider.addOnChangeListener { _, value, _ ->
            viewModel.setBatchSize(value.toInt())
        }
    }

    private fun updateStepUI() {
        val currentStep = binding.stepFlipper.displayedChild
        binding.btnBack.visibility = if (currentStep == 0) View.GONE else View.VISIBLE
        binding.btnNext.text = if (currentStep == 2) getString(R.string.btn_finish) else getString(R.string.btn_next)
        
        when (currentStep) {
            0 -> {
                binding.titleText.text = getString(R.string.step_select_fields)
                binding.descriptionText.text = getString(R.string.step_select_fields_desc)
            }
            1 -> {
                binding.titleText.text = getString(R.string.step_select_categories)
                binding.descriptionText.text = getString(R.string.step_select_categories_desc)
            }
            2 -> {
                binding.titleText.text = getString(R.string.step_triage_settings)
                binding.descriptionText.text = getString(R.string.step_triage_settings_desc)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    mainFieldAdapter.submitData(MainField.entries, state.selectedMajorFields)
                    nestedCategoryAdapter.submitData(
                        state.selectedMajorFields.toList(),
                        state.availableCategories,
                        state.selectedCategoryIds
                    )
                    binding.batchSizeValue.text = getString(R.string.batch_size_format, state.batchSize)
                    if (binding.batchSizeSlider.value != state.batchSize.toFloat()) {
                        binding.batchSizeSlider.value = state.batchSize.toFloat()
                    }
                }
            }
        }
    }

    private inner class MainFieldAdapter(private val onToggle: (MainField) -> Unit) :
        RecyclerView.Adapter<MainFieldAdapter.ViewHolder>() {
        
        private var items = listOf<MainField>()
        private var selected = setOf<MainField>()

        fun submitData(newItems: List<MainField>, newSelected: Set<MainField>) {
            items = newItems
            selected = newSelected
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemOnboardingCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val binding: ItemOnboardingCheckboxBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(field: MainField) {
                binding.text.text = itemView.context.getString(field.groupNameRes)
                binding.checkbox.setOnCheckedChangeListener(null)
                binding.checkbox.isChecked = selected.contains(field)
                binding.checkbox.setOnCheckedChangeListener { _, _ -> onToggle(field) }
                binding.root.setOnClickListener { binding.checkbox.toggle() }
            }
        }
    }

    private inner class NestedCategoryAdapter(
        private val onCategoryToggle: (String) -> Unit,
        private val onHeaderToggle: (MainField, Boolean) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var flatList = listOf<Any>()
        private var selectedIds = setOf<String>()
        private var selectedFields = setOf<MainField>()

        fun submitData(fields: List<MainField>, allCategories: List<PaperCategory>, selected: Set<String>) {
            selectedIds = selected
            selectedFields = fields.toSet()
            val newList = mutableListOf<Any>()
            fields.forEach { field ->
                newList.add(field)
                val subCategories = allCategories.filter { 
                    it.group == field && !it.categoryId.endsWith(".*") 
                }
                newList.addAll(subCategories)
            }
            flatList = newList
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return if (flatList[position] is MainField) 0 else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                val binding = ItemOnboardingHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            } else {
                val binding = ItemOnboardingCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CategoryViewHolder(binding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = flatList[position]
            if (holder is HeaderViewHolder) holder.bind(item as MainField)
            else if (holder is CategoryViewHolder) holder.bind(item as PaperCategory)
        }

        override fun getItemCount() = flatList.size

        inner class HeaderViewHolder(private val binding: ItemOnboardingHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(field: MainField) {
                binding.text.text = itemView.context.getString(field.groupNameRes)
                binding.checkbox.setOnCheckedChangeListener(null)
                
                val fieldCategories = flatList.filterIsInstance<PaperCategory>().filter { it.group == field }
                val allChecked = fieldCategories.isNotEmpty() && fieldCategories.all { selectedIds.contains(it.categoryId) }
                
                binding.checkbox.isChecked = allChecked
                binding.checkbox.setOnCheckedChangeListener { _, isChecked -> 
                    onHeaderToggle(field, isChecked)
                }
                binding.root.setOnClickListener { binding.checkbox.toggle() }
            }
        }

        inner class CategoryViewHolder(private val binding: ItemOnboardingCheckboxBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(category: PaperCategory) {
                binding.text.text = itemView.context.getString(
                    R.string.category_display_format,
                    category.categoryId,
                    itemView.context.getString(category.displayNameRes)
                )
                binding.checkbox.setOnCheckedChangeListener(null)
                binding.checkbox.isChecked = selectedIds.contains(category.categoryId)
                binding.checkbox.setOnCheckedChangeListener { _, _ -> onCategoryToggle(category.categoryId) }
                binding.root.setOnClickListener { binding.checkbox.toggle() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
