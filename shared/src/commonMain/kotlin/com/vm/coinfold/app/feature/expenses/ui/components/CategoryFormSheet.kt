package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.category_edit_title
import coinfold.shared.generated.resources.category_new_title
import coinfold.shared.generated.resources.field_name
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.CategoryIcons
import com.vm.coinfold.app.shared.ui.components.CategoryBadge
import com.vm.coinfold.app.shared.ui.components.ColorPalette
import com.vm.coinfold.app.shared.ui.components.IconColorPicker
import org.jetbrains.compose.resources.stringResource

/** Create ([category] == null) or edit a category; the icon and color live in two tabs below the actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFormSheet(
    category: Category?,
    onSave: (name: String, color: Long, icon: String) -> Unit,
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
            // Live preview of the badge as it will look in the grid.
            CategoryBadge(icon, Color(color), size = 64, modifier = Modifier.align(Alignment.CenterHorizontally))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            // Actions sit above the long icon/color lists so they are always reachable without scrolling.
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
            IconColorPicker(
                icon = icon,
                color = color,
                onIconChange = { icon = it },
                onColorChange = { color = it },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
