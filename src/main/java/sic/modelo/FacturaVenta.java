package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.modelo.embeddable.ClienteEmbeddable;
import sic.controller.Views;

@Entity
@Table(name = "facturaventa")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({"cliente", "usuario", "empresa", "pedido", "transportista", "clienteEmbedded"})
@JsonView(Views.Comprador.class)
public class FacturaVenta extends Factura implements Serializable {

  @Embedded
  private ClienteEmbeddable clienteEmbedded;

  @ManyToOne
  @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
  @NotNull(message = "{mensaje_factura_cliente_vacio}")
  private Cliente cliente;

  public FacturaVenta() {}

  public FacturaVenta(
      long id_Factura,
      ClienteEmbeddable clienteEmbedded,
      Cliente cliente,
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
      BigDecimal cantidadDeArticulos,
      Empresa empresa,
      boolean eliminada,
      long CAE,
      Date vencimientoCAE,
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
      cantidadDeArticulos,
      empresa,
      eliminada,
      CAE,
      vencimientoCAE,
      numSerieAfip,
      numFacturaAfip);
    this.clienteEmbedded = clienteEmbedded;
    this.cliente = cliente;
  }

  @JsonGetter("idCliente")
  public Long getIdCliente() {
    return cliente.getId_Cliente();
  }

  @JsonGetter("nombreFiscalCliente")
  public String getNombreFiscalCliente() {
    return clienteEmbedded.getNombreFiscal();
  }

  @JsonGetter("nroDeCliente")
  public String getNroDeCliente() {
    return clienteEmbedded.getNroCliente();
  }

  @JsonGetter("categoriaIVA")
  public CategoriaIVA getCategoriaIVA() {
    return clienteEmbedded.getCategoriaIVA();
  }

  @JsonGetter("idViajante")
  public Long getIdViajante() {
    return (cliente.getViajante() != null) ? cliente.getViajante().getId_Usuario() : null;
  }

  @JsonGetter("nombreViajante")
  public String getNombreViajante() {
    return (cliente.getViajante() != null)
        ? cliente.getViajante().getNombre()
            + " "
            + cliente.getViajante().getApellido()
            + " ("
            + cliente.getViajante().getUsername()
            + ")"
        : null;
  }

  @JsonGetter("ubicacion")
  public String getUbicacionFacturacion() {
    return (clienteEmbedded.getUbicacion() != null) ? clienteEmbedded.getUbicacion().toString() : null;
  }
}
