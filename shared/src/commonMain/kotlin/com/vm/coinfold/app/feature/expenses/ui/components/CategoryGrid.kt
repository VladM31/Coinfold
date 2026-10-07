package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.category_add
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.CategorySpend
import com.vm.coinfold.app.shared.ui.components.CategoryBadge
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.format
import kotlin.math.abs
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

const val GRID_COLUMNS = 4
private val CELL_HEIGHT = 104.dp

/** Moving less than this (in px) between long press and release counts as a plain long press. */
private const val DRAG_THRESHOLD = 12f

/**
 * Category circles in a grid with a trailing "add" cell.
 * - tap: [onCategoryClick] (add an expense), or [onAddClick] on the last cell
 * - long press and release: [onCategoryEdit]
 * - long press and drag: reorder; the new order is reported once on drop via [onReorder]
 *
 * All gestures are handled by one stationary container that works out which cell is under the finger.
 * (Handlers attached to the moving cells themselves would see their own movement as pointer movement and
 * break repeated drags.) Cells are placed by index and animate to their slots when the order changes.
 */
@Composable
fun CategoryGrid(
    items: List<CategorySpend>,
    onCategoryClick: (Category) -> Unit,
    onCategoryEdit: (Category) -> Unit,
    onAddClick: () -> Unit,
    onReorder: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sourceIds = items.map { it.category.id }
    // The order shown while (and right after) dragging. It must be ONE stable state object: the gesture
    // handler below is created once and keeps writing to whatever it captured first. When the stored
    // order changes, this override is simply cleared (see LaunchedEffect) and [sourceIds] shows through.
    var localOrder by remember { mutableStateOf<List<Long>?>(null) }
    val order = localOrder ?: sourceIds
    var draggingId by remember { mutableStateOf<Long?>(null) }
    // Top-left corner of the dragged cell, in grid pixels.
    var dragTopLeft by remember { mutableStateOf(Offset.Zero) }
    val byId = items.associateBy { it.category.id }

    LaunchedEffect(sourceIds) { if (draggingId == null) localOrder = null }

    val currentOrder by rememberUpdatedState(order)
    val currentSource by rememberUpdatedState(sourceIds)
    val clickHandler by rememberUpdatedState(onCategoryClick)
    val editHandler by rememberUpdatedState(onCategoryEdit)
    val addHandler by rememberUpdatedState(onAddClick)
    val reorderHandler by rememberUpdatedState(onReorder)
    val categories by rememberUpdatedState(items.associate { it.category.id to it.category })

    val density = LocalDensity.current
    val cellHeightPx = with(density) { CELL_HEIGHT.toPx() }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val cellWidth = maxWidth / GRID_COLUMNS
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val rows = (order.size + 1 + GRID_COLUMNS - 1) / GRID_COLUMNS

        fun slotOf(index: Int) = Offset((index % GRID_COLUMNS) * cellWidthPx, (index / GRID_COLUMNS) * cellHeightPx)

        fun indexAt(position: Offset): Int {
            val column = (position.x / cellWidthPx).toInt().coerceIn(0, GRID_COLUMNS - 1)
            val row = (position.y / cellHeightPx).toInt().coerceAtLeast(0)
            return row * GRID_COLUMNS + column
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(CELL_HEIGHT * rows)
                .pointerInput(cellWidthPx) {
                    detectTapGestures(
                        onTap = { position ->
                            val index = indexAt(position)
                            val id = currentOrder.getOrNull(index)
                            if (id != null) categories[id]?.let { clickHandler(it) }
                            else if (index == currentOrder.size) addHandler()
                        },
                    )
                }
                .pointerInput(cellWidthPx) {
                    var finger = Offset.Zero
                    var grab = Offset.Zero
                    var moved = 0f
                    var activeId: Long? = null
                    detectDragGesturesAfterLongPress(
                        onDragStart = { position ->
                            val index = indexAt(position)
                            activeId = currentOrder.getOrNull(index)
                            if (activeId != null) {
                                finger = position
                                grab = position - slotOf(index)
                                moved = 0f
                                dragTopLeft = slotOf(index)
                                draggingId = activeId
                            }
                        },
                        onDrag = { change, dragAmount ->
                            val id = activeId ?: return@detectDragGesturesAfterLongPress
                            change.consume()
                            moved += abs(dragAmount.x) + abs(dragAmount.y)
                            finger += dragAmount
                            dragTopLeft = finger - grab
                            // Move the category to the slot under the finger; the others slide aside.
                            val target = indexAt(finger).coerceIn(0, currentOrder.lastIndex)
                            val from = currentOrder.indexOf(id)
                            if (target != from) {
                                localOrder = currentOrder.toMutableList().also {
                                    it.removeAt(from)
                                    it.add(target, id)
                                }
                            }
                        },
                        onDragEnd = {
                            val id = activeId
                            activeId = null
                            draggingId = null
                            when {
                                id == null -> Unit
                                moved < DRAG_THRESHOLD -> {
                                    localOrder = null
                                    categories[id]?.let { editHandler(it) }
                                }
                                currentOrder != currentSource -> reorderHandler(currentOrder)
                                else -> localOrder = null
                            }
                        },
                        onDragCancel = {
                            activeId = null
                            draggingId = null
                            localOrder = null
                        },
                    )
                },
        ) {
            order.forEachIndexed { index, id ->
                val item = byId[id] ?: return@forEachIndexed
                key(id) {
                    val isDragging = id == draggingId
                    val slot = slotOf(index)
                    val animated by animateIntOffsetAsState(
                        IntOffset(slot.x.roundToInt(), slot.y.roundToInt()),
                        label = "slot",
                    )
                    val position = if (isDragging) {
                        IntOffset(dragTopLeft.x.roundToInt(), dragTopLeft.y.roundToInt())
                    } else {
                        animated
                    }
                    Box(
                        Modifier
                            .offset { position }
                            .zIndex(if (isDragging) 1f else 0f)
                            .width(cellWidth)
                            .height(CELL_HEIGHT),
                        contentAlignment = Alignment.TopCenter,
                    ) { CategoryCell(item) }
                }
            }

            // The "add" cell always follows the last category.
            val addSlot = slotOf(order.size)
            Box(
                Modifier
                    .offset { IntOffset(addSlot.x.roundToInt(), addSlot.y.roundToInt()) }
                    .width(cellWidth)
                    .height(CELL_HEIGHT),
                contentAlignment = Alignment.TopCenter,
            ) { AddCategoryCell() }
        }
    }
}

@Composable
private fun CategoryCell(item: CategorySpend) {
    val language = LocalAppLanguage.current
    val category = item.category
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        CategoryBadge(category.icon, Color(category.color), size = 56)
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

@Composable
private fun AddCategoryCell() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text("+", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) }
        Text(stringResource(Res.string.category_add), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
