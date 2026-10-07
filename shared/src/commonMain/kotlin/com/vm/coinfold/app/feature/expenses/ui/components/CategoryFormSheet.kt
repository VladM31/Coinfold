package com.vm.coinfold.app.feature.expenses.ui.components

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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.category_edit_title
import coinfold.shared.generated.resources.category_icon
import coinfold.shared.generated.resources.category_icon_tab_emoji
import coinfold.shared.generated.resources.category_icon_tab_icons
import coinfold.shared.generated.resources.category_new_title
import coinfold.shared.generated.resources.field_color
import coinfold.shared.generated.resources.field_name
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.CategoryIcons
import com.vm.coinfold.app.shared.ui.components.CategoryBadge
import com.vm.coinfold.app.shared.ui.components.CategoryIcon
import com.vm.coinfold.app.shared.ui.components.CategoryIconCatalog
import com.vm.coinfold.app.shared.ui.components.ColorPalette
import com.vm.coinfold.app.shared.ui.components.ColorPicker
import org.jetbrains.compose.resources.stringResource

/** Create ([category] == null) or edit a category; editing also offers reordering and delete. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    // Open on the tab that contains the current icon.
    var showEmoji by remember { mutableStateOf(CategoryIconCatalog.find(icon) == null) }

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
            // Actions sit above the long icon list so they are always reachable without scrolling.
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
            Text(stringResource(Res.string.field_color), style = MaterialTheme.typography.labelLarge)
            ColorPicker(selected = color, onSelected = { color = it ?: color })
            Text(stringResource(Res.string.category_icon), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showEmoji,
                    onClick = { showEmoji = false },
                    label = { Text(stringResource(Res.string.category_icon_tab_icons)) },
                )
                FilterChip(
                    selected = showEmoji,
                    onClick = { showEmoji = true },
                    label = { Text(stringResource(Res.string.category_icon_tab_emoji)) },
                )
            }
            val candidates = if (showEmoji) {
                CategoryIcons.all
            } else {
                CategoryIconCatalog.icons.map { (key, _) -> CategoryIconCatalog.stored(key) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                candidates.forEach { candidate ->
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
                    ) { CategoryIcon(candidate, tint = MaterialTheme.colorScheme.onSurface, size = 24) }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
