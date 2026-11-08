package com.danilkinkin.buckwheat.util

import com.danilkinkin.buckwheat.data.ExtendCurrency
import java.util.Locale

/**
 * Number formatting is locale-specific, not currency-specific: `NumberFormat` has no idea that
 * `USD` should group in thousands while `INR` groups in lakhs. Choosing a currency therefore has
 * to choose a formatting locale too, otherwise a USD amount on an Indian device renders as
 * "10,00,000" and a euro amount uses the device's decimal mark.
 *
 * The map picks the currency's primary locale. Currencies used by many countries (EUR, USD, CHF)
 * fall back to the app locale when it is one of those countries, so a French user still gets
 * French separators for EUR.
 */
private val currencyLocales: Map<String, Locale> = mapOf(
    "INR" to Locale.forLanguageTag("en-IN"),
    "USD" to Locale.forLanguageTag("en-US"),
    "EUR" to Locale.forLanguageTag("de-DE"),
    "GBP" to Locale.forLanguageTag("en-GB"),
    "JPY" to Locale.forLanguageTag("ja-JP"),
    "CNY" to Locale.forLanguageTag("zh-CN"),
    "AUD" to Locale.forLanguageTag("en-AU"),
    "CAD" to Locale.forLanguageTag("en-CA"),
    "CHF" to Locale.forLanguageTag("de-CH"),
    "SGD" to Locale.forLanguageTag("en-SG"),
    "AED" to Locale.forLanguageTag("en-AE"),
    "RUB" to Locale.forLanguageTag("ru-RU"),
    "BRL" to Locale.forLanguageTag("pt-BR"),
    "MXN" to Locale.forLanguageTag("es-MX"),
    "KRW" to Locale.forLanguageTag("ko-KR"),
    "TRY" to Locale.forLanguageTag("tr-TR"),
    "ZAR" to Locale.forLanguageTag("en-ZA"),
    "NZD" to Locale.forLanguageTag("en-NZ"),
    "SEK" to Locale.forLanguageTag("sv-SE"),
    "NOK" to Locale.forLanguageTag("nb-NO"),
    "DKK" to Locale.forLanguageTag("da-DK"),
    "PLN" to Locale.forLanguageTag("pl-PL"),
    "CZK" to Locale.forLanguageTag("cs-CZ"),
    "HUF" to Locale.forLanguageTag("hu-HU"),
    "THB" to Locale.forLanguageTag("th-TH"),
    "IDR" to Locale.forLanguageTag("id-ID"),
    "MYR" to Locale.forLanguageTag("ms-MY"),
    "PHP" to Locale.forLanguageTag("en-PH"),
    "VND" to Locale.forLanguageTag("vi-VN"),
    "ILS" to Locale.forLanguageTag("he-IL"),
    "PKR" to Locale.forLanguageTag("en-PK"),
    "BDT" to Locale.forLanguageTag("bn-BD"),
    "LKR" to Locale.forLanguageTag("si-LK"),
    "NPR" to Locale.forLanguageTag("ne-NP"),
)

/** Eurozone countries, used to keep their own separators when EUR is selected. */
private val euroCountries = setOf(
    "AT", "BE", "CY", "DE", "EE", "ES", "FI", "FR", "GR", "HR", "IE", "IT",
    "LT", "LU", "LV", "MT", "NL", "PT", "SI", "SK",
)

fun formattingLocaleFor(currency: ExtendCurrency): Locale {
    val appLocale = Locale.getDefault()

    if (currency.type !== ExtendCurrency.Type.FROM_LIST) return appLocale

    val code = currency.value ?: return appLocale

    if (code == "EUR" && appLocale.country in euroCountries) return appLocale
    if (code == "USD" && appLocale.country == "US") return appLocale
    if (code == "CHF" && appLocale.country == "CH") return appLocale
    if (code == "CAD" && appLocale.country == "CA") return appLocale
    if (code == "AUD" && appLocale.country == "AU") return appLocale

    // Unknown codes keep the app locale rather than guessing a country's digit shapes.
    return currencyLocales[code] ?: appLocale
}
