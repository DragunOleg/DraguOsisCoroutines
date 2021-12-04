package com.example.draguosiscoroutines

data class Params(val username: String, val password: String, val org: String, val variant: Variant)

const val PREFS_NAMESPACE = "settings"
const val PREFS_USERNAME = "username"
const val PREFS_PASSWORD = "password"
const val PREFS_ORG = "org"
const val PREFS_CHIP_VARIAN = "variant"

