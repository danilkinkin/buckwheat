package com.danilkinkin.buckwheat.subscriptions

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.danilkinkin.buckwheat.MainActivity
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.budgetDataStore
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.di.DatabaseModule
import com.danilkinkin.buckwheat.di.SeedDefaultCategoriesCallback
import com.danilkinkin.buckwheat.di.currencyStoreKey
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.toLocalDate
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CHANNEL_ID = "subscriptions"

/**
 * Daily catch-up for due subscriptions.
 *
 * Work is intentionally read-only: the app posts the transaction when the user confirms, so a
 * notification can never duplicate an expense. A second, short-lived Room instance is used
 * instead of pulling Hilt into WorkManager's worker factory.
 */
class SubscriptionCheckWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val database = Room.databaseBuilder(
            applicationContext,
            DatabaseModule::class.java,
            "buckwheat-db",
        )
            .addMigrations(*DatabaseModule.MANUAL_MIGRATIONS)
            .addCallback(SeedDefaultCategoriesCallback)
            .build()

        return try {
            val due = database.subscriptionDao().getDue(System.currentTimeMillis())

            if (due.isNotEmpty()) {
                ensureChannel()

                val currencyCode = applicationContext.budgetDataStore.data
                    .first()[currencyStoreKey]
                val currency = ExtendCurrency.getInstance(currencyCode)

                due.forEach { subscription ->
                    notify(subscription, currency)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        } finally {
            database.close()
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.subscriptions_title),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
    }

    private fun notify(subscription: Subscription, currency: ExtendCurrency) {
        val amount = numberFormat(
            applicationContext,
            subscription.amount,
            currency,
            trimDecimalPlaces = true,
        )
        val due = DateTimeFormatter
            .ofPattern("d MMM", Locale.getDefault())
            .format(subscription.nextDue.toLocalDate())

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            subscription.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_balance_wallet)
            .setContentTitle(subscription.name)
            .setContentText(applicationContext.getString(R.string.subscription_due_text, amount, due))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(subscription.id, notification)
    }
}
