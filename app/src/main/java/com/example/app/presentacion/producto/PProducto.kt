package com.example.app.presentacion.producto

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.app.R
import com.example.app.base.BaseActivity
import com.example.app.databinding.ActivityPproductoBinding
import com.example.app.negocio.producto.NProducto
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PProducto (Activity)
 * - descripcion: string
 * - id: int  
 * - nombre: string
 * - precio: double
 * - stock: int
 * + btnEliminar(): void
 * + btnListar(string): List
 * + btnModificar(): void
 * + btnRegistrar(): void
 */
class PProducto : BaseActivity() {

    private var descripcion: String = ""
    private var id: Int = 0
    private var nombre: String = ""
    private var precio: Double = 0.0
    private var stock: Int = 0
    private var categoriaId: Int = 0

    private lateinit var b: ActivityPproductoBinding
    private lateinit var n: NProducto

    private val data = mutableListOf<Map<String, Any?>>()
    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPproductoBinding.inflate(layoutInflater)
        setContentView(b.root)
        n = NProducto(applicationContext)

        adapter = ProductoAdapter(
            productos = data,
            onEdit = { id, nombre, descripcion, precio, stock, categoriaId ->
                cargarProductoEnFormulario(id, nombre, descripcion, precio, stock, categoriaId)
            },
            onDelete = { id, nombre ->
                eliminarProductoIndividual(id, nombre)
            }
        )

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFormulario()
        setupBottomNavigation(b.bottomNavigation, R.id.nav_productos)
        
        // Cargar lista al abrir
        btnListar("")
    }
    
    private fun setupToolbar() {
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Remover el menú del toolbar ya que ahora el formulario siempre está visible
    }
    
    private fun setupRecyclerView() {
        b.rv.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@PProducto)
            adapter = this@PProducto.adapter
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
        b.etCategoria.editText?.inputType = InputType.TYPE_CLASS_NUMBER
        b.btnActualizar.text = "Actualizar"
        
        // Configurar botones principales
        b.btnRegistrar.setOnClickListener { btnRegistrar() }
        b.btnActualizar.setOnClickListener {
            if (b.etId.editText.getTextOrEmpty().isEmpty()) {
                snack("Selecciona un producto de la lista para actualizar")
            } else {
                btnModificar()
            }
        }
    }

    // Métodos para botones individuales
    private fun cargarProductoEnFormulario(id: Int, nombre: String, descripcion: String, precio: Double, stock: Int, categoriaId: Int) {
        b.etId.editText?.setText(id.toString())
        b.etNombre.editText?.setText(nombre)
        b.etDescripcion.editText?.setText(descripcion)
        b.etPrecio.editText?.setText(precio.toString())
        b.etStock.editText?.setText(stock.toString())
        b.etCategoria.editText?.setText(categoriaId.toString())
        
        snack("📝 Producto cargado en el formulario para modificar")
    }
    
    private fun eliminarProductoIndividual(id: Int, nombre: String) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de eliminar el producto '$nombre'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val ok = try { 
                        n.eliminar(id) 
                    } catch (e: Exception) { 
                        withContext(Dispatchers.Main) {
                            snack("Error: ${e.message}")
                        }
                        false 
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (ok) {
                            snack("Producto '$nombre' eliminado exitosamente")
                        } else {
                            snack("Error: No se pudo eliminar el producto")
                        }
                        btnListar("")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
        
        dialog.show()
    }

    private fun actualizarDesdeUI() {
        id = b.etId.editText.getIntOrNull() ?: 0
        nombre = b.etNombre.editText.getTextOrEmpty()
        descripcion = b.etDescripcion.editText.getTextOrEmpty()
        precio = b.etPrecio.editText.getDoubleOrNull() ?: 0.0
        stock = b.etStock.editText.getIntOrNull() ?: 0
        categoriaId = b.etCategoria.editText.getIntOrNull() ?: 1
    }

    fun btnModificar() {
        actualizarDesdeUI()
        lifecycleScope.launch(Dispatchers.IO) {
            val ok = try { 
                n.modifica(id, nombre, descripcion, precio, stock, categoriaId) 
            } catch (e: Exception) { 
                withContext(Dispatchers.Main) {
                    snack("Error: ${e.message}")
                }
                false 
            }
            
            withContext(Dispatchers.Main) {
                if (ok) {
                    snack("Producto modificado exitosamente")
                    limpiarFormulario()
                } else {
                    snack("Error: No se pudo modificar el producto")
                }
                btnListar("")
            }
        }
    }

    // ---- Métodos----
    fun btnRegistrar() {
        actualizarDesdeUI()
        
        // Validaciones básicas en la UI
        if (nombre.isBlank()) {
            snack("Error: El nombre del producto es requerido")
            return
        }
        if (precio < 0) {
            snack("Error: El precio debe ser mayor o igual a 0")
            return
        }
        if (stock < 0) {
            snack("Error: El stock debe ser mayor o igual a 0")
            return
        }
        
        lifecycleScope.launch(Dispatchers.IO) {
            val ok = try { 
                n.registra(id, nombre, descripcion, precio, stock, categoriaId) 
            } catch (e: Exception) { 
                withContext(Dispatchers.Main) {
                    snack("Error: ${e.message}")
                }
                false 
            }
            
            withContext(Dispatchers.Main) {
                if (ok) {
                    snack("Producto registrado exitosamente")
                    limpiarFormulario()
                } else {
                    snack("Error: No se pudo registrar el producto")
                }
                btnListar("")
            }
        }
    }

    private fun limpiarFormulario() {
        listOf(
            b.etId, b.etNombre, b.etDescripcion,
            b.etPrecio, b.etStock, b.etCategoria
        ).forEach { it.editText?.setText("") }
    }

    fun btnListar(filtro: String): List<Map<String, Any?>> {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = n.getLista(filtro)
            withContext(Dispatchers.Main) {
                data.clear()
                data.addAll(lista)
                adapter.notifyDataSetChanged()

                // Mostrar/ocultar estado vacío
                if (lista.isEmpty()) {
                    b.layoutEmpty.show()
                    b.rv.hide()
                } else {
                    b.layoutEmpty.hide()
                    b.rv.show()
                }
            }
        }
        return n.getLista(filtro)
    }

    private fun snack(msg: String) = Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()

    // ========== Extension Functions ==========

    /**
     * Extension para obtener texto de EditText de forma segura
     */
    private fun android.widget.EditText?.getTextOrEmpty(): String = this?.text?.toString()?.trim() ?: ""

    /**
     * Extension para obtener Int de EditText de forma segura
     */
    private fun android.widget.EditText?.getIntOrNull(): Int? = getTextOrEmpty().toIntOrNull()

    /**
     * Extension para obtener Double de EditText de forma segura
     */
    private fun android.widget.EditText?.getDoubleOrNull(): Double? = getTextOrEmpty().toDoubleOrNull()

    /**
     * Extension para mostrar una vista
     */
    private fun View.show() {
        visibility = View.VISIBLE
    }

    /**
     * Extension para ocultar una vista
     */
    private fun View.hide() {
        visibility = View.GONE
    }
}

// ========== Adapter y ViewHolder ==========

/**
 * Adapter para la lista de productos
 */
class ProductoAdapter(
    private val productos: MutableList<Map<String, Any?>>,
    private val onEdit: (Int, String, String, Double, Int, Int) -> Unit,
    private val onDelete: (Int, String) -> Unit
) : RecyclerView.Adapter<ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false) as ViewGroup
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        val id = (producto["id"] as? Number)?.toInt() ?: 0
        val nombre = producto["nombre"] as? String ?: ""
        val descripcion = producto["descripcion"] as? String ?: ""
        val precio = (producto["precio"] as? Number)?.toDouble() ?: 0.0
        val stock = (producto["stock"] as? Number)?.toInt() ?: 0
        val categoriaId = (producto["categoria_id"] as? Number)?.toInt() ?: 1

        holder.bind(id, nombre, descripcion, precio, stock, categoriaId, onEdit, onDelete)
    }

    override fun getItemCount(): Int = productos.size
}

/**
 * ViewHolder para items de producto
 */
class ProductoViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root) {
    private val txtId: TextView = root.findViewById(R.id.txtId)
    private val txtNombre: TextView = root.findViewById(R.id.txtNombre)
    private val txtDescripcion: TextView = root.findViewById(R.id.txtDescripcion)
    private val chipPrecio: Chip = root.findViewById(R.id.chipPrecio)
    private val chipStock: Chip = root.findViewById(R.id.chipStock)
    private val btnModificar: MaterialButton = root.findViewById(R.id.btnModificar)
    private val btnEliminar: MaterialButton = root.findViewById(R.id.btnEliminar)

    fun bind(
        id: Int,
        nombre: String,
        descripcion: String,
        precio: Double,
        stock: Int,
        categoriaId: Int,
        onEdit: (Int, String, String, Double, Int, Int) -> Unit,
        onDelete: (Int, String) -> Unit
    ) {
        txtId.text = "ID: $id"
        txtNombre.text = nombre
        txtDescripcion.text = descripcion
        chipPrecio.text = "%.2f bs".format(precio)
        chipStock.text = "Stock: $stock"
        btnModificar.text = "📝"
        btnEliminar.text = "🗑️"

        // Cambiar color del chip según stock
        val stockColor = when {
            stock < 10 -> android.R.color.holo_red_light
            stock < 50 -> android.R.color.holo_orange_light
            else -> android.R.color.holo_green_light
        }
        chipStock.setChipBackgroundColorResource(stockColor)

        // Configurar listeners
        btnModificar.setOnClickListener {
            onEdit(id, nombre, descripcion, precio, stock, categoriaId)
        }

        btnEliminar.setOnClickListener {
            onDelete(id, nombre)
        }
    }
}
