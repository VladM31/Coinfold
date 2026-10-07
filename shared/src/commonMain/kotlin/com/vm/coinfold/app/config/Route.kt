package com.vm.coinfold.app.config

import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.nav_accounts
import coinfold.shared.generated.resources.nav_expenses
import coinfold.shared.generated.resources.nav_settings
import coinfold.shared.generated.resources.nav_transactions
import com.vm.coinfold.app.shared.ui.NavIconKind
import org.jetbrains.compose.resources.StringResource

/** Bottom navigation destinations; [Expenses] is the start (main) screen. */
enum class Route(val path: String, val label: StringResource, val icon: NavIconKind) {
    Accounts("accounts", Res.string.nav_accounts, NavIconKind.ACCOUNTS),
    Expenses("expenses", Res.string.nav_expenses, NavIconKind.EXPENSES),
    Transactions("transactions", Res.string.nav_transactions, NavIconKind.TRANSACTIONS),
    Settings("settings", Res.string.nav_settings, NavIconKind.SETTINGS),
}
