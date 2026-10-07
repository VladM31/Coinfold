package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.category_add
import com.vm.coinfold.app.feature.expenses.domain.models.CategorySpend
import com.vm.coinfold.app.feature.expenses.domain.viewmodels.ExpensesIntent
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.format
import org.jetbrains.compose.resources.stringResource

const val GRID_COLUMNS = 4

@Composable
fun CategoryRow(row: List<CategorySpend?>, onIntent: (ExpensesIntent) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        repeat(GRID_COLUMNS) { column ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                if (column < row.size) {
                    val item = row[column]
                    if (item == null) AddCategoryCircle(onIntent) else CategoryCircle(item, onIntent)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCircle(item: CategorySpend, onIntent: (ExpensesIntent) -> Unit) {
    val language = LocalAppLanguage.current
    val category = item.category
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onIntent(ExpensesIntent.CategoryClicked(category)) },
                onLongClick = { onIntent(ExpensesIntent.CategoryLongClicked(category)) },
            )
            .padding(4.dp),
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(Color(category.color)),
            contentAlignment = Alignment.Center,
        ) { Text(category.icon, style = MaterialTheme.typography.titleLarge) }
        Text(category.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (item.spent.minorUnits > 0) {
            Text(
                item.spent.format(language),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddCategoryCircle(onIntent: (ExpensesIntent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = { onIntent(ExpensesIntent.AddCategoryClicked) })
            .padding(4.dp),
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text("+", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) }
        Text(stringResource(Res.string.category_add), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
