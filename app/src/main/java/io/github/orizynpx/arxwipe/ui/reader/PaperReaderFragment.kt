package io.github.orizynpx.arxwipe.ui.reader

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.databinding.FragmentPaperReaderBinding
import io.github.orizynpx.arxwipe.domain.model.ArxivPaper
import io.github.orizynpx.arxwipe.domain.repository.PaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PaperReaderFragment : Fragment() {

    @Inject
    lateinit var repository: PaperRepository

    @Inject
    lateinit var okHttpClient: OkHttpClient

    private var _binding: FragmentPaperReaderBinding? = null
    private val binding get() = _binding!!

    private var pdfRenderer: PdfRenderer? = null
    private var pdfDescriptor: ParcelFileDescriptor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaperReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tbReader.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.tbReader.title = getString(R.string.reading_view)

        val arxivId = arguments?.getString("arxiv_id")
        val readerMode = arguments?.getString("reader_mode")

        if (arxivId != null) {
            loadPaper(arxivId, readerMode)
        } else {
            showError(getString(R.string.error_no_paper_id))
        }
    }

    private fun loadPaper(arxivId: String, readerMode: String?) {
        binding.pbLoading.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch {
            val paper = repository.getPaperById(arxivId)
            if (paper != null) {
                
                handlePaperReading(paper, readerMode)
            } else {
                showError(getString(R.string.error_paper_not_found))
            }
        }
    }

    private fun handlePaperReading(paper: ArxivPaper, readerMode: String?) {
        when {
            readerMode == "HTML" && paper.htmlUrl != null -> {
                showHtml(paper.htmlUrl)
            }
            readerMode == "PDF" && paper.pdfUrl != null -> {
                downloadAndOpenPdf(paper.pdfUrl, paper.arxivId)
            }
            paper.htmlUrl != null -> {
                showHtml(paper.htmlUrl)
            }
            paper.pdfUrl != null -> {
                downloadAndOpenPdf(paper.pdfUrl, paper.arxivId)
            }
            else -> {
                showError(getString(R.string.error_no_readable_format))
            }
        }
    }

    private fun showHtml(url: String) {
        _binding?.let { b ->
            b.pbLoading.isVisible = false
            b.wvReader.isVisible = true

            b.wvReader.apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        _binding?.pbLoading?.isVisible = (newProgress != 100)
                    }
                }
                loadUrl(url)
            }
        }
    }

    private fun downloadAndOpenPdf(url: String, arxivId: String) {
        binding.pbLoading.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = File(requireContext().cacheDir, "papers")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                
                val fileName = arxivId.replace("/", "_") + ".pdf"
                val file = File(cacheDir, fileName)

                if (!file.exists()) {
                    val request = Request.Builder().url(url).build()
                    val response = okHttpClient.newCall(request).execute()
                    
                    if (!response.isSuccessful) throw Exception("Download failed")
                    
                    response.body?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    _binding?.let { b ->
                        b.pbLoading.isVisible = false
                        showPdf(file)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError(getString(R.string.error_download_failed, e.message ?: ""))
                }
            }
        }
    }

    private fun showPdf(file: File) {
        try {
            closePdf()
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(descriptor)
            pdfDescriptor = descriptor
            pdfRenderer = renderer

            val pageWidth = resources.displayMetrics.widthPixels

            _binding?.apply {
                wvReader.isVisible = false
                rvPdfPages.isVisible = true
                rvPdfPages.layoutManager = LinearLayoutManager(requireContext())
                rvPdfPages.adapter = PdfPageAdapter(renderer, pageWidth)
            }
        } catch (e: Exception) {
            showError(getString(R.string.error_pdf_open_failed, e.message ?: ""))
        }
    }

    private fun closePdf() {
        _binding?.rvPdfPages?.adapter = null
        pdfRenderer?.close()
        pdfRenderer = null
        pdfDescriptor?.close()
        pdfDescriptor = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closePdf()
        _binding = null
    }

    private fun showError(message: String) {
        _binding?.apply {
            pbLoading.isVisible = false
            tvError.text = message
            tvError.isVisible = true
        }
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}
