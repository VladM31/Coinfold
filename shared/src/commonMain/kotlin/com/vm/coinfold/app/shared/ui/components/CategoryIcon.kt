package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AirportShuttle
import androidx.compose.material.icons.outlined.Atm
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Attractions
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Balcony
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Blender
import androidx.compose.material.icons.outlined.Bloodtype
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Boy
import androidx.compose.material.icons.outlined.BreakfastDining
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CarRepair
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Commute
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsBoat
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Doorbell
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.DryCleaning
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Elderly
import androidx.compose.material.icons.outlined.ElectricBike
import androidx.compose.material.icons.outlined.ElectricCar
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EmojiFoodBeverage
import androidx.compose.material.icons.outlined.EmojiNature
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Face3
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Festival
import androidx.compose.material.icons.outlined.Fireplace
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Garage
import androidx.compose.material.icons.outlined.Girl
import androidx.compose.material.icons.outlined.GolfCourse
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Iron
import androidx.compose.material.icons.outlined.Kayaking
import androidx.compose.material.icons.outlined.KebabDining
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Liquor
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Masks
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Microwave
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Moped
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PedalBike
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.RamenDining
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Roofing
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Sailing
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Shower
import androidx.compose.material.icons.outlined.Sick
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.Skateboarding
import androidx.compose.material.icons.outlined.SmokeFree
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Snowboarding
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Sports
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsGymnastics
import androidx.compose.material.icons.outlined.SportsMartialArts
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.SportsVolleyball
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Stream
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Subway
import androidx.compose.material.icons.outlined.Surfing
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.Tram
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material.icons.outlined.WineBar
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.Yard
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
            "store" to Icons.Outlined.Store,
            "storefront" to Icons.Outlined.Storefront,
            "local_mall" to Icons.Outlined.LocalMall,
            "sell" to Icons.Outlined.Sell,
            "local_offer" to Icons.Outlined.LocalOffer,
            "redeem" to Icons.Outlined.Redeem,
            "inventory2" to Icons.Outlined.Inventory2,
            "watch" to Icons.Outlined.Watch,
            "diamond" to Icons.Outlined.Diamond,
            "dry_cleaning" to Icons.Outlined.DryCleaning,
            "face" to Icons.Outlined.Face,
            "face3" to Icons.Outlined.Face3,
            "boy" to Icons.Outlined.Boy,
            "girl" to Icons.Outlined.Girl,
            "local_pizza" to Icons.Outlined.LocalPizza,
            "local_bar" to Icons.Outlined.LocalBar,
            "local_dining" to Icons.Outlined.LocalDining,
            "icecream" to Icons.Outlined.Icecream,
            "bakery_dining" to Icons.Outlined.BakeryDining,
            "lunch_dining" to Icons.Outlined.LunchDining,
            "ramen_dining" to Icons.Outlined.RamenDining,
            "set_meal" to Icons.Outlined.SetMeal,
            "coffee" to Icons.Outlined.Coffee,
            "local_drink" to Icons.Outlined.LocalDrink,
            "emoji_food_beverage" to Icons.Outlined.EmojiFoodBeverage,
            "liquor" to Icons.Outlined.Liquor,
            "wine_bar" to Icons.Outlined.WineBar,
            "kebab_dining" to Icons.Outlined.KebabDining,
            "dinner_dining" to Icons.Outlined.DinnerDining,
            "breakfast_dining" to Icons.Outlined.BreakfastDining,
            "directions_bike" to Icons.Outlined.DirectionsBike,
            "two_wheeler" to Icons.Outlined.TwoWheeler,
            "local_taxi" to Icons.Outlined.LocalTaxi,
            "subway" to Icons.Outlined.Subway,
            "directions_boat" to Icons.Outlined.DirectionsBoat,
            "electric_car" to Icons.Outlined.ElectricCar,
            "ev_station" to Icons.Outlined.EvStation,
            "local_parking" to Icons.Outlined.LocalParking,
            "commute" to Icons.Outlined.Commute,
            "moped" to Icons.Outlined.Moped,
            "tram" to Icons.Outlined.Tram,
            "local_shipping" to Icons.Outlined.LocalShipping,
            "car_repair" to Icons.Outlined.CarRepair,
            "traffic" to Icons.Outlined.Traffic,
            "sailing" to Icons.Outlined.Sailing,
            "directions_walk" to Icons.Outlined.DirectionsWalk,
            "directions_run" to Icons.Outlined.DirectionsRun,
            "electric_bike" to Icons.Outlined.ElectricBike,
            "pedal_bike" to Icons.Outlined.PedalBike,
            "airport_shuttle" to Icons.Outlined.AirportShuttle,
            "luggage" to Icons.Outlined.Luggage,
            "hotel" to Icons.Outlined.Hotel,
            "beach_access" to Icons.Outlined.BeachAccess,
            "cottage" to Icons.Outlined.Cottage,
            "map" to Icons.Outlined.Map,
            "public" to Icons.Outlined.Public,
            "language" to Icons.Outlined.Language,
            "chair" to Icons.Outlined.Chair,
            "bed" to Icons.Outlined.Bed,
            "kitchen" to Icons.Outlined.Kitchen,
            "lightbulb" to Icons.Outlined.Lightbulb,
            "weekend" to Icons.Outlined.Weekend,
            "yard" to Icons.Outlined.Yard,
            "plumbing" to Icons.Outlined.Plumbing,
            "cleaning_services" to Icons.Outlined.CleaningServices,
            "thermostat" to Icons.Outlined.Thermostat,
            "router" to Icons.Outlined.Router,
            "tv" to Icons.Outlined.Tv,
            "computer" to Icons.Outlined.Computer,
            "laptop" to Icons.Outlined.Laptop,
            "headphones" to Icons.Outlined.Headphones,
            "handyman" to Icons.Outlined.Handyman,
            "construction" to Icons.Outlined.Construction,
            "doorbell" to Icons.Outlined.Doorbell,
            "fireplace" to Icons.Outlined.Fireplace,
            "balcony" to Icons.Outlined.Balcony,
            "garage" to Icons.Outlined.Garage,
            "roofing" to Icons.Outlined.Roofing,
            "window" to Icons.Outlined.Window,
            "blender" to Icons.Outlined.Blender,
            "microwave" to Icons.Outlined.Microwave,
            "iron" to Icons.Outlined.Iron,
            "palette" to Icons.Outlined.Palette,
            "theater_comedy" to Icons.Outlined.TheaterComedy,
            "casino" to Icons.Outlined.Casino,
            "camera_alt" to Icons.Outlined.CameraAlt,
            "photo_camera" to Icons.Outlined.PhotoCamera,
            "games" to Icons.Outlined.Games,
            "mic" to Icons.Outlined.Mic,
            "piano" to Icons.Outlined.Piano,
            "headset" to Icons.Outlined.Headset,
            "attractions" to Icons.Outlined.Attractions,
            "festival" to Icons.Outlined.Festival,
            "hiking" to Icons.Outlined.Hiking,
            "surfing" to Icons.Outlined.Surfing,
            "pool" to Icons.Outlined.Pool,
            "golf_course" to Icons.Outlined.GolfCourse,
            "sports_soccer" to Icons.Outlined.SportsSoccer,
            "sports_basketball" to Icons.Outlined.SportsBasketball,
            "sports_tennis" to Icons.Outlined.SportsTennis,
            "sports_volleyball" to Icons.Outlined.SportsVolleyball,
            "sports_martial_arts" to Icons.Outlined.SportsMartialArts,
            "sports_gymnastics" to Icons.Outlined.SportsGymnastics,
            "self_improvement" to Icons.Outlined.SelfImprovement,
            "kayaking" to Icons.Outlined.Kayaking,
            "skateboarding" to Icons.Outlined.Skateboarding,
            "snowboarding" to Icons.Outlined.Snowboarding,
            "sports" to Icons.Outlined.Sports,
            "stadium" to Icons.Outlined.Stadium,
            "forest" to Icons.Outlined.Forest,
            "eco" to Icons.Outlined.Eco,
            "nature" to Icons.Outlined.Nature,
            "landscape" to Icons.Outlined.Landscape,
            "local_florist" to Icons.Outlined.LocalFlorist,
            "local_activity" to Icons.Outlined.LocalActivity,
            "interests" to Icons.Outlined.Interests,
            "emoji_events" to Icons.Outlined.EmojiEvents,
            "emoji_nature" to Icons.Outlined.EmojiNature,
            "emoji_emotions" to Icons.Outlined.EmojiEmotions,
            "vaccines" to Icons.Outlined.Vaccines,
            "medical_services" to Icons.Outlined.MedicalServices,
            "health_and_safety" to Icons.Outlined.HealthAndSafety,
            "healing" to Icons.Outlined.Healing,
            "psychology" to Icons.Outlined.Psychology,
            "bloodtype" to Icons.Outlined.Bloodtype,
            "sick" to Icons.Outlined.Sick,
            "monitor_heart" to Icons.Outlined.MonitorHeart,
            "masks" to Icons.Outlined.Masks,
            "hearing" to Icons.Outlined.Hearing,
            "visibility" to Icons.Outlined.Visibility,
            "bathtub" to Icons.Outlined.Bathtub,
            "shower" to Icons.Outlined.Shower,
            "smoke_free" to Icons.Outlined.SmokeFree,
            "science" to Icons.Outlined.Science,
            "auto_stories" to Icons.Outlined.AutoStories,
            "local_library" to Icons.Outlined.LocalLibrary,
            "business" to Icons.Outlined.Business,
            "engineering" to Icons.Outlined.Engineering,
            "lock" to Icons.Outlined.Lock,
            "security" to Icons.Outlined.Security,
            "translate" to Icons.Outlined.Translate,
            "calculate" to Icons.Outlined.Calculate,
            "quiz" to Icons.Outlined.Quiz,
            "backpack" to Icons.Outlined.Backpack,
            "edit" to Icons.Outlined.Edit,
            "create" to Icons.Outlined.Create,
            "draw" to Icons.Outlined.Draw,
            "payments" to Icons.Outlined.Payments,
            "paid" to Icons.Outlined.Paid,
            "attach_money" to Icons.Outlined.AttachMoney,
            "monetization_on" to Icons.Outlined.MonetizationOn,
            "money" to Icons.Outlined.Money,
            "account_balance_wallet" to Icons.Outlined.AccountBalanceWallet,
            "currency_exchange" to Icons.Outlined.CurrencyExchange,
            "percent" to Icons.Outlined.Percent,
            "trending_up" to Icons.Outlined.TrendingUp,
            "show_chart" to Icons.Outlined.ShowChart,
            "request_quote" to Icons.Outlined.RequestQuote,
            "receipt_long" to Icons.Outlined.ReceiptLong,
            "wallet" to Icons.Outlined.Wallet,
            "price_check" to Icons.Outlined.PriceCheck,
            "currency_bitcoin" to Icons.Outlined.CurrencyBitcoin,
            "euro" to Icons.Outlined.Euro,
            "toll" to Icons.Outlined.Toll,
            "atm" to Icons.Outlined.Atm,
            "handshake" to Icons.Outlined.Handshake,
            "volunteer_activism" to Icons.Outlined.VolunteerActivism,
            "groups" to Icons.Outlined.Groups,
            "person" to Icons.Outlined.Person,
            "elderly" to Icons.Outlined.Elderly,
            "church" to Icons.Outlined.Church,
            "star" to Icons.Outlined.Star,
            "favorite" to Icons.Outlined.Favorite,
            "flag" to Icons.Outlined.Flag,
            "category" to Icons.Outlined.Category,
            "cloud" to Icons.Outlined.Cloud,
            "phone" to Icons.Outlined.Phone,
            "phone_iphone" to Icons.Outlined.PhoneIphone,
            "sim_card" to Icons.Outlined.SimCard,
            "sms" to Icons.Outlined.Sms,
            "mail" to Icons.Outlined.Mail,
            "print" to Icons.Outlined.Print,
            "podcasts" to Icons.Outlined.Podcasts,
            "stream" to Icons.Outlined.Stream,
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
