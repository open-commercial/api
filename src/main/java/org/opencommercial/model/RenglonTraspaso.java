package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.config.Views;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Entity
@Table(name = "renglontraspaso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonView(Views.Vendedor.class)
public class RenglonTraspaso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRenglonTraspaso;

    @NotNull
    private Long idProducto;

    private String codigoProducto;

    @NotNull(message = "{mensaje_producto_vacio_descripcion}")
    @NotEmpty(message = "{mensaje_producto_vacio_descripcion}")
    @JsonView(Views.Comprador.class)
    private String descripcionProducto;

    @NotEmpty
    private String nombreMedidaProducto;

    @Positive
    private BigDecimal cantidadProducto;
}
