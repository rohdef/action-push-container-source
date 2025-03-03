@file:JsModule("@docker/actions-toolkit/lib/context")
@file:JsNonModule
package com.docker.actions_toolkit.lib.context

external object Context {
    fun gitContext(): String
}