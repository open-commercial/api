package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.modelo.embeddable.ClienteEmbeddable;
import sic.config.Views;

@Entity
@Table(name = "facturaventa")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({
  "cliente",
  "usuario",
  "sucursal",
  "pedido",
  "transportista",
  "clienteEmbedded"
})
@JsonView(Views.Comprador.class)
public class FacturaVenta extends Factura implements Serializable {

  @Embedded private ClienteEmbeddable clienteEmbedded;

  @ManyToOne
  @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
  @NotNull(message = "{mensaje_factura_cliente_vacio}")
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "idRemito", referencedColumnName = "idRemito")
  private Remito remito;

  public FacturaVenta() {}

  public FacturaVenta(
      long idFactura,
      ClienteEmbeddable clienteEmbedded,
      Cliente cliente,
      Usuario usuario,
      LocalDateTime fecha,
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
      BigDecimal cantidadDeArticulos,
      Sucursal sucursal,
      boolean eliminada,
      long cae,
      LocalDate vencimientoCAE,
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
        cantidadDeArticulos,
        sucursal,
        eliminada,
        cae,
        vencimientoCAE,
        numSerieAfip,
        numFacturaAfip);
    this.clienteEmbedded = clienteEmbedded;
    this.cliente = cliente;
  }

  @JsonGetter("idCliente")
  public Long getIdCliente() {
    return cliente.getIdCliente();
  }

  @JsonGetter("nombreFiscalCliente")
  public String getNombreFiscalCliente() {
    return clienteEmbedded.getNombreFiscalCliente();
  }

  @JsonGetter("nroDeCliente")
  public String getNroDeCliente() {
    return clienteEmbedded.getNroCliente();
  }

  @JsonGetter("categoriaIVACliente")
  public CategoriaIVA getCategoriaIVA() {
    return clienteEmbedded.getCategoriaIVACliente();
  }

  @JsonGetter("idViajanteCliente")
  public Long getIdViajante() {
    return (cliente.getViajante() != null) ? cliente.getViajante().getIdUsuario() : null;
  }

  @JsonGetter("nombreViajanteCliente")
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

  @JsonGetter("ubicacionCliente")
  public String getUbicacionCliente() {
    return (clienteEmbedded.getCalleUbicacionCliente() != null
            ? clienteEmbedded.getCalleUbicacionCliente() + " "
            : "")
        + (clienteEmbedded.getNumeroUbicacionCliente() != null
            ? clienteEmbedded.getNumeroUbicacionCliente() + " "
            : "")
        + (clienteEmbedded.getPisoUbicacionCliente() != null
            ? clienteEmbedded.getPisoUbicacionCliente() + " "
            : "")
        + (clienteEmbedded.getDepartamentoUbicacionCliente() != null
            ? clienteEmbedded.getDepartamentoUbicacionCliente() + " "
            : "")
        + ((clienteEmbedded.getDescripcionUbicacionCliente() != null
                && !clienteEmbedded.getDescripcionUbicacionCliente().isEmpty())
            ? "(" + clienteEmbedded.getDescripcionUbicacionCliente() + ")" + " "
            : "")
        + (clienteEmbedded.getNombreLocalidadCliente() != null
            ? clienteEmbedded.getNombreLocalidadCliente() + " "
            : "")
        + (clienteEmbedded.getNombreProvinciaCliente() != null
            ? clienteEmbedded.getNombreProvinciaCliente()
            : "");
  }
}
