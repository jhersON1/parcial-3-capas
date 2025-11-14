package com.example.app.negocio.categoria.command

import com.example.app.negocio.categoria.NCategoria

class RegistrarCategoriaCommand(
    private val nCategoria: NCategoria,
    private val id: Int,
    private val nombre: String
) : CategoriaCommand {

    override fun execute(): CommandResult {
        return try {
            val ok = nCategoria.registrar(id, nombre)
            if (ok) CommandResult(true, "Categoría registrada")
            else CommandResult(false, "No se pudo registrar")
        } catch (e: Exception) {
            CommandResult(false, e.message ?: "Error inesperado")
        }
    }
}
