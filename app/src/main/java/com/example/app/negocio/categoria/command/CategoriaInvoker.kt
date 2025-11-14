package com.example.app.negocio.categoria.command

class CategoriaInvoker {
    private var command: CategoriaCommand? = null

    fun setCommand(command: CategoriaCommand) { this.command = command }

    fun executeCommand(): CommandResult {
        val cmd = command ?: return CommandResult(false, "No hay comando configurado")
        return cmd.execute()
    }
}
