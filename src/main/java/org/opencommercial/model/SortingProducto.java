package org.opencommercial.model;

import java.util.Optional;

public enum SortingProducto {
    DESCRIPCION("descripcion"),
    FECHA_ULTIMA_MODIFICACION("fechaUltimaModificacion"),
    CODIGO("codigo"),
    TOTAL_SUCURSALES("cantidadProducto.cantidadTotalEnSucursales"),
    VENTA_POR_CANTIDAD("cantidadProducto.cantMinima"),
    PRECIO_COSTO("precioProducto.precioCosto"),
    PORCENTAJE_GANANCIA("precioProducto.gananciaPorcentaje"),
    PRECIO_LISTA("precioProducto.precioLista"),
    FECHA_ALTA("fechaAlta"),
    PROVEEDOR("proveedor.razonSocial"),
    RUBRO("rubro.nombre"),
    ID_PRODUCTO("idProducto");

    private final String nombre;

    SortingProducto(String nombre) {
        this.nombre = nombre;
    }

    public static Optional<SortingProducto> fromValue(String value) {
        for (SortingProducto sortingProducto : SortingProducto.values()) {
            if (sortingProducto.nombre.equalsIgnoreCase(value)) {
                return Optional.of(sortingProducto);
            }
        }
        return Optional.empty();
    }

    public String getNombre() {
        return this.nombre;
    }
}
