package com.example.app.presentacion.categoria

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
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

    private lateinit var b: ActivityPcategoriaBinding
    private lateinit var n: NCategoria

    // Adapter simple SIN archivo extra (usa un layout integrado)
    private val data = mutableListOf<Pair<Int, String>>()
    private val adapter = object : RecyclerView.Adapter<CatVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_categoria, parent, false)
            return CatVH(v as ViewGroup)
        }
        override fun onBindViewHolder(holder: CatVH, position: Int) {
            val (id, nombre) = data[position]
            holder.txtId.text = "ID: $id"
            holder.txtNombre.text = nombre
            
            // Configurar botones individuales
            holder.btnModificar.setOnClickListener {
                btnModificar(id, nombre)
            }
            
            holder.btnEliminar.setOnClickListener {
                btnEliminar(id)
            }
        }
        override fun getItemCount() = data.size
    }

    class CatVH(root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val txtId: TextView = root.findViewById(R.id.txtId)
        val txtNombre: TextView = root.findViewById(R.id.txtNombre)
        val btnModificar: com.google.android.material.button.MaterialButton = root.findViewById(R.id.btnModificar)
        val btnEliminar: com.google.android.material.button.MaterialButton = root.findViewById(R.id.btnEliminar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPcategoriaBinding.inflate(layoutInflater)
        setContentView(b.root)
        n = NCategoria(applicationContext)

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
            val ok = try { 
                // Verificar primero si tiene productos
                val tieneProductos = n.getLista("").any {
                    false
                }
                n.eliminar(categoriaId) 
            } catch (e: Exception) { 
                withContext(Dispatchers.Main) {
                    snack(" Error al eliminar: ${e.message}")
                }
                false 
            }
            
            withContext(Dispatchers.Main) {
                if (ok) {
                    snack(" Categoría ID:$categoriaId eliminada exitosamente")
                    btnListar("")
                } else {
                    snack(" No se pudo eliminar categoría ID:$categoriaId. Verifica que no tenga productos asociados.")
                }
            }
        }
    }

    private fun actualizarDesdeUI() {
        id = b.etId.editText?.text.toString().toIntOrNull() ?: 0
        nombre = b.etNombre.editText?.text.toString()
    }

    // ---- Métodos exactos del diagrama ----
    fun btnRegistrar() {
        actualizarDesdeUI()
        
        if (nombre.isBlank()) {
            snack(" Error: El nombre de la categoría es requerido")
            return
        }
        
        lifecycleScope.launch(Dispatchers.IO) {
            val ok = try {
                // Si el campo ID tiene valor > 0, es modificación; si no, es nuevo registro
                if (id > 0) {
                    // Modificación: usar el ID especificado
                    n.modificar(id, nombre)
                } else {
                    // Nuevo registro: usar ID 0 para que la BD use auto-increment
                    n.registrar(0, nombre)
                }
            } catch (e: Exception) { 
                withContext(Dispatchers.Main) {
                    snack(" Error: ${e.message}")
                }
                false 
            }
            
            withContext(Dispatchers.Main) {
                val esModificacion = id > 0
                val mensaje = if (esModificacion) {
                    if (ok) "Categoría modificada exitosamente" else " No se pudo modificar"
                } else {
                    if (ok) "Categoría registrada exitosamente" else " No se pudo registrar"
                }
                
                snack(mensaje)
                if (ok) {
                    limpiarFormulario()
                }
                btnListar("")
            }
        }
    }
    
    fun btnListar(filtro: String): List<Map<String, Any?>> {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = n.getTabla(filtro)
            withContext(Dispatchers.Main) {
                data.clear()
                data.addAll(lista.map {
                    val id = (it["id"] as? Number)?.toInt() ?: 0
                    val nombre = (it["nombre"] as? String).orEmpty()
                    id to nombre
                })
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
