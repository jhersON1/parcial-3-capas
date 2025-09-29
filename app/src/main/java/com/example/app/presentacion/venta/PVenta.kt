package com.example.app.presentacion.venta

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
import com.example.app.databinding.ActivityPventaBinding
import com.example.app.negocio.venta.NVenta
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PVenta : BaseActivity() {
    private var cantidad: Int = 0
    private var fechaHora: String = ""
    private var idProducto: Int = 0
    private var precioUnitario: Double = 0.0
    private val items: MutableList<Map<String, Any>> = mutableListOf()

    private lateinit var b: ActivityPventaBinding
    private lateinit var n: NVenta

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
            val pu = it["precio_unitario"] as Double
            holder.t1.text = "Prod: $idP  Cant: $cant"
            holder.t2.text = "P. Unit: $pu  Subtotal: ${cant * pu}"
        }
        override fun getItemCount() = items.size
    }

    class ItemVH(root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val t1: TextView = root.findViewById(android.R.id.text1)
        val t2: TextView = root.findViewById(android.R.id.text2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPventaBinding.inflate(layoutInflater)
        setContentView(b.root)
        n = NVenta(applicationContext)

        setupToolbar()
        setupRecyclerView()
        setupBottomNavigation(b.bottomNavigation, R.id.nav_ventas)

        b.btnAgregar.setOnClickListener { btnAgregarItem() }
        b.btnConfirmar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val ok = btnConfirmar()
                withContext(Dispatchers.Main) {
                    snack(if (ok) "Venta registrada" else "No se pudo registrar")
                    if (ok) btnNuevaVenta()
                }
            }
        }
        b.btnNueva.setOnClickListener { btnNuevaVenta() }
        b.btnEliminarItem.setOnClickListener {
            val idx = b.etIndex.editText?.text.toString().toIntOrNull() ?: -1
            btnEliminarItem(idx)
        }

        fechaHora = ahora()
        b.tvFecha.text = fechaHora
    }
    
    private fun setupToolbar() {
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupRecyclerView() {
        b.rv.apply {
            layoutManager = LinearLayoutManager(this@PVenta)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@PVenta.adapter
        }
    }

    private fun ahora(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // ---- Métodos exactos del diagrama ----
    fun btnAgregarItem() {
        idProducto = b.etIdProducto.editText?.text.toString().toIntOrNull() ?: 0
        cantidad = b.etCantidad.editText?.text.toString().toIntOrNull() ?: 0
        precioUnitario = b.etPrecioUnitario.editText?.text.toString().toDoubleOrNull() ?: 0.0
        if (!n.validarItem(idProducto, cantidad, precioUnitario)) {
            snack("Item inválido")
            return
        }
        items.add(mapOf(
            "id_producto" to idProducto,
            "cantidad" to cantidad,
            "precio_unitario" to precioUnitario
        ))
        adapter.notifyItemInserted(items.lastIndex)
        b.etCantidad.editText?.setText("")
        b.etIdProducto.editText?.setText("")
        b.etPrecioUnitario.editText?.setText("")
        b.tvTotal.text = "Total: ${items.sumOf { (it["cantidad"] as Int) * (it["precio_unitario"] as Double) }}"
    }

    fun btnConfirmar(): Boolean {
        fechaHora = ahora()
        return n.registrar(fechaHora, items)
    }

    fun btnEliminarItem(index:Int) {
        if (index in items.indices) {
            items.removeAt(index)
            adapter.notifyDataSetChanged()
        }
    }

    fun btnNuevaVenta() {
        items.clear(); adapter.notifyDataSetChanged()
        fechaHora = ahora(); b.tvFecha.text = fechaHora
        b.tvTotal.text = "Total: 0"
    }

    private fun snack(msg:String) = Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
}
