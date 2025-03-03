@file:JsModule("@docker/actions-toolkit/lib/github")
@file:JsNonModule
package com.docker.actions_toolkit.lib.github

import kotlin.js.Promise

external object GitHub {
    fun printActionsRuntimeTokenACs(): Promise<Unit>
}