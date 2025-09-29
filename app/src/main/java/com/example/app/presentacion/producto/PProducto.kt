package com.example.app.presentacion.producto

import android.os.Bundle
import android.text.InputType
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.app.R
import com.example.app.base.BaseActivity
import com.example.app.databinding.ActivityPproductoBinding
import com.example.app.negocio.producto.NProducto
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
    private val adapter = object : RecyclerView.Adapter<ProdVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_producto, parent, false) as ViewGroup
            return ProdVH(v)
        }
        override fun onBindViewHolder(holder: ProdVH, position: Int) {
            val it = data[position]
            val id = (it["id"] as? Number)?.toInt() ?: 0
            val nom = it["nombre"] as? String ?: ""
            val desc = it["descripcion"] as? String ?: ""
            val precio = (it["precio"] as? Number)?.toDouble() ?: 0.0
            val stock = (it["stock"] as? Number)?.toInt() ?: 0
            val categoriaId = (it["categoria_id"] as? Number)?.toInt() ?: 1
            
            holder.txtId.text = "ID: $id"
            holder.txtNombre.text = nom
            holder.txtDescripcion.text = desc
            holder.chipPrecio.text = "%.2f bs".format(precio)
            holder.chipStock.text = "Stock: $stock"
            holder.btnModificar.text = "📝"
            holder.btnEliminar.text = "🗑️"
            
            // Cambiar color del chip según stock
            val stockColor = if (stock < 10) {
                android.R.color.holo_red_light
            } else if (stock < 50) {
                android.R.color.holo_orange_light  
            } else {
                android.R.color.holo_green_light
            }
            holder.chipStock.setChipBackgroundColorResource(stockColor)
            
            // Configurar listeners de los botones individuales
            holder.btnModificar.setOnClickListener {
                // Solo cargamos el formulario; el guardado se hace con el botón Actualizar
                cargarProductoEnFormulario(id, nom, desc, precio, stock, categoriaId)
            }
            
            holder.btnEliminar.setOnClickListener {
                eliminarProductoIndividual(id, nom)
            }
        }
        override fun getItemCount() = data.size
    }

    class ProdVH(root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val txtId: TextView = root.findViewById(R.id.txtId)
        val txtNombre: TextView = root.findViewById(R.id.txtNombre)
        val txtDescripcion: TextView = root.findViewById(R.id.txtDescripcion)
        val chipPrecio: com.google.android.material.chip.Chip = root.findViewById(R.id.chipPrecio)
        val chipStock: com.google.android.material.chip.Chip = root.findViewById(R.id.chipStock)
        val btnModificar: com.google.android.material.button.MaterialButton = root.findViewById(R.id.btnModificar)
        val btnEliminar: com.google.android.material.button.MaterialButton = root.findViewById(R.id.btnEliminar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPproductoBinding.inflate(layoutInflater)
        setContentView(b.root)
        n = NProducto(applicationContext)

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
            val idText = b.etId.editText?.text?.toString()?.trim().orEmpty()
            if (idText.isEmpty()) {
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
                            snack(" Error: No se pudo eliminar el producto")
                        }
                        btnListar("")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
        
        dialog.show()
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
                    snack(" Producto modificado exitosamente")
                    limpiarFormulario()
                } else {
                    snack(" Error: No se pudo modificar el producto")
                }
                btnListar("")
            }
        }
    }

    private fun actualizarDesdeUI() {
        id = b.etId.editText?.text.toString().toIntOrNull() ?: 0
        nombre = b.etNombre.editText?.text.toString()
        descripcion = b.etDescripcion.editText?.text.toString()
        precio = b.etPrecio.editText?.text.toString().toDoubleOrNull() ?: 0.0
        stock = b.etStock.editText?.text.toString().toIntOrNull() ?: 0
        categoriaId = b.etCategoria.editText?.text.toString().toIntOrNull() ?: 1
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
                    snack(" Producto registrado exitosamente")
                    limpiarFormulario()
                } else {
                    snack(" Error: No se pudo registrar el producto")
                }
                btnListar("")
            }
        }
    }

    private fun limpiarFormulario() {
        b.etId.editText?.setText("")
        b.etNombre.editText?.setText("")
        b.etDescripcion.editText?.setText("")
        b.etPrecio.editText?.setText("")
        b.etStock.editText?.setText("")
        b.etCategoria.editText?.setText("")
    }

    fun btnListar(filtro: String): List<Map<String, Any?>> {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = n.getLista(filtro)
            withContext(Dispatchers.Main) {
                data.clear()
                data.addAll(lista)
                adapter.notifyDataSetChanged()
                
                // Mostrar/ocultar estado vacío solamente
                if (lista.isEmpty()) {
                    b.layoutEmpty.visibility = android.view.View.VISIBLE
                    b.rv.visibility = android.view.View.GONE
                } else {
                    b.layoutEmpty.visibility = android.view.View.GONE
                    b.rv.visibility = android.view.View.VISIBLE
                }
            }
        }
        return n.getLista(filtro)
    }

    private fun snack(msg: String) = Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
}
