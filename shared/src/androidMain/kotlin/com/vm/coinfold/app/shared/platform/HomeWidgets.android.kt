package com.vm.coinfold.app.shared.platform

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.widget_add_expense
import coinfold.shared.generated.resources.widget_hidden
import coinfold.shared.generated.resources.widget_title
import com.vm.coinfold.app.feature.expenses.domain.usecases.GetPeriodBalanceUseCase
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.shared.domain.models.ResolvedLanguage
import com.vm.coinfold.app.utils.format
import java.util.Locale
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString
import org.koin.core.context.GlobalContext

/** The widget provider lives in the app module (it needs the app's layout); it is addressed by name. */
private const val WIDGET_PROVIDER_CLASS = "com.vm.coinfold.app.QuickAddWidgetProvider"

actual object HomeWidgets {
    actual fun refresh() {
        val context = GlobalContext.getOrNull()?.get<Context>() ?: return
        val component = ComponentName(context.packageName, WIDGET_PROVIDER_CLASS)
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(component)
        if (ids.isEmpty()) return
        context.sendBroadcast(
            Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setComponent(component)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids),
        )
    }
}

/** The three texts of the widget. */
class WidgetContent(val title: String, val amount: String, val addLabel: String)

/** Gives the widget provider in the app module what to show, using the app's own data layer. */
object WidgetBridge {
    suspend fun load(): WidgetContent {
        val koin = GlobalContext.get()
        val settings = koin.get<SettingsRepository>().settings.first()

        // The widget has its own process lifetime, so apply the language chosen in the app before reading strings.
        val language = when (settings.language) {
            AppLanguage.UK -> ResolvedLanguage.UK
            AppLanguage.EN -> ResolvedLanguage.EN
            AppLanguage.SYSTEM -> if (Locale.getDefault().language == "uk") ResolvedLanguage.UK else ResolvedLanguage.EN
        }
        if (settings.language != AppLanguage.SYSTEM) {
            Locale.setDefault(Locale.forLanguageTag(if (language == ResolvedLanguage.UK) "uk" else "en"))
        }

        // With a PIN set the amounts must not be readable from the home screen.
        val protectedByPin = koin.get<SecurityRepository>().isPinSet.first()
        val amount = if (protectedByPin) {
            getString(Res.string.widget_hidden)
        } else {
            koin.get<GetPeriodBalanceUseCase>().invoke().remaining.format(language)
        }
        return WidgetContent(
            title = getString(Res.string.widget_title),
            amount = amount,
            addLabel = getString(Res.string.widget_add_expense),
        )
    }
}
