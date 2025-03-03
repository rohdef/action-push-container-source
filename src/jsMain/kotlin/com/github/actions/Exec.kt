package com.github.actions

import kotlin.js.Promise

@JsModule("@actions/exec")
@JsNonModule
external object Exec {
    fun getExecOutput(
        name: String,
        args: Array<String>?,
    ): Promise<ExecOutput> // ExecOutput
}

external interface ExecOutput {
    val exitCode: Int
    val stdout: String
    val stderr: String
}