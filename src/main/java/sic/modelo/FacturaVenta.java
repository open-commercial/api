package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.modelo.dto.ClienteDTO;
import sic.modelo.dto.UbicacionDTO;

@Entity
@Table(name = "facturaventa")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({"cliente", "usuario", "empresa", "pedido", "transportista"})
public class FacturaVenta extends Factura implements Serializable {

  @AttributeOverride(name = "id_Cliente", column = @Column(name = "idClienteEmbedded"))
  @Embedded
  private ClienteDTO clienteDTO;

  @ManyToOne
  @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
  @NotNull(message = "{mensaje_factura_cliente_vacio}")
  private Cliente cliente;

  public FacturaVenta() {}

  public FacturaVenta(
    ClienteDTO clienteDTO,
    Cliente cliente,
    Usuario usuario,
    long id_Factura,
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
    this.clienteDTO = clienteDTO;
    this.cliente = cliente;
  }

  @JsonGetter("idCliente")
  public Long getIdCliente() {
    return clienteDTO.getId_Cliente();
  }

  @JsonGetter("nombreFiscalCliente")
  public String getNombreFiscalCliente() {
    return clienteDTO.getNombreFiscal();
  }

  @JsonGetter("nroDeCliente")
  public String getNroDeCliente() {
    return clienteDTO.getNroCliente();
  }

  @JsonGetter("categoriaIVA")
  public CategoriaIVA getCategoriaIVA() {
    return clienteDTO.getCategoriaIVA();
  }

  @JsonGetter("idViajante")
  public Long getIdViajante() {
    return clienteDTO.getIdViajante();
  }

  @JsonGetter("nombreViajante")
  public String getNombreViajante() {
    return clienteDTO.getNombreViajante();
  }

  @JsonGetter("ubicacionFacturacion")
  public UbicacionDTO getUbicacionFacturacion() {
    return clienteDTO.getUbicacionFacturacion();
  }
}