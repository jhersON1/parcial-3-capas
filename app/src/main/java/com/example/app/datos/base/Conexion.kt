package com.example.app.datos.base

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Singleton de conexión SQLite para TODA la app.
 * Se usa exactamente como atributo 'bd: Conexion' en las clases D.
 */
class Conexion private constructor(private val appCtx: Context) :
    SQLiteOpenHelper(appCtx, "tienda.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.execSQL("PRAGMA foreign_keys=ON")
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tablas base: categoría y producto
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categoria(
              id     INTEGER PRIMARY KEY AUTOINCREMENT,
              nombre TEXT NOT NULL UNIQUE
            );
            """.trimIndent()
        )
        
        // Insertar categoría por defecto
        db.execSQL("INSERT OR IGNORE INTO categoria (id, nombre) VALUES (1, 'General')")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS producto(
              id           INTEGER PRIMARY KEY,
              nombre       TEXT    NOT NULL,
              descripcion  TEXT    NOT NULL,
              precio       REAL    NOT NULL,
              stock        INTEGER NOT NULL,
              categoria_id INTEGER NOT NULL,
              FOREIGN KEY(categoria_id) REFERENCES categoria(id) ON DELETE RESTRICT
            );
            """.trimIndent()
        )

        // Ventas
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS venta(
              id         INTEGER PRIMARY KEY AUTOINCREMENT,
              fechaHora  TEXT    NOT NULL,
              total      REAL    NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS detalle_venta(
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              id_venta        INTEGER NOT NULL,
              id_producto     INTEGER NOT NULL,
              cantidad        INTEGER NOT NULL,
              precio_unitario REAL    NOT NULL,
              FOREIGN KEY(id_venta)    REFERENCES venta(id)    ON DELETE CASCADE,
              FOREIGN KEY(id_producto) REFERENCES producto(id) ON DELETE RESTRICT
            );
            """.trimIndent()
        )

        // Compras
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS compra(
              id         INTEGER PRIMARY KEY AUTOINCREMENT,
              fechaHora  TEXT    NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS detalle_compra(
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              id_compra       INTEGER NOT NULL,
              id_producto     INTEGER NOT NULL,
              cantidad        INTEGER NOT NULL,
              costo_unitario  REAL    NOT NULL,
              FOREIGN KEY(id_compra)   REFERENCES compra(id)   ON DELETE CASCADE,
              FOREIGN KEY(id_producto) REFERENCES producto(id) ON DELETE RESTRICT
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS detalle_compra")
        db.execSQL("DROP TABLE IF EXISTS compra")
        db.execSQL("DROP TABLE IF EXISTS detalle_venta")
        db.execSQL("DROP TABLE IF EXISTS venta")
        db.execSQL("DROP TABLE IF EXISTS producto")
        db.execSQL("DROP TABLE IF EXISTS categoria")
        onCreate(db)
    }

    companion object {
        @Volatile
        private var INSTANCIA: Conexion? = null

        /** Obtiene la única instancia usando ApplicationContext (no Activity) */
        fun instancia(ctx: Context): Conexion {
            if (INSTANCIA == null) {
                synchronized(this) {
                    if (INSTANCIA == null) {
                        INSTANCIA = Conexion(ctx.applicationContext)
                    }
                }
            }
            return INSTANCIA!!
        }
    }
}

