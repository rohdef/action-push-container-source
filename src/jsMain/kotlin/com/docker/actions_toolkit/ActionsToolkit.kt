package com.docker.actions_toolkit

import kotlin.js.Promise

external class ActionsToolkit {
    suspend fun run(
        main: () -> Promise<Unit>,
        post: (() -> Promise<Unit>)?,
    )
}