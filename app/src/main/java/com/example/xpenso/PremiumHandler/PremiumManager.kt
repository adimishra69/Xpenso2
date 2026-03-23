package com.example.xpenso.PremiumHandler

object PremiumManager {

    private val validCodes = setOf(
        "XPENSO2026",
        "JOHNDEV",
        "FREEPREMIUM" ,
        "JOHN"
    )

    fun isValid(code: String): Boolean {
        return validCodes.contains(code.trim().uppercase())
    }
}