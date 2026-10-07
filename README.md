# Coinfold

Coinfold is an offline-first personal finance tracker for **Android and iOS**, built with **Kotlin Multiplatform** and **Compose Multiplatform**. All data stays on the device. Track your accounts in UAH, USD and EUR, record expenses by category with a built-in calculator, and see where your money goes in a ring chart and a daily overview.

The look is violet and white: mostly white in the light theme, deep violet-tinted dark in the dark theme.

> **Status:** the Android app is built and tested on a real device. The iOS target shares all code and has the iOS-specific pieces implemented, but it has not been built or run yet (it needs a Mac with Xcode).

## Features

### Accounts
- Create accounts in **UAH, USD or EUR** with an initial balance, an icon and a color.
- Balance = initial balance + top-ups - withdrawals, calculated from the transactions.
- **Total balance** card on top: all accounts converted into the main currency you pick.
- Tap an account to open a **calculator sheet** for a top-up (with a source such as salary, debt return, gift, or your own) or a withdrawal. Long-press an account to edit name, balance, currency, icon and color.
- Accounts with transactions are archived instead of deleted, and their currency is locked.

### Expenses (home screen)
- A **ring chart** for the selected period: one segment per category color, a gray segment for uncategorized spending and a green segment for what is left (income minus spending).
- Pick the calculation period: a calendar month or a month starting on any day (for example the 7th to the 6th). Switch between periods with the arrows.
- **Category circles** in a grid. Tap one to add an expense; long-press to edit; long-press and drag to reorder.
- The add-expense sheet has a mini calculator (`+ - x /`, no parentheses), the currency (UAH/USD/EUR, the last used one is remembered), the account, a note and a date. If the currency differs from the account currency the amount is converted and the rate is stored with the transaction.
- Categories have a name, a color from a **160-color palette** and an icon: **226 vector icons** or emoji. New installs start with 14 default categories (Groceries, Pets, Utilities, Health, Gifts, Taxes, Subs, Transport, Cafes, Clothes, Fun, Study, Sport, Other).

### Overview
- Balance of the period, expenses and income cards.
- Daily bar chart with bars stacked by category color.
- Averages from real spending only, never extrapolated: per day, per started week, and per month (the mean over all periods that have expenses).
- Categories ranked by amount with their percentage of total spending.

### Transactions
- All income and expenses grouped by day with a daily subtotal in the main currency.
- Filters: period (all time, current, custom range), account, category and type.
- Edit or delete a transaction; balances are recalculated automatically. Editing keeps the stored exchange rate as long as the currency pair does not change.
- Paged list, so scrolling stays smooth with a long history.

### Settings
- Theme: system, light or dark.
- Language: **English or Ukrainian**, applied immediately without restarting. By default it follows the system language, falling back to English.
- Main currency, period start day (1-28) and the time of the last exchange rate update with a manual refresh.

### Exchange rates
- Rates come from the public **Monobank API** (`/bank/currency`). The buy/sell midpoint is used, or the cross rate when there is none. USD to EUR goes through UAH.
- Rates are cached locally and refreshed at most once every 5 minutes (the API limit). Offline, the last saved rates are used.
- If no rate was ever loaded, cross-currency operations are blocked with a clear message.
- The rate used by a transaction is saved with it and never recalculated afterwards.

## Tech stack

| Area | Library |
| --- | --- |
| UI | Compose Multiplatform 1.12, Material 3 |
| Language | Kotlin 2.4 |
| Database | Room (KMP) with the bundled SQLite driver |
| Settings | DataStore (preferences) |
| Networking | Ktor Client, kotlinx.serialization |
| DI | Koin |
| Navigation | Compose Navigation |
| Dates | kotlinx-datetime |
| Money math | `ionspin/kotlin-multiplatform-bignum` |
| Icons | Material Icons Extended (vector category icons) |
| Tests | kotlin-test, coroutines-test, Ktor MockEngine |

Platform support: Android 8.0+ (API 26) and iOS 15+.

## Architecture

The app uses **MVI**: each screen has an immutable `State`, user `Intent`s and one-off `Effect`s. A `ViewModel` turns intents into state; composables only render the state and send intents. Repositories expose `Flow`s from the database.

Money is never a `Double`. `Money(minorUnits: Long, currency)` stores cents/kopecks, rates and the calculator use `BigDecimal`, and results are rounded half up to the minor unit. In the database an amount is an `INTEGER` plus the currency code as `TEXT`.

### Project layout

```
androidApp/              Android entry point (Application, Activity, manifest, launcher icons)
iosApp/                  iOS entry point (Xcode project, SwiftUI host)
shared/src/
  commonMain/            all shared code and resources (strings in en and uk)
  androidMain/           Android actuals (database builder, HTTP engine, locale override)
  iosMain/               iOS actuals
  commonTest/            unit tests
docs/                    notes (image prompts, how the phone was driven over ADB)
```

Inside `commonMain/kotlin/com/vm/coinfold/app`:

```
config/                  Koin modules, navigation routes
shared/
  db/                    AppDatabase (one database for the whole app), converters
  domain/models/         Money, Currency, Period, IncomeSource, ...
  ui/                    theme, shared components, icon catalog, color palette
utils/                   money and date formatting, amount parser, calculator
feature/
  accounts/  currency/  expenses/  overview/  settings/  transactions/
```

Every feature follows the same structure and dependencies only point one way: `ui -> domain -> db / net`.

```
feature/<name>/
  ui/
    screens/             composable screens
    components/          reusable composables and sheets
  domain/
    models/              domain models
    viewmodels/          ViewModel with State / Intent / Effect
    usecases/            use cases
    services/            pure business logic (calculations, grouping)
    repositories/        repository interfaces
      impls/             implementations
  db/
    entities/            Room entities and query projections
    daos/                Room DAOs
    storages/            non-Room storage (DataStore)
  net/
    dtos/                response DTOs
    models/              models returned by clients
    mappers/             DTO -> model
    clients/             client interfaces
      impls/             implementations (Monobank)
```

Features do not reach into each other's internals; they only use the public interfaces from `domain` (for example expenses use `ConvertMoneyUseCase` from currency). Platform code is limited to `expect/actual` for the database builder, the HTTP engine, DataStore paths and the runtime language override.

## Getting started

Requirements: JDK 21, Android Studio (or IntelliJ IDEA) with the Kotlin Multiplatform plugin, an Android SDK with API 36+. For iOS: macOS with Xcode.

```bash
# build the Android debug APK
./gradlew :androidApp:assembleDebug

# run the unit tests
./gradlew :shared:testAndroidHostTest
```

The APK is written to `androidApp/build/outputs/apk/debug/`. For iOS, open `iosApp/` in Xcode and run it.

Tip: a freshly installed debug build starts slowly because it is JIT-compiled; it starts in about a second once compiled ahead of time or as a release build.

## Testing

The unit tests live in `shared/src/commonTest` and cover the parts where mistakes would cost money:

- `Money` arithmetic, conversion and rounding
- the mini calculator and amount parsing
- exchange rate lookup (direct, inverse, via UAH), caching and the 5-minute throttle
- the Monobank response mapping
- account balances, total balance, expense summaries and the overview statistics
- transaction grouping and editing rules (the stored rate is kept)
- category reordering and the color palette

## Roadmap

Not part of the first version: transfers between accounts, budgets per category, recurring transactions, CSV export, backup and sync, charts by month, PIN and biometrics.

## Notes

- `docs/image-prompts.md` has prompts for generating the app icon, store graphics and illustrations.
- `docs/phone-adb-notes.md` explains how the app was installed and checked on a real phone with ADB.
