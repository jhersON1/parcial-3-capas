package com.example.app.presentacion.compra

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.app.R
import com.example.app.base.BaseActivity
import com.example.app.databinding.ActivityPcompraBinding
import com.example.app.negocio.compra.NCompra
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PCompra : BaseActivity() {

    private var cantidad: Int = 0
    private var costoUnitario: Double = 0.0
    private var fechaHora: String = ""
    private var idProducto: Int = 0
    private val items: MutableList<Map<String, Any>> = mutableListOf()

    private lateinit var b: ActivityPcompraBinding
    private lateinit var n: NCompra

    private val adapter = object : RecyclerView.Adapter<ItemVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ItemVH(v as ViewGroup)
        }
        override fun onBindViewHolder(holder: ItemVH, position: Int) {
            val it = items[position]
            val idP = it["id_producto"] as Int
            val cant = it["cantidad"] as Int
            val cu = it["costo_unitario"] as Double
            holder.t1.text = "Prod: $idP  Cant: $cant"
            holder.t2.text = "Costo: $cu  Subtotal: ${cant * cu}"
        }
        override fun getItemCount() = items.size
    }

    class ItemVH(root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val t1: TextView = root.findViewById(android.R.id.text1)
        val t2: TextView = root.findViewById(android.R.id.text2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPcompraBinding.inflate(layoutInflater)
        setContentView(b.root)
        n = NCompra(applicationContext)

        setupToolbar()
        setupRecyclerView()
        setupBottomNavigation(b.bottomNavigation, R.id.nav_compras)

        // Configurar listeners de botones
        b.btnAgregar.setOnClickListener { 
            snack("Botón Agregar presionado")  // Debug
            btnAgregarItem() 
        }
        b.btnConfirmar.setOnClickListener {
            snack("Botón Confirmar presionado")  // Debug
            lifecycleScope.launch(Dispatchers.IO) {
                val ok = btnConfirmar()
                withContext(Dispatchers.Main) {
                    snack(if (ok) "✅ Compra registrada exitosamente" else " No se pudo registrar la compra")
                    if (ok) btnNuevaCompra()
                }
            }
        }
        b.btnNueva.setOnClickListener { 
            snack("Botón Nueva presionado")  // Debug
            btnNuevaCompra() 
        }
        b.btnEliminarItem.setOnClickListener {
            val idx = b.etIndex.editText?.text.toString().toIntOrNull() ?: -1
            btnEliminarItem(idx)
        }

        fechaHora = ahora()
        b.tvFecha.text = "📅 Fecha: $fechaHora"
        
        // Mostrar formulario y estado inicial
        inicializarVistas()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupRecyclerView() {
        b.rv.apply {
            layoutManager = LinearLayoutManager(this@PCompra)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@PCompra.adapter
        }
    }
    
    private fun inicializarVistas() {
        // Asegurar que el formulario siempre esté visible
        b.cardFormulario.visibility = android.view.View.VISIBLE
        
        // Mostrar estado inicial basado en si hay items
        if (items.isEmpty()) {
            b.layoutEmpty.visibility = android.view.View.VISIBLE
            b.rv.visibility = android.view.View.GONE
        } else {
            b.layoutEmpty.visibility = android.view.View.GONE
            b.rv.visibility = android.view.View.VISIBLE
        }
    }

    private fun ahora(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // ---- Métodos exactos del diagrama ----
    fun btnAgregarItem() {
        idProducto = b.etIdProducto.editText?.text.toString().toIntOrNull() ?: 0
        cantidad = b.etCantidad.editText?.text.toString().toIntOrNull() ?: 0
        costoUnitario = b.etCostoUnitario.editText?.text.toString().toDoubleOrNull() ?: 0.0
        
        // Validaciones específicas
        if (idProducto <= 0) {
            snack("Error: Debes ingresar un ID de producto válido (debe existir en Productos)")
            return
        }
        if (cantidad <= 0) {
            snack(" Error: La cantidad debe ser mayor a 0")
            return
        }
        if (costoUnitario < 0) {
            snack("Error: El costo debe ser mayor o igual a 0")
            return
        }
        
        if (!n.validarItem(idProducto, cantidad, costoUnitario)) { 
            snack("Error: El producto con ID $idProducto no existe. Créalo primero en Productos.")
            return 
        }
        
        items.add(mapOf("id_producto" to idProducto, "cantidad" to cantidad, "costo_unitario" to costoUnitario))
        adapter.notifyItemInserted(items.lastIndex)
        
        // Mostrar/ocultar estado vacío
        if (items.isNotEmpty()) {
            b.layoutEmpty.visibility = android.view.View.GONE
            b.rv.visibility = android.view.View.VISIBLE
        }
        
        // Limpiar formulario
        b.etIdProducto.editText?.setText("")
        b.etCantidad.editText?.setText("")
        b.etCostoUnitario.editText?.setText("")
        
        snack("Item agregado correctamente (Stock se actualizará al confirmar)")
    }

    fun btnConfirmar(): Boolean {
        fechaHora = ahora()
        return n.registrar(fechaHora, items)
    }

    fun btnEliminarItem(index:Int) {
        if (index in items.indices) { items.removeAt(index); adapter.notifyDataSetChanged() }
    }

    fun btnNuevaCompra() {
        items.clear()
        adapter.notifyDataSetChanged()
        fechaHora = ahora()
        b.tvFecha.text = "📅 Fecha: $fechaHora"
        
        // Mostrar estado vacío
        b.layoutEmpty.visibility = android.view.View.VISIBLE
        b.rv.visibility = android.view.View.GONE
        
        snack("Nueva compra iniciada")
    }

    private fun snack(msg: String) = Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
}
