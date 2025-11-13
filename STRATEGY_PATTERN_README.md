# Patrón Strategy - Sistema de Descuentos en Ventas

## 📋 Descripción

Este proyecto implementa el **Patrón de Diseño Strategy** en la capa de negocio para aplicar diferentes tipos de descuentos a las ventas de manera flexible y extensible.

## 🏗️ Arquitectura

El patrón se implementó en la **Capa de Negocio** (`negocio/venta/estrategias/`) siguiendo los principios SOLID:

```
negocio/
  └── venta/
      ├── NVenta.kt                    (Modificado - usa PricingContext)
      └── estrategias/                 (NUEVO)
          ├── Venta.kt                 (Modelo de datos)
          ├── PricingStrategy.kt       (Interfaz Strategy)
          ├── PricingContext.kt        (Contexto)
          ├── SinDescuento.kt          (Estrategia concreta)
          ├── DescuentoVIP.kt          (Estrategia concreta)
          ├── DescuentoNavideno.kt     (Estrategia concreta)
          └── DescuentoPorMonto.kt     (Estrategia concreta)
```

## 🎯 Componentes del Patrón

### 1. **Interfaz PricingStrategy**
Define el contrato que todas las estrategias deben cumplir:
```kotlin
interface PricingStrategy {
    fun calcular(venta: Venta): Double
}
```

### 2. **Contexto (PricingContext)**
Mantiene una referencia a la estrategia actual y delega el cálculo:
```kotlin
class PricingContext(private var strategy: PricingStrategy) {
    fun setStrategy(strategy: PricingStrategy)
    fun calcularTotal(venta: Venta): Double
}
```

### 3. **Estrategias Concretas**

#### a) **SinDescuento**
- No aplica ningún descuento
- Retorna el precio total normal

#### b) **DescuentoVIP**
- Aplica un porcentaje fijo de descuento
- Parámetro: `porcentaje` (ej: 15.0 para 15%)
- Ejemplo: Cliente VIP obtiene 15% de descuento

#### c) **DescuentoNavideno**
- Aplica descuento solo en un rango de fechas
- Parámetros: `porcentaje`, `inicio`, `fin`
- Ejemplo: 20% de descuento del 1 al 31 de diciembre

#### d) **DescuentoPorMonto**
- Aplica descuento si la compra supera un umbral
- Parámetros: `umbral`, `porcentaje`
- Ejemplo: 10% de descuento si la compra es >= $500

## 🚀 Uso

### Desde la Capa de Negocio (NVenta)

```kotlin
val nVenta = NVenta(context, nProducto)

// Cambiar estrategia
nVenta.seleccionarEstrategia(DescuentoVIP(15.0))

// Obtener estrategia actual
val estrategiaActual = nVenta.obtenerEstrategia()

// Registrar venta (automáticamente aplica la estrategia)
nVenta.registrar(fechaHora, items)
```

### Desde la Capa de Presentación (PVenta)

El usuario puede seleccionar la estrategia mediante un Spinner:
- **Sin Descuento**: Precio normal
- **Descuento VIP (15%)**: 15% de descuento
- **Descuento Navideño (20%)**: 20% en diciembre
- **Descuento por Monto**: 10% si compra >= $500

La estrategia se cambia dinámicamente al seleccionar una opción.

## ✅ Ventajas de esta Implementación

1. **Open/Closed Principle**: Abierto para extensión, cerrado para modificación
2. **Single Responsibility**: Cada estrategia tiene una sola responsabilidad
3. **Fácil de extender**: Agregar nuevos descuentos sin modificar código existente
4. **Testeable**: Cada estrategia se puede probar de forma aislada
5. **Reutilizable**: Las estrategias se pueden usar en diferentes contextos
6. **Separación de responsabilidades**: Lógica de negocio separada de la presentación

## 🔧 Cómo Agregar una Nueva Estrategia

1. Crear una nueva clase que implemente `PricingStrategy`:

```kotlin
class DescuentoBlackFriday(val porcentaje: Double) : PricingStrategy {
    override fun calcular(venta: Venta): Double {
        // Lógica de descuento
        val subtotal = venta.items.sumOf { it.cantidad * it.precioUnitario }
        return subtotal * (1 - porcentaje / 100)
    }
}
```

2. Agregar la opción en `strings.xml`:
```xml
<string-array name="estrategias_descuento">
    ...
    <item>Black Friday (50%)</item>
</string-array>
```

3. Actualizar el método `cambiarEstrategia()` en PVenta:
```kotlin
4 -> DescuentoBlackFriday(50.0)
```

## 📊 Flujo de Datos

```
Usuario selecciona estrategia en Spinner
    ↓
PVenta.cambiarEstrategia(position)
    ↓
NVenta.seleccionarEstrategia(strategy)
    ↓
PricingContext.setStrategy(strategy)
    ↓
Usuario agrega items y confirma venta
    ↓
NVenta.registrar() usa PricingContext.calcularTotal()
    ↓
Strategy.calcular() aplica el descuento
    ↓
Se guarda en BD el total con descuento
```

## 📝 Notas Importantes

- El descuento se calcula al momento de **registrar la venta**
- El total mostrado en pantalla se actualiza al cambiar la estrategia
- El descuento se aplica sobre el **subtotal** de todos los items
- Las validaciones de items se mantienen independientes del descuento

## 🎓 Patrón Strategy en Acción

Este es un ejemplo clásico del patrón Strategy donde:
- **Strategy** = PricingStrategy (interfaz)
- **ConcreteStrategy** = SinDescuento, DescuentoVIP, etc. (implementaciones)
- **Context** = PricingContext (mantiene la estrategia actual)
- **Client** = NVenta (usa el contexto para delegar el cálculo)

---

**Desarrollado como parte del Parcial de Arquitectura de Software - Arquitectura de 3 Capas**

