package com.vm.coinfold.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.vm.coinfold.app.shared.platform.WidgetBridge
import com.vm.coinfold.app.shared.platform.WidgetContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Home screen widget: what is left this period and a "+ Expense" button that opens the expense sheet.
 * The numbers come from the shared data layer through [WidgetBridge]; with a PIN set they are hidden.
 */
class QuickAddWidgetProvider : AppWidgetProvider() {

    private companion object {
        const val TAG = "QuickAddWidget"
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        // Reading the database must not block the main thread; goAsync keeps the receiver alive meanwhile.
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val content = runCatching { WidgetBridge.load() }
                    .onFailure { Log.w(TAG, "Could not load the widget data", it) }
                    .getOrNull()
                appWidgetIds.forEach { manager.updateAppWidget(it, buildViews(context, content)) }
            } finally {
                pending.finish()
            }
        }
    }

    private fun buildViews(context: Context, content: WidgetContent?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_add)
        if (content != null) {
            views.setTextViewText(R.id.widget_title, content.title)
            views.setTextViewText(R.id.widget_amount, content.amount)
            views.setTextViewText(R.id.widget_add, content.addLabel)
        }
        views.setOnClickPendingIntent(R.id.widget_add, launch(context, requestCode = 1, action = "add_expense"))
        views.setOnClickPendingIntent(R.id.widget_root, launch(context, requestCode = 2, action = null))
        return views
    }

    private fun launch(context: Context, requestCode: Int, action: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (action != null) putExtra(MainActivity.EXTRA_ACTION, action)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
