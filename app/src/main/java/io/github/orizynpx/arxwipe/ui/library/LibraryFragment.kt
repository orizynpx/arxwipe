package io.github.orizynpx.arxwipe.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.orizynpx.arxwipe.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentLibraryBinding.inflate(inflater, container, false).root
    }
}