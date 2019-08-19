package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "facturacompra")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({"proveedor", "usuario", "transportista", "sucursal", "pedido"})
public class FacturaCompra extends Factura implements Serializable {

  @ManyToOne
  @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
  @NotNull(message = "{mensaje_factura_proveedor_vacio}")
  private Proveedor proveedor;

  public FacturaCompra() {}

  public FacturaCompra(
      long id_Factura,
      Usuario usuario,
      Date fecha,
      TipoDeComprobante tipoComprobante,
      long numSerie,
      long numFactura,
      Date fechaVencimiento,
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
      long CAE,
      Date vencimientoCAE,
      Proveedor proveedor,
      long numSerieAfip,
      long numFacturaAfip) {

    super(
        id_Factura,
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
        CAE,
        vencimientoCAE,
        numSerieAfip,
        numFacturaAfip);
    this.proveedor = proveedor;
  }

  @JsonGetter("idProveedor")
  public Long getIdProveedor() {
    return proveedor.getId_Proveedor();
  }

  @JsonGetter("razonSocialProveedor")
  public String getRazonSocialProveedor() {
    return proveedor.getRazonSocial();
  }
}