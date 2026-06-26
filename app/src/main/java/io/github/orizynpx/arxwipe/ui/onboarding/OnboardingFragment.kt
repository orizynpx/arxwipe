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
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.databinding.FragmentOnboardingBinding
import io.github.orizynpx.arxwipe.ui.MainActivity
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by viewModels()
    private val selectedCategories = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            viewModel.savePreferences(selectedCategories.toList(), 20) {
                (activity as? MainActivity)?.onOnboardingComplete()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    binding.tabLayout.removeAllViews()
                    categories.forEach { category ->
                        val chip = Chip(requireContext()).apply {
                            text = category.displayName
                            isCheckable = true
                            setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    selectedCategories.add(category.categoryId)
                                } else {
                                    selectedCategories.remove(category.categoryId)
                                }
                            }
                        }
                        binding.tabLayout.addView(chip)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}