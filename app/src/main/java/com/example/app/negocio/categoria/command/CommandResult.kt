package com.example.app.negocio.categoria.command

data class CommandResult(
    val success: Boolean,
    val message: String = "",
    val data: Map<String, Any?> = emptyMap()
)

