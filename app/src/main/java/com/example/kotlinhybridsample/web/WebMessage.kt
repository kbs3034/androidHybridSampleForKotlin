package com.example.kotlinhybridsample.web

typealias WebMessageArgs = Map<String, Any>

data class WebMessage(
    val group: String,
    val function: String,
    val callback: String?,
    val args: WebMessageArgs?
)