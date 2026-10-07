package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import coinfold.shared.generated.resources.category_icon
import coinfold.shared.generated.resources.category_icon_tab_emoji
import coinfold.shared.generated.resources.category_icon_tab_icons
import coinfold.shared.generated.resources.field_color
import org.jetbrains.compose.resources.stringResource

/** Emoji offered next to the vector icons (stored as the emoji text itself). */
val EmojiIconSet: List<String> = listOf(
    "🛒", "🍔", "☕", "🚌", "🚗", "🏠",
    "💡", "📱", "🎬", "👕", "💊", "🎁",
    "✈️", "🐾", "🎓", "🏋️", "💼", "📦",
    "💰", "💳", "🏦", "💎", "🎁", "🍽️",
)

private const val TAB_ICON = 0
private const val TAB_COLOR = 1

/**
 * Two tabs, "Icon" and "Color", shared by the category and account forms. The icon tab has vector icons and
 * emoji; the color tab is the 160-color grid.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconColorPicker(
    icon: String,
    color: Long?,
    onIconChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(TAB_ICON) }
    // Open the icon tab on the group that contains the current icon.
    var showEmoji by remember { mutableStateOf(CategoryIconCatalog.find(icon) == null) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == TAB_ICON,
                onClick = { tab = TAB_ICON },
                text = { Text(stringResource(Res.string.category_icon)) },
            )
            Tab(
                selected = tab == TAB_COLOR,
                onClick = { tab = TAB_COLOR },
                text = { Text(stringResource(Res.string.field_color)) },
            )
        }

        if (tab == TAB_COLOR) {
            ColorGrid(colors = ExtendedColorPalette, selected = color, onSelected = onColorChange)
            return@Column
        }

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
            EmojiIconSet.distinct()
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
                        .clickable { onIconChange(candidate) },
                    contentAlignment = Alignment.Center,
                ) { CategoryIcon(candidate, tint = MaterialTheme.colorScheme.onSurface, size = 24) }
            }
        }
    }
}
