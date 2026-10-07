package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Vector icons a category can use. A category icon is stored as a string: either `icon:<key>` for an
 * entry of this catalog, or a plain emoji. Keys are persisted, so never rename an existing one.
 */
object CategoryIconCatalog {
    const val PREFIX = "icon:"

    val icons: List<Pair<String, ImageVector>> by lazy {
        listOf(
            "cart" to Icons.Outlined.ShoppingCart,
            "bag" to Icons.Outlined.ShoppingBag,
            "grocery" to Icons.Outlined.LocalGroceryStore,
            "restaurant" to Icons.Outlined.Restaurant,
            "fastfood" to Icons.Outlined.Fastfood,
            "cafe" to Icons.Outlined.LocalCafe,
            "cake" to Icons.Outlined.Cake,
            "bus" to Icons.Outlined.DirectionsBus,
            "car" to Icons.Outlined.DirectionsCar,
            "fuel" to Icons.Outlined.LocalGasStation,
            "train" to Icons.Outlined.Train,
            "flight" to Icons.Outlined.Flight,
            "home" to Icons.Outlined.Home,
            "electricity" to Icons.Outlined.Bolt,
            "water" to Icons.Outlined.WaterDrop,
            "internet" to Icons.Outlined.Wifi,
            "phone" to Icons.Outlined.PhoneAndroid,
            "laundry" to Icons.Outlined.LocalLaundryService,
            "repair" to Icons.Outlined.Build,
            "movie" to Icons.Outlined.Movie,
            "music" to Icons.Outlined.MusicNote,
            "games" to Icons.Outlined.SportsEsports,
            "celebration" to Icons.Outlined.Celebration,
            "subscriptions" to Icons.Outlined.Subscriptions,
            "park" to Icons.Outlined.Park,
            "art" to Icons.Outlined.Brush,
            "fitness" to Icons.Outlined.FitnessCenter,
            "spa" to Icons.Outlined.Spa,
            "hospital" to Icons.Outlined.LocalHospital,
            "medication" to Icons.Outlined.Medication,
            "child" to Icons.Outlined.ChildCare,
            "pets" to Icons.Outlined.Pets,
            "school" to Icons.Outlined.School,
            "book" to Icons.Outlined.Book,
            "clothes" to Icons.Outlined.Checkroom,
            "gift" to Icons.Outlined.CardGiftcard,
            "work" to Icons.Outlined.Work,
            "bank" to Icons.Outlined.AccountBalance,
            "card" to Icons.Outlined.CreditCard,
            "savings" to Icons.Outlined.Savings,
            "receipt" to Icons.Outlined.Receipt,
            "other" to Icons.Outlined.MoreHoriz,
        )
    }

    private val byKey: Map<String, ImageVector> by lazy { icons.toMap() }

    fun stored(key: String): String = PREFIX + key

    /** The vector for a stored value, or null if it is an emoji (or an unknown key). */
    fun find(stored: String): ImageVector? =
        if (stored.startsWith(PREFIX)) byKey[stored.removePrefix(PREFIX)] else null
}

/** Draws a stored category icon: a vector from the catalog, or the emoji text. */
@Composable
fun CategoryIcon(stored: String, tint: Color, size: Int, modifier: Modifier = Modifier) {
    val vector = CategoryIconCatalog.find(stored)
    if (vector != null) {
        Icon(vector, contentDescription = null, tint = tint, modifier = modifier.size(size.dp))
    } else {
        // Emoji are glyphs, so they ignore the tint and are a little smaller than the icon box.
        Text(stored, fontSize = (size * 0.85f).sp, modifier = modifier)
    }
}

/** Round colored badge with the category icon, used in lists and the category grid. */
@Composable
fun CategoryBadge(stored: String, color: Color, size: Int = 40, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) { CategoryIcon(stored, tint = Color.White, size = size / 2) }
}
