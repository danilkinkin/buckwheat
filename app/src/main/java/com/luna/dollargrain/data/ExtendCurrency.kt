package com.luna.dollargrain.data

import java.util.Currency


class ExtendCurrency(val value: String? = null, val type: Type) {
    enum class Type { FROM_LIST, CUSTOM, NONE }

    companion object {

        fun getInstance(value: String?): ExtendCurrency {


            val currency = try {
                Currency.getInstance(value)
            } catch (e: Exception) {
                null
            }

            if (currency !== null) return ExtendCurrency(
                value = value,
                type = Type.FROM_LIST
            )
            if (value.isNullOrEmpty() || value == "null") return ExtendCurrency(
                value = null,
                type = Type.NONE
            )

            return ExtendCurrency(value = value, type = Type.CUSTOM)
        }

        fun none(): ExtendCurrency {
            return ExtendCurrency(type = Type.NONE)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ExtendCurrency) return false

        return other.value == this.value && this.type == this.type
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        return result
    }
}