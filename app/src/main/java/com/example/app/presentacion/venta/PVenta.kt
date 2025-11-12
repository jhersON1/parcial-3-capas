// kotlin
package com.example.app.presentacion.venta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    // Atributos según el diagrama (privados)
    private var cantidad: Int = 0
    private var idProducto: Int = 0
    private var precioUnitario: Double = 0.0
    private var fechaHora: String = ""

    // Item dentro de PVenta
    data class Item(val idProducto: Int, val cantidad: Int, val precioUnitario: Double)

    private val items: MutableList<Item> = mutableListOf()

    private lateinit var b: ActivityPventaBinding
    private lateinit var n: NVenta
    private lateinit var adapter: VentaItemAdapter

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPventaBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Wiring correcto según arquitectura 3 capas:
        // NProducto crea DProducto internamente -> NVenta recibe NProducto
        val nProducto = com.example.app.negocio.producto.NProducto(applicationContext)
        n = NVenta(applicationContext, nProducto)

        adapter = VentaItemAdapter(items)

        setupToolbar()
        setupRecyclerView()
        setupButtons()
        setupBottomNavigation(b.bottomNavigation, R.id.nav_ventas)

        fechaHora = fechaAhora()
        b.tvFecha.text = fechaHora
        actualizarTotal()
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

    private fun setupButtons() {
        b.btnAgregar.setOnClickListener { btnAgregarItem() }
        b.btnConfirmar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val ok = btnConfirmar()
                withContext(Dispatchers.Main) {
                    snack(if (ok) "✅ Venta registrada" else "❌ No se pudo registrar")
                    if (ok) btnNuevaVenta()
                }
            }
        }
        b.btnNueva.setOnClickListener { btnNuevaVenta() }
        b.btnEliminarItem.setOnClickListener {
            val idx = b.etIndex.editText.getIntOrNull() ?: -1
            btnEliminarItem(idx)
        }
    }

    private fun fechaAhora(): String = dateFormat.format(Date())

    // ---- Helpers ----
    private fun totalAmount(): Double = items.sumOf { it.cantidad * it.precioUnitario }

    private fun clearInputs() {
        listOf(b.etCantidad, b.etIdProducto, b.etPrecioUnitario)
            .forEach { it.editText?.setText("") }
        // Resetear los atributos que reflejan los inputs
        cantidad = 0
        idProducto = 0
        precioUnitario = 0.0
    }

    private fun actualizarTotal() {
        b.tvTotal.text = "Total: ${"%.2f".format(totalAmount())}"
    }

    // ---- Métodos del diagrama usando los atributos de la clase ----
    fun btnAgregarItem() {
        // Sincronizar atributos con los inputs
        idProducto = b.etIdProducto.editText.getIntOrNull() ?: 0
        cantidad = b.etCantidad.editText.getIntOrNull() ?: 0
        precioUnitario = b.etPrecioUnitario.editText.getDoubleOrNull() ?: 0.0

        if (!n.validarItem(idProducto, cantidad, precioUnitario)) {
            snack("❌ Item inválido")
            return
        }

        items.add(Item(idProducto, cantidad, precioUnitario))
        adapter.notifyItemInserted(items.lastIndex)
        clearInputs()
        actualizarTotal()
        snack("✅ Item agregado")
    }

    fun btnConfirmar(): Boolean {
        fechaHora = fechaAhora()
        return n.registrar(fechaHora, items.map { mapOf(
            "id_producto" to it.idProducto,
            "cantidad" to it.cantidad,
            "precio_unitario" to it.precioUnitario
        ) })
    }

    fun btnEliminarItem(index: Int) {
        if (index in items.indices) {
            items.removeAt(index)
            adapter.notifyItemRemoved(index)
            adapter.notifyItemRangeChanged(index, items.size)
            actualizarTotal()
            snack("🗑️ Item eliminado")
        } else {
            snack("❌ Índice inválido")
        }
    }

    fun btnNuevaVenta() {
        items.clear()
        adapter.notifyDataSetChanged()
        fechaHora = fechaAhora()
        b.tvFecha.text = fechaHora
        actualizarTotal()
        snack("📝 Nueva venta iniciada")
    }

    private fun snack(msg: String) = Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()

    // ========== Extension Functions ==========
    private fun android.widget.EditText?.getTextOrEmpty(): String = this?.text?.toString()?.trim() ?: ""
    private fun android.widget.EditText?.getIntOrNull(): Int? = getTextOrEmpty().toIntOrNull()
    private fun android.widget.EditText?.getDoubleOrNull(): Double? = getTextOrEmpty().toDoubleOrNull()
}

/* Adapter y ViewHolder (misma idea, pueden estar en archivos separados si se prefiere) */

class VentaItemAdapter(
    private val items: MutableList<PVenta.Item>
) : RecyclerView.Adapter<VentaItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VentaItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size
}

class VentaItemViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    private val t1: TextView = root.findViewById(android.R.id.text1)
    private val t2: TextView = root.findViewById(android.R.id.text2)

    fun bind(item: PVenta.Item) {
        t1.text = "Producto ID: ${item.idProducto}  |  Cantidad: ${item.cantidad}"
        val subtotal = item.cantidad * item.precioUnitario
        t2.text = "Precio Unit: ${"%.2f".format(item.precioUnitario)}  |  Subtotal: ${"%.2f".format(subtotal)}"
    }
}
