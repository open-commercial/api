package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.config.Views;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "facturacompra")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({"proveedor", "usuario", "transportista", "pedido"})
@JsonView(Views.Comprador.class)
public class FacturaCompra extends Factura implements Serializable {

  @ManyToOne
  @JoinColumn(name = "id_Proveedor")
  @NotNull(message = "{mensaje_factura_proveedor_vacio}")
  private Proveedor proveedor;

  @NotNull(message = "{mensaje_factura_compra_fecha_alta_vacia}")
  private LocalDateTime fechaAlta;

  public FacturaCompra() {}

  public FacturaCompra(
      long idFactura,
      Usuario usuario,
      LocalDateTime fecha,
      LocalDateTime fechaAlta,
      TipoDeComprobante tipoComprobante,
      long numSerie,
      long numFactura,
      LocalDate fechaVencimiento,
      Pedido pedido,
      Transportista transportista,
      List<RenglonFactura> renglones,
      BigDecimal subTotal,
      BigDecimal recargoPorcentaje,
      BigDecimal recargoNeto,
      BigDecimal descuentoPorcentaje,
      BigDecimal descuentoNeto,
      BigDecimal subTotalNeto,
      BigDecimal iva105Neto,
      BigDecimal iva21Neto,
      BigDecimal impuestoInternoNeto,
      BigDecimal total,
      String observaciones,
      BigDecimal cantidadArticulos,
      Sucursal sucursal,
      boolean eliminada,
      long cae,
      LocalDate vencimientoCAE,
      Proveedor proveedor,
      long numSerieAfip,
      long numFacturaAfip) {

    super(
        idFactura,
        usuario,
        fecha,
        tipoComprobante,
        numSerie,
        numFactura,
        fechaVencimiento,
        pedido,
        transportista,
        renglones,
        subTotal,
        recargoPorcentaje,
        recargoNeto,
        descuentoPorcentaje,
        descuentoNeto,
        subTotalNeto,
        iva105Neto,
        iva21Neto,
        impuestoInternoNeto,
        total,
        observaciones,
        cantidadArticulos,
      sucursal,
        eliminada,
        cae,
        vencimientoCAE,
        numSerieAfip,
        numFacturaAfip);
    this.proveedor = proveedor;
    this.fechaAlta = fechaAlta;
  }

  @JsonGetter("idProveedor")
  public Long getIdProveedor() {
    return proveedor.getIdProveedor();
  }

  @JsonGetter("razonSocialProveedor")
  public String getRazonSocialProveedor() {
    return proveedor.getRazonSocial();
  }
}
