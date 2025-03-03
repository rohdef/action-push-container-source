@file:JsModule("@docker/actions-toolkit/lib/docker/docker")
@file:JsNonModule

package com.docker.actions_toolkit.lib.docker

import kotlin.js.Promise

external object Docker {
    fun configFile(): ConfigFile?
    fun printVersion(): Promise<Unit>
    fun printInfo(): Promise<Unit>
}

external interface ConfigFile {
    val proxies: ProxyConfig?
}

external interface ProxyConfig {
    val httpProxy: String?
    val httpsProxy: String?
    val noProxy: String?
    val ftpProxy: String?
}