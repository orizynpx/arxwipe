package io.github.orizynpx.arxwipe.ui.reader

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.orizynpx.arxwipe.databinding.ItemPdfPageBinding


class PdfPageAdapter(
    private val renderer: PdfRenderer,
    private val pageWidthPx: Int
) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

    private val renderLock = Any()

    override fun getItemCount(): Int = renderer.pageCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemPdfPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(renderPage(position))
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        holder.recycle()
    }

    private fun renderPage(index: Int): Bitmap {
        synchronized(renderLock) {
            renderer.openPage(index).use { page ->
                val height = (page.height * (pageWidthPx.toFloat() / page.width)).toInt()
                    .coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(pageWidthPx, height, Bitmap.Config.ARGB_8888)
                
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return bitmap
            }
        }
    }

    class PageViewHolder(
        private val binding: ItemPdfPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bitmap: Bitmap) {
            binding.pdfPageImage.setImageBitmap(bitmap)
        }

        fun recycle() {
            binding.pdfPageImage.setImageDrawable(null)
        }
    }
}
