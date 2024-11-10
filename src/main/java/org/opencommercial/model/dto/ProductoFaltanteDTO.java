package org.opencommercial.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.config.Views;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Comprador.class)
public class ProductoFaltanteDTO {

    private long idProducto;
    private String codigo;
    private String descripcion;
    private long idSucursal;
    private String nombreSucursal;
    private BigDecimal cantidadSolicitada;
    private BigDecimal cantidadDisponible;
}
