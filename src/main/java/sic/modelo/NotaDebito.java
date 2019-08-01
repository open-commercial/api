package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "notadebito")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "renglonesNotaDebito")
public class NotaDebito extends Nota implements Serializable {

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "idNota")
  //@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(nullable = false)
  private List<RenglonNotaDebito> renglonesNotaDebito;

  @Column(precision = 25, scale = 15)
  private BigDecimal montoNoGravado;

  @ManyToOne
  @JoinColumn(name = "idRecibo", referencedColumnName = "idRecibo")
  private Recibo recibo;

  public NotaDebito() {}

  public NotaDebito(
      long idNota,
      long serie,
      long nroNota,
      boolean eliminada,
      TipoDeComprobante tipoDeComprobante,
      Date fecha,
      Empresa empresa,
      Usuario usuario,
      Cliente cliente,
      Movimiento movimiento,
      String motivo,
      List<RenglonNotaDebito> renglones,
      BigDecimal subTotalBruto,
      BigDecimal iva21Neto,
      BigDecimal iva105Neto,
      BigDecimal total,
      BigDecimal montoNoGravado,
      long CAE,
      Date vencimientoCAE,
      long numSerieAfip,
      long numNotaAfip,
      Recibo recibo) {

    super(
        idNota,
        serie,
        nroNota,
        eliminada,
        tipoDeComprobante,
        fecha,
        empresa,
        usuario,
        cliente,
        null,
        null,
        null,
        movimiento,
        motivo,
        subTotalBruto,
        iva21Neto,
        iva105Neto,
        total,
        CAE,
        vencimientoCAE,
        numSerieAfip,
        numNotaAfip);
    this.montoNoGravado = montoNoGravado;
    this.renglonesNotaDebito = renglones;
    this.recibo = recibo;
  }

  public NotaDebito(
      long idNota,
      long serie,
      long nroNota,
      boolean eliminada,
      TipoDeComprobante tipoDeComprobante,
      Date fecha,
      Empresa empresa,
      Usuario usuario,
      Proveedor proveedor,
      Movimiento movimiento,
      String motivo,
      List<RenglonNotaDebito> renglones,
      BigDecimal subTotalBruto,
      BigDecimal iva21Neto,
      BigDecimal iva105Neto,
      BigDecimal total,
      BigDecimal montoNoGravado,
      long CAE,
      Date vencimientoCAE,
      long numSerieAfip,
      long numNotaAfip,
      Recibo recibo) {

    super(
        idNota,
        serie,
        nroNota,
        eliminada,
        tipoDeComprobante,
        fecha,
        empresa,
        usuario,
        null,
        null,
        proveedor,
        null,
        movimiento,
        motivo,
        subTotalBruto,
        iva21Neto,
        iva105Neto,
        total,
        CAE,
        vencimientoCAE,
        numSerieAfip,
        numNotaAfip);
    this.montoNoGravado = montoNoGravado;
    this.renglonesNotaDebito = renglones;
    this.recibo = recibo;
  }

  @JsonGetter("idRecibo")
  public Long getIdRecibo() {
    return (recibo != null) ? recibo.getIdRecibo() : null;
  }

  @JsonGetter("numSerieRecibo")
  public Long getSerieRecibo() {
    return (recibo != null) ? recibo.getNumSerie() : null;
  }

  @JsonGetter("nroRecibo")
  public Long getNroRecibo() {
    return (recibo != null) ? recibo.getNumRecibo() : null;
  }
}
