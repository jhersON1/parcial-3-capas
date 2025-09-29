package com.example.app.presentacion.reporte

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.app.R
import com.example.app.base.BaseActivity
import com.example.app.databinding.ActivityPreporteBinding
import com.example.app.negocio.reporte.NReporte
import java.util.Calendar

/**
 * PReporte según diagrama:
 * - fechaFin: string
 * - fechaInicio: string
 * + btnConsultar(): void
 */
class PReporte : BaseActivity() {
    private lateinit var b: ActivityPreporteBinding
    private lateinit var n: NReporte

    private var fechaInicio: String = ""
    private var fechaFin: String = ""

    private val data = mutableListOf<Map<String, Any?>>()
    private val adapter = object : RecyclerView.Adapter<RVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return RVH(v as ViewGroup)
        }
        override fun onBindViewHolder(holder: RVH, position: Int) {
            val it = data[position]
            val id = it["id"] as Int
            val nom = it["nombre"] as String
            val unidades = it["unidades"] as Int
            val total = it["total"] as Double
            holder.t1.text = "$id - $nom"
            holder.t2.text = "Unidades: $unidades  Total: $total"
        }
        override fun getItemCount() = data.size
    }

    class RVH(root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val t1: TextView = root.findViewById(android.R.id.text1)
        val t2: TextView = root.findViewById(android.R.id.text2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPreporteBinding.inflate(layoutInflater)
        setContentView(b.root)
        n = NReporte(applicationContext)

        setupToolbar()
        setupRecyclerView()
        setupBottomNavigation(b.bottomNavigation, R.id.nav_reportes)

        // Date pickers
        b.etDesde.editText?.setOnClickListener { showDate { s -> b.etDesde.editText?.setText(s) } }
        b.etHasta.editText?.setOnClickListener { showDate { s -> b.etHasta.editText?.setText(s) } }

        b.btnConsultar.setOnClickListener { btnConsultar() }
    }

    private fun showDate(onPick:(String)->Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val s = String.format("%04d-%02d-%02d", y, m + 1, d)
            onPick(s)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    // --- Método exacto del diagrama ---
    fun btnConsultar() {
        fechaInicio = b.etDesde.editText?.text.toString()
        fechaFin = b.etHasta.editText?.text.toString()

        val numVentas = n.obtenerNumeroVentas(fechaInicio, fechaFin)
        val total = n.obtenerTotalVendido(fechaInicio, fechaFin)
        b.tvResumen.text = "Ventas: $numVentas   Total vendido: $total"

        data.clear()
        data.addAll(n.obtenerVentasPorProducto(fechaInicio, fechaFin))
        adapter.notifyDataSetChanged()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupRecyclerView() {
        b.rv.apply {
            layoutManager = LinearLayoutManager(this@PReporte)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = this@PReporte.adapter
        }
    }
}
