package io.github.orizynpx.arxwipe.ui.dialogs

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.domain.model.SearchFilterCategories


object CategoryFilterSheet {

    fun show(
        context: Context,
        selectedCodes: Set<String>,
        onApply: (Set<String>) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val density = context.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        
        val selected = SearchFilterCategories.allSubcategories
            .filter { sub -> sub.codes.all { it in selectedCodes } }
            .toMutableSet()

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(context).apply {
            text = context.getString(R.string.filter_categories)
            setPadding(dp(16), dp(16), dp(16), dp(8))
            TextViewCompat.setTextAppearance(
                this,
                com.google.android.material.R.style.TextAppearance_Material3_TitleLarge
            )
        }
        root.addView(title)

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(8), dp(8))
        }

        
        val parentRefreshers = mutableListOf<() -> Unit>()
        val allChildBoxes = mutableListOf<MaterialCheckBox>()

        for (group in SearchFilterCategories.groups) {
            val parent = MaterialCheckBox(context).apply {
                text = group.name ?: group.nameRes?.let { context.getString(it) }
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(16), dp(6), dp(16), dp(6))
            }
            content.addView(parent)

            val childBoxes = group.subcategories.map { sub ->
                val box = MaterialCheckBox(context).apply {
                    text = sub.label ?: sub.labelRes?.let { context.getString(it) }
                    isChecked = sub in selected
                    setPadding(dp(40), dp(2), dp(16), dp(2))
                }
                content.addView(box)
                allChildBoxes.add(box)
                box to sub
            }

            fun refreshParent() {
                val checkedCount = childBoxes.count { (_, sub) -> sub in selected }
                parent.checkedState = when (checkedCount) {
                    0 -> MaterialCheckBox.STATE_UNCHECKED
                    childBoxes.size -> MaterialCheckBox.STATE_CHECKED
                    else -> MaterialCheckBox.STATE_INDETERMINATE
                }
            }
            parentRefreshers.add(::refreshParent)

            parent.setOnClickListener {
                val selectAll = childBoxes.any { (_, sub) -> sub !in selected }
                childBoxes.forEach { (box, sub) ->
                    box.isChecked = selectAll
                    if (selectAll) selected.add(sub) else selected.remove(sub)
                }
                refreshParent()
            }

            childBoxes.forEach { (box, sub) ->
                box.setOnClickListener {
                    if (box.isChecked) selected.add(sub) else selected.remove(sub)
                    refreshParent()
                }
            }

            refreshParent()
        }

        val scroll = NestedScrollView(context).apply {
            addView(content)
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (context.resources.displayMetrics.heightPixels * 0.55f).toInt()
            )
        }
        root.addView(scroll)

        val buttonBar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(dp(16), dp(8), dp(16), dp(16))
        }
        val clearButton = MaterialButton(
            context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = context.getString(R.string.clear_all)
            setOnClickListener {
                allChildBoxes.forEach { it.isChecked = false }
                selected.clear()
                parentRefreshers.forEach { it() }
            }
        }
        val applyButton = MaterialButton(context).apply {
            text = context.getString(R.string.show_results)
            (layoutParams as? LinearLayout.LayoutParams ?: LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )).also { it.marginStart = dp(8); layoutParams = it }
            setOnClickListener {
                onApply(selected.flatMap { it.codes }.toSet())
                dialog.dismiss()
            }
        }
        buttonBar.addView(clearButton)
        buttonBar.addView(applyButton)
        root.addView(buttonBar)

        dialog.setContentView(root)
        dialog.show()
    }
}
