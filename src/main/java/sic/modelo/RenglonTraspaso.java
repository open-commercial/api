package sic.modelo;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
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

    @NotNull(message = "{mensaje_producto_vacio_descripcion}")
    @NotEmpty(message = "{mensaje_producto_vacio_descripcion}")
    @JsonView(Views.Comprador.class)
    private String descripcionProducto;

    @NotEmpty
    private String nombreMedidaProducto;

    @Positive
    private BigDecimal cantidadProducto;
}
