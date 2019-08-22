package com.sglasman.tvqueue.models

data class TVDBCredentials(val username: String, val userKey: String, val apiKey: String)

fun readCredsFromFile(): TVDBCredentials =