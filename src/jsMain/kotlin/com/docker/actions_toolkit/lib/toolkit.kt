@file:JsModule("@docker/actions-toolkit/lib/toolkit")
@file:JsNonModule
package com.docker.actions_toolkit.lib.toolkit

import kotlin.js.Promise

external class  Toolkit {
    val buildx: Buildx
}

external object Buildx {
    fun isAvailable(): Promise<Boolean>

    fun printVersion(): Promise<Unit>

    fun versionSatisfies(range: String): Promise<Boolean>
}