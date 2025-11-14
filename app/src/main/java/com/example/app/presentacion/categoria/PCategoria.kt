package com.example.app.presentacion.categoria

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.example.app.R
import com.example.app.base.BaseActivity
import com.example.app.databinding.ActivityPcategoriaBinding
import com.example.app.negocio.categoria.NCategoria
import com.example.app.negocio.categoria.command.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PCategoria (Activity)
 * - id: int
 * - nombre: string
 * + btnEliminar(): void
 * + btnListar(string): List
 * + btnModificar(): void
 * + btnRegistrar(): void
 */
class PCategoria : BaseActivity() {

    private var id: Int = 0
    private var nombre: String = ""

    data class Item(val id: Int, val nombre: String)

    private lateinit var b: ActivityPcategoriaBinding
    private lateinit var n: NCategoria
    private val invoker = CategoriaInvoker()

    private val items: MutableList<Item> = mutableListOf()
    private lateinit var adapter: CategoriaItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPcategoriaBinding.inflate(layoutInflater)
        setContentView(b.root)
        n = NCategoria(applicationContext)

        adapter = CategoriaItemAdapter(
            items,
            onModificar = { idCat, nom -> btnModificar(idCat, nom) },
            onEliminar = { idCat -> btnEliminar(idCat) }
        )

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFormulario()
        setupBottomNavigation(b.bottomNavigation, R.id.nav_categorias)

        btnListar("")
    }
    
    private fun setupToolbar() {
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupRecyclerView() {
        b.rv.apply {
            layoutManager = LinearLayoutManager(this@PCategoria)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@PCategoria.adapter
        }
    }
    
    private fun setupSearch() {
        b.etFiltro.editText?.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val filtro = s.toString()
                btnListar(filtro)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFormulario() {
        b.etId.editText?.inputType = InputType.TYPE_CLASS_NUMBER
        
        // Configurar botones principales
        b.btnRegistrar.setOnClickListener { btnRegistrar() }
        b.btnListar.setOnClickListener { 
            val filtro = b.etFiltro.editText?.text.toString()
            btnListar(filtro)
        }
    }
    
    private fun btnModificar(categoriaId: Int, nombreActual: String) {
        // Cargar datos en el formulario para modificación
        b.etId.editText?.setText(categoriaId.toString())
        b.etNombre.editText?.setText(nombreActual)
        
        // Hacer scroll hacia arriba para mostrar el formulario
        b.scroll.smoothScrollTo(0, 0)
        
        snack("📝 Datos cargados. Modifica y presiona 'Registrar' para guardar cambios")
    }
    
    private fun btnEliminar(categoriaId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // ================== PATRÓN COMMAND ==================
            // 1. Crear el comando concreto con el Receiver (NCategoria) y parámetros
            val eliminarCommand = EliminarCategoriaCommand(n, categoriaId)
            // 2. Configurar el comando en el Invoker
            invoker.setCommand(eliminarCommand)
            // 3. Ejecutar el comando a través del Invoker
            val res = invoker.executeCommand()
            // ====================================================
            withContext(Dispatchers.Main) {
                snack((if (res.success) "✅" else "❌") + " ${res.message}")
                if (res.success) btnListar("")
            }
        }
    }

    private fun actualizarDesdeUI() {
        id = b.etId.editText?.text.toString().toIntOrNull() ?: 0
        nombre = b.etNombre.editText?.text.toString()
    }

    fun btnRegistrar() {
        actualizarDesdeUI()
        if (nombre.isBlank()) { snack("❌ El nombre de la categoría es requerido"); return }
        lifecycleScope.launch(Dispatchers.IO) {
            // ================== PATRÓN COMMAND ==================
            // 1. Crear el comando concreto (Registrar o Modificar) con el Receiver y parámetros
            val command: CategoriaCommand = if (id > 0) {
                ModificarCategoriaCommand(n, id, nombre)
            } else {
                RegistrarCategoriaCommand(n, 0, nombre)
            }
            // 2. Configurar el comando en el Invoker
            invoker.setCommand(command)
            // 3. Ejecutar el comando a través del Invoker
            val res = invoker.executeCommand()
            // ====================================================
            withContext(Dispatchers.Main) {
                snack((if (res.success) "✅" else "❌") + " ${res.message}")
                if (res.success) { limpiarFormulario(); btnListar("") }
            }
        }
    }

    fun btnListar(filtro: String): List<Map<String, Any?>> {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = n.getTabla(filtro)
            withContext(Dispatchers.Main) {
                items.clear()
                items.addAll(lista.map { Item(((it["id"] as? Number)?.toInt() ?: 0), (it["nombre"] as? String).orEmpty()) })
                adapter.notifyDataSetChanged()
                if (lista.isEmpty()) { b.layoutEmpty.visibility = View.VISIBLE; b.rv.visibility = View.GONE }
                else { b.layoutEmpty.visibility = View.GONE; b.rv.visibility = View.VISIBLE }
            }
        }
        return n.getTabla(filtro)
    }
    
    private fun limpiarFormulario() {
        b.etId.editText?.setText("")
        b.etNombre.editText?.setText("")
    }

    private fun snack(msg: String) {
        Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
    }
}


class CategoriaItemAdapter(
    private val items: MutableList<PCategoria.Item>,
    private val onModificar: (Int, String) -> Unit,
    private val onEliminar: (Int) -> Unit
) : RecyclerView.Adapter<CategoriaItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return CategoriaItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onModificar, onEliminar)
    }

    override fun getItemCount(): Int = items.size
}

class CategoriaItemViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    private val txtId: TextView = root.findViewById(R.id.txtId)
    private val txtNombre: TextView = root.findViewById(R.id.txtNombre)
    private val btnModificar: com.google.android.material.button.MaterialButton = root.findViewById(R.id.btnModificar)
    private val btnEliminar: com.google.android.material.button.MaterialButton = root.findViewById(R.id.btnEliminar)

    fun bind(item: PCategoria.Item, onModificar: (Int, String) -> Unit, onEliminar: (Int) -> Unit) {
        txtId.text = "ID: ${item.id}"
        txtNombre.text = item.nombre
        btnModificar.setOnClickListener { onModificar(item.id, item.nombre) }
        btnEliminar.setOnClickListener { onEliminar(item.id) }
    }
}
