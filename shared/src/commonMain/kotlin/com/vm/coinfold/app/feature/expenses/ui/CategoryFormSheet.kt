package com.vm.coinfold.app.feature.expenses.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.category_edit_title
import coinfold.shared.generated.resources.category_icon
import coinfold.shared.generated.resources.category_move_earlier
import coinfold.shared.generated.resources.category_move_later
import coinfold.shared.generated.resources.category_new_title
import coinfold.shared.generated.resources.field_color
import coinfold.shared.generated.resources.field_name
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.CategoryIcons
import com.vm.coinfold.app.shared.ui.ColorPalette
import com.vm.coinfold.app.shared.ui.ColorPicker
import org.jetbrains.compose.resources.stringResource

/** Create ([category] == null) or edit a category; editing also offers reordering and delete. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryFormSheet(
    category: Category?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSave: (name: String, color: Long, icon: String) -> Unit,
    onMove: (up: Boolean) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(category?.name.orEmpty()) }
    var color by remember { mutableStateOf(category?.color ?: ColorPalette.first()) }
    var icon by remember { mutableStateOf(category?.icon ?: CategoryIcons.default) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(if (category == null) Res.string.category_new_title else Res.string.category_edit_title),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(Res.string.field_color), style = MaterialTheme.typography.labelLarge)
            ColorPicker(selected = color, onSelected = { color = it ?: color })
            Text(stringResource(Res.string.category_icon), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryIcons.all.forEach { candidate ->
                    val selected = candidate == icon
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .then(
                                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier,
                            )
                            .clickable { icon = candidate },
                        contentAlignment = Alignment.Center,
                    ) { Text(candidate, style = MaterialTheme.typography.titleLarge) }
                }
            }
            if (category != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onMove(true) }, enabled = canMoveUp) {
                        Text("← " + stringResource(Res.string.category_move_earlier))
                    }
                    OutlinedButton(onClick = { onMove(false) }, enabled = canMoveDown) {
                        Text(stringResource(Res.string.category_move_later) + " →")
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Spacer(Modifier)
                }
                Button(enabled = name.isNotBlank(), onClick = { onSave(name, color, icon) }) {
                    Text(stringResource(Res.string.action_save))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
