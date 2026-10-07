package com.vm.coinfold.app.config

import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.nav_accounts
import coinfold.shared.generated.resources.nav_expenses
import coinfold.shared.generated.resources.nav_settings
import coinfold.shared.generated.resources.nav_transactions
import org.jetbrains.compose.resources.StringResource

/** Bottom navigation destinations; [Expenses] is the start (main) screen. */
enum class Route(val path: String, val label: StringResource, val glyph: String) {
    Accounts("accounts", Res.string.nav_accounts, "◧"),
    Expenses("expenses", Res.string.nav_expenses, "◔"),
    Transactions("transactions", Res.string.nav_transactions, "≡"),
    Settings("settings", Res.string.nav_settings, "⚙"),
}
