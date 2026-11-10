package com.danilkinkin.buckwheat.data

import androidx.annotation.DrawableRes
import com.danilkinkin.buckwheat.R

enum class ServiceGroup { BUNDLES, STREAMING, MUSIC, CLOUD_AI, OTHER }

/**
 * A service or a bundle the user can attach to a subscription. This is display metadata only:
 * picking one fills the name and shows its tile, nothing is charged separately.
 *
 * Logos are CC0 Simple Icons; brands remain trademarks of their owners. A null [logo] falls back
 * to a brand-coloured monogram.
 */
data class SubscriptionService(
    val id: String,
    val name: String,
    val group: ServiceGroup,
    val brandColor: Long,
    @DrawableRes val logo: Int? = null,
    val monogram: String? = null,
)

object ServiceCatalog {
    val services = listOf(
        // Bundles
        SubscriptionService(
            "apple_one_family", "Apple One Family", ServiceGroup.BUNDLES, 0xFF000000,
            R.drawable.ic_service_apple,
        ),
        SubscriptionService(
            "amazon_prime", "Amazon Prime", ServiceGroup.BUNDLES, 0xFF00A8E1,
            R.drawable.ic_service_amazonprime,
        ),
        SubscriptionService(
            "jio", "Jio", ServiceGroup.BUNDLES, 0xFF0A2885, R.drawable.ic_service_jio,
        ),
        SubscriptionService(
            "airtel", "Airtel", ServiceGroup.BUNDLES, 0xFFE40000, R.drawable.ic_service_airtel,
        ),
        SubscriptionService(
            "swiggy_one", "Swiggy One", ServiceGroup.BUNDLES, 0xFFFC8019,
            R.drawable.ic_service_swiggy,
        ),
        SubscriptionService(
            "zomato_one", "Zomato One", ServiceGroup.BUNDLES, 0xFFE23744,
            R.drawable.ic_service_zomato,
        ),

        // Streaming
        SubscriptionService(
            "jiohotstar", "JioHotstar", ServiceGroup.STREAMING, 0xFF101828, monogram = "JH",
        ),
        SubscriptionService(
            "netflix", "Netflix", ServiceGroup.STREAMING, 0xFFE50914,
            R.drawable.ic_service_netflix,
        ),
        SubscriptionService(
            "prime_video", "Prime Video", ServiceGroup.STREAMING, 0xFF1F2E3E,
            R.drawable.ic_service_primevideo,
        ),
        SubscriptionService(
            "apple_tv", "Apple TV+", ServiceGroup.STREAMING, 0xFF000000,
            R.drawable.ic_service_appletv,
        ),
        SubscriptionService(
            "zee5", "Zee5", ServiceGroup.STREAMING, 0xFF8230C6, monogram = "Z5",
        ),
        SubscriptionService(
            "sonyliv", "SonyLIV", ServiceGroup.STREAMING, 0xFF0D1B3E, monogram = "SL",
        ),
        SubscriptionService(
            "aha", "Aha", ServiceGroup.STREAMING, 0xFFFF4E00, monogram = "aha",
        ),
        SubscriptionService(
            "sun_nxt", "Sun NXT", ServiceGroup.STREAMING, 0xFFE31E24, monogram = "SN",
        ),

        // Music
        SubscriptionService(
            "apple_music_family", "Apple Music Family", ServiceGroup.MUSIC, 0xFFFA243C,
            R.drawable.ic_service_applemusic,
        ),
        SubscriptionService(
            "spotify", "Spotify", ServiceGroup.MUSIC, 0xFF1ED760, R.drawable.ic_service_spotify,
        ),
        SubscriptionService(
            "youtube_premium", "YouTube Premium", ServiceGroup.MUSIC, 0xFFFF0000,
            R.drawable.ic_service_youtube,
        ),

        // Cloud & AI
        SubscriptionService(
            "icloud_plus", "iCloud+", ServiceGroup.CLOUD_AI, 0xFF3693F3,
            R.drawable.ic_service_icloud,
        ),
        SubscriptionService(
            "google_one", "Google One", ServiceGroup.CLOUD_AI, 0xFF4285F4,
            R.drawable.ic_service_google,
        ),
        SubscriptionService(
            "opencode", "OpenCode Go", ServiceGroup.CLOUD_AI, 0xFF000000,
            R.drawable.ic_service_opencode,
        ),
        SubscriptionService(
            "chatgpt", "ChatGPT", ServiceGroup.CLOUD_AI, 0xFF412991,
            R.drawable.ic_service_openai,
        ),
        SubscriptionService(
            "claude", "Claude", ServiceGroup.CLOUD_AI, 0xFFD97757, R.drawable.ic_service_claude,
        ),
        SubscriptionService(
            "gemini", "Gemini", ServiceGroup.CLOUD_AI, 0xFF8E75B2,
            R.drawable.ic_service_googlegemini,
        ),
        SubscriptionService(
            "perplexity", "Perplexity", ServiceGroup.CLOUD_AI, 0xFF1FB8CD,
            R.drawable.ic_service_perplexity,
        ),
        SubscriptionService(
            "github_copilot", "GitHub Copilot", ServiceGroup.CLOUD_AI, 0xFF000000,
            R.drawable.ic_service_githubcopilot,
        ),

        // Other
        SubscriptionService(
            "microsoft_365", "Microsoft 365", ServiceGroup.OTHER, 0xFF5E5E5E,
            R.drawable.ic_service_microsoft,
        ),
        SubscriptionService(
            "canva", "Canva", ServiceGroup.OTHER, 0xFF00C4CC, R.drawable.ic_service_canva,
        ),
        SubscriptionService(
            "notion", "Notion", ServiceGroup.OTHER, 0xFF000000, R.drawable.ic_service_notion,
        ),
        SubscriptionService(
            "cult_fit", "Cult.fit", ServiceGroup.OTHER, 0xFFA3E635, monogram = "CF",
        ),
    )

    private val byId = services.associateBy { it.id }

    fun find(id: String?): SubscriptionService? = id?.let(byId::get)
}

/**
 * Category used when a service is selected and the user has not chosen one manually.
 * Resolved by name so it survives category edits and old installs.
 */
val SubscriptionService.defaultCategoryName: String
    get() = when (id) {
        "jio", "airtel" -> "Mobile & Internet"

        "swiggy_one", "zomato_one" -> "Food & Dining"

        "apple_music_family", "spotify", "youtube_premium" -> "Music"

        "cult_fit" -> "Health & Fitness"

        "icloud_plus", "google_one", "opencode", "chatgpt", "claude", "gemini",
        "perplexity", "github_copilot", "microsoft_365", "canva", "notion",
        -> "Digital & AI"

        "jiohotstar", "netflix", "prime_video", "apple_tv", "zee5", "sonyliv",
        "aha", "sun_nxt", "apple_one_family", "amazon_prime",
        -> "Streaming"

        else -> "Other"
    }
