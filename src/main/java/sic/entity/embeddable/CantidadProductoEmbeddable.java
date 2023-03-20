package sic.entity.embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;
import sic.entity.CantidadEnSucursal;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
@JsonView(Views.Vendedor.class)
public class CantidadProductoEmbeddable implements Serializable  {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idProducto")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @JsonView(Views.Viajante.class)
    @NotEmpty(message = "{mensaje_producto_cantidad_en_sucursales_vacia}")
    private Set<CantidadEnSucursal> cantidadEnSucursales;

    @JsonView(Views.Comprador.class)
    @Transient
    private Set<CantidadEnSucursal> cantidadEnSucursalesDisponible;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
    @JsonView(Views.Comprador.class)
    @NotNull(message = "{mensaje_producto_cantidad_cantidad_total_sucursales_invalida}")
    private BigDecimal cantidadTotalEnSucursales;

    @JsonView(Views.Comprador.class)
    @Transient
    private BigDecimal cantidadTotalEnSucursalesDisponible;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
    @NotNull(message = "{mensaje_producto_cantidad_reservada_invalida}")
    @JsonView(Views.Vendedor.class)
    private BigDecimal cantidadReservada;

    @Column(precision = 25, scale = 15)
    @DecimalMin(value = "1", message = "{mensaje_producto_cantidad_venta_minima_invalida}")
    @NotNull(message = "{mensaje_producto_cantidad_venta_minima_invalida}")
    @JsonView(Views.Comprador.class)
    private BigDecimal cantMinima;

    private boolean ilimitado;
}
