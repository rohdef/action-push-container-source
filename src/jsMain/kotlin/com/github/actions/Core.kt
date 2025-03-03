package com.github.actions

import kotlin.js.Promise

@JsModule("@actions/core")
@JsNonModule
external object Core {
    fun getInput(name: String): String

    fun <T> group(name: String, block: () -> Promise<T>): Promise<T>

    fun setOutput(name: String, value: Any)
    fun setFailed(message: String)

    fun debug(message: String)
    fun notice(message: String)
    fun info(message: String)
    fun warning(message: String)
    fun error(message: String)
}