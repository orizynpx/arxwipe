package io.github.orizynpx.arxwipe.ui.dialogs

import android.content.Context
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.orizynpx.arxwipe.R
import io.github.orizynpx.arxwipe.domain.model.PaperCollection
import kotlin.uuid.Uuid

object CollectionDialogs {

    fun showMultiChoiceCollectionDialog(
        context: Context,
        paperId: String,
        collections: List<PaperCollection>,
        onSelectionChanged: (collectionId: Uuid, isChecked: Boolean) -> Unit
    ) {
        val collectionNames = collections.map { it.name }.toTypedArray()
        val checkedItems = collections.map { coll -> coll.papers.any { it.arxivId == paperId } }.toBooleanArray()
        
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_move_to_collections)
            .setMultiChoiceItems(collectionNames, checkedItems) { _, which, isChecked ->
                onSelectionChanged(collections[which].collectionId, isChecked)
            }
            .setPositiveButton(R.string.dialog_btn_done, null)
            .show()
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
