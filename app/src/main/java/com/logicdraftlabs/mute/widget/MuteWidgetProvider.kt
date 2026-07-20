package com.logicdraftlabs.mute.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.text.format.DateFormat
import java.util.Calendar
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.receiver.ToggleReceiver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

class MuteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, MuteWidgetViews.buildCompact(context))
        }
    }

    companion object {
        /**
         * Called directly by MuteController right after a mute/unmute, so widgets reflect the
         * new state immediately rather than waiting for the system's next update cycle.
         * This updates both the classic RemoteViews widgets and the Glance widget.
         */
        fun updateAllWidgets(context: Context) {
            MuteWidgetViews.updateProvider(
                context = context,
                provider = MuteWidgetProvider::class.java,
                buildViews = MuteWidgetViews::buildCompact
            )
            MuteWidgetViews.updateProvider(
                context = context,
                provider = MuteWideWidgetProvider::class.java,
                buildViews = MuteWidgetViews::buildWide
            )
            // Also update the Glance widget so it reflects the new mute state immediately.
            MainScope().launch {
                updateAll<MuteGlanceWidget>(context)
            }
        }
    }
}

class MuteWideWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, MuteWidgetViews.buildWide(context))
        }
    }
}

private object MuteWidgetViews {

    fun updateProvider(
        context: Context,
        provider: Class<out AppWidgetProvider>,
        buildViews: (Context) -> RemoteViews
    ) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, provider))
        if (ids.isEmpty()) return

        ids.forEach { id -> manager.updateAppWidget(id, buildViews(context)) }
    }

    fun buildCompact(context: Context): RemoteViews {
        val muted = MuteController.isMuted(context)
        val views = RemoteViews(context.packageName, R.layout.widget_mute)

        applyStateChrome(context, views, muted)
        views.setOnClickPendingIntent(R.id.widget_root, togglePendingIntent(context))
        views.setContentDescription(
            R.id.widget_root,
            context.getString(if (muted) R.string.hint_muted else R.string.hint_active)
        )
        return views
    }

    fun buildWide(context: Context): RemoteViews {
        val muted = MuteController.isMuted(context)
        val views = RemoteViews(context.packageName, R.layout.widget_mute_wide)

        applyStateChrome(context, views, muted)
        views.setTextViewText(R.id.widget_detail, wideStatus(context, muted))
        views.setTextColor(
            R.id.widget_detail,
            context.getColor(if (muted) R.color.signal_red else R.color.dim_grey)
        )
        views.setOnClickPendingIntent(R.id.widget_root, togglePendingIntent(context))
        views.setContentDescription(
            R.id.widget_root,
            context.getString(if (muted) R.string.hint_muted else R.string.hint_active)
        )
        return views
    }

    private fun applyStateChrome(context: Context, views: RemoteViews, muted: Boolean) {
        views.setInt(
            R.id.widget_root,
            "setBackgroundResource",
            if (muted) R.drawable.widget_bg_muted else R.drawable.widget_bg_active
        )
        views.setImageViewResource(
            R.id.widget_icon,
            if (muted) R.drawable.ic_tile_muted else R.drawable.ic_tile_active
        )
        views.setInt(
            R.id.widget_icon,
            "setColorFilter",
            context.getColor(if (muted) R.color.signal_red else R.color.pure_white)
        )
        views.setTextColor(
            R.id.widget_text,
            context.getColor(if (muted) R.color.signal_red else R.color.dim_grey)
        )
    }

    private fun wideStatus(context: Context, muted: Boolean): String {
        if (!muted) return context.getString(R.string.widget_status_active)
        val source = PrefsManager.getMuteSource(context)
        if (source is PrefsManager.MuteSource.Scheduled) {
            val schedules = PrefsManager.getSchedules(context).filter { it.enabled }
            val schedule = schedules.find { it.id == source.scheduleId }
            if (schedule != null) {
                val now = System.currentTimeMillis()
                val currentWindow = com.logicdraftlabs.mute.core.ScheduleManager.getNextWindows(schedule).find { now in it.start..it.end }
                if (currentWindow != null) {
                    val c = Calendar.getInstance().apply { timeInMillis = currentWindow.end }
                    return context.getString(R.string.tile_label_scheduled, DateFormat.getTimeFormat(context).format(c.time))
                }
            }
        }

        val restoreAt = PrefsManager.getAutoRestoreAt(context)
        val remaining = restoreAt?.minus(System.currentTimeMillis()) ?: 0L
        if (remaining <= 0L) return context.getString(R.string.widget_status_muted)

        return context.getString(
            R.string.widget_status_muted_countdown,
            formatRemaining(remaining)
        )
    }

    private fun formatRemaining(remainingMillis: Long): String {
        val totalMinutes = ((remainingMillis + 59_999L) / 60_000L).coerceAtLeast(1L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L

        return when {
            hours > 0L && minutes > 0L -> "${hours}h${minutes}m"
            hours > 0L -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    private fun togglePendingIntent(context: Context): PendingIntent {
        val toggleIntent = Intent(context, ToggleReceiver::class.java)
            .setAction(ToggleReceiver.ACTION_TOGGLE_MUTE)
        return PendingIntent.getBroadcast(
            context,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

