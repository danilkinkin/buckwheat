package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPriceChange
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Date

/**
 * Full-fidelity JSON backup. CSV is one-way and loses categories, subscriptions and service
 * links; this format is intended to be restored, not read by a spreadsheet.
 */
object BackupCodec {
    private const val FORMAT = "eva-backup"
    private const val VERSION = 1

    data class Backup(
        val categories: List<Category>,
        val subscriptions: List<Subscription>,
        val transactions: List<Transaction>,
        val priceChanges: List<SubscriptionPriceChange> = emptyList(),
    )

    fun encode(backup: Backup): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)

        val categories = JSONArray()
        backup.categories.forEach { category ->
            categories.put(
                JSONObject().apply {
                    put("id", category.id)
                    put("name", category.name)
                    put("emoji", category.emoji)
                    put("icon", category.icon ?: JSONObject.NULL)
                    put("color", category.color)
                }
            )
        }
        root.put("categories", categories)

        val subscriptions = JSONArray()
        backup.subscriptions.forEach { subscription ->
            subscriptions.put(
                JSONObject().apply {
                    put("id", subscription.id)
                    put("name", subscription.name)
                    put("amount", subscription.amount.toPlainString())
                    put("period", subscription.period)
                    put("intervalCount", subscription.intervalCount)
                    put("anchorDay", subscription.anchorDay)
                    put("nextDue", subscription.nextDue.time)
                    put("categoryId", subscription.categoryId ?: JSONObject.NULL)
                    put("serviceId", subscription.serviceId ?: JSONObject.NULL)
                    put("active", subscription.active)
                }
            )
        }
        root.put("subscriptions", subscriptions)

        val transactions = JSONArray()
        backup.transactions.forEach { transaction ->
            transactions.put(
                JSONObject().apply {
                    put("uid", transaction.uid)
                    put("type", transaction.type.name)
                    put("value", transaction.value.toPlainString())
                    put("date", transaction.date.time)
                    put("comment", transaction.comment)
                    put("categoryId", transaction.categoryId ?: JSONObject.NULL)
                    put("serviceId", transaction.serviceId ?: JSONObject.NULL)
                }
            )
        }
        root.put("transactions", transactions)

        val priceChanges = JSONArray()
        backup.priceChanges.forEach { change ->
            priceChanges.put(
                JSONObject().apply {
                    put("id", change.id)
                    put("subscriptionId", change.subscriptionId)
                    put("name", change.name)
                    put("oldAmount", change.oldAmount.toPlainString())
                    put("newAmount", change.newAmount.toPlainString())
                    put("changedAt", change.changedAt.time)
                }
            )
        }
        root.put("priceChanges", priceChanges)

        return root.toString(2)
    }

    fun decode(json: String): Backup {
        val root = JSONObject(json)

        require(root.optString("format") == FORMAT) { "Not a Khata backup" }

        val version = root.optInt("version", 0)
        require(version in 1..VERSION) { "Unsupported backup version: $version" }

        val categories = mutableListOf<Category>()
        val categoryArray = root.optJSONArray("categories") ?: JSONArray()
        for (index in 0 until categoryArray.length()) {
            val item = categoryArray.getJSONObject(index)
            categories.add(
                Category(
                    id = item.getInt("id"),
                    name = item.getString("name"),
                    emoji = item.optString("emoji"),
                    icon = item.nullableString("icon"),
                    color = item.getLong("color"),
                )
            )
        }

        val subscriptions = mutableListOf<Subscription>()
        val subscriptionArray = root.optJSONArray("subscriptions") ?: JSONArray()
        for (index in 0 until subscriptionArray.length()) {
            val item = subscriptionArray.getJSONObject(index)
            subscriptions.add(
                Subscription(
                    id = item.getInt("id"),
                    name = item.getString("name"),
                    amount = BigDecimal(item.getString("amount")),
                    period = item.getString("period"),
                    intervalCount = item.optInt("intervalCount", 1),
                    anchorDay = item.getInt("anchorDay"),
                    nextDue = Date(item.getLong("nextDue")),
                    categoryId = item.nullableInt("categoryId"),
                    serviceId = item.nullableString("serviceId"),
                    active = item.optBoolean("active", true),
                )
            )
        }

        val transactions = mutableListOf<Transaction>()
        val transactionArray = root.optJSONArray("transactions") ?: JSONArray()
        for (index in 0 until transactionArray.length()) {
            val item = transactionArray.getJSONObject(index)
            transactions.add(
                Transaction(
                    type = TransactionType.valueOf(item.getString("type")),
                    value = BigDecimal(item.getString("value")),
                    date = Date(item.getLong("date")),
                    comment = item.optString("comment"),
                    categoryId = item.nullableInt("categoryId"),
                    serviceId = item.nullableString("serviceId"),
                ).apply {
                    uid = item.getInt("uid")
                }
            )
        }

        val priceChanges = mutableListOf<SubscriptionPriceChange>()
        val priceChangeArray = root.optJSONArray("priceChanges") ?: JSONArray()
        for (index in 0 until priceChangeArray.length()) {
            val item = priceChangeArray.getJSONObject(index)
            priceChanges.add(
                SubscriptionPriceChange(
                    id = item.getInt("id"),
                    subscriptionId = item.getInt("subscriptionId"),
                    name = item.getString("name"),
                    oldAmount = BigDecimal(item.getString("oldAmount")),
                    newAmount = BigDecimal(item.getString("newAmount")),
                    changedAt = Date(item.getLong("changedAt")),
                )
            )
        }

        return Backup(categories, subscriptions, transactions, priceChanges)
    }

    private fun JSONObject.nullableInt(key: String): Int? =
        if (isNull(key)) null else getInt(key)

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else getString(key)
}
