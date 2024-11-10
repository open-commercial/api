package org.opencommercial.model.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.opencommercial.model.Nota;
import org.opencommercial.model.TipoDeComprobante;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"idNota", "serie", "nroNota"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idNota", scope = Nota.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NotaCreditoDTO.class, name = "NotaCredito"),
        @JsonSubTypes.Type(value = NotaDebitoDTO.class, name = "NotaDebito"),
})
public abstract class NotaDTO implements Serializable {

  private Long idNota;
  private long serie;
  private long nroNota;
  private boolean eliminada;
  private TipoDeComprobante tipoComprobante;
  private LocalDateTime fecha;
  private long idSucursal;
  private String nombreSucursal;
  private Long idCliente;
  private String nombreFiscalCliente;
  private long idViajante;
  private String nombreViajante;
  private Long idFacturaVenta;
  private Long idProveedor;
  private String razonSocialProveedor;
  private Long idFacturaCompra;
  private long idUsuario;
  private String nombreUsuario;
  private String motivo;
  private BigDecimal subTotalBruto;
  private BigDecimal iva21Neto;
  private BigDecimal iva105Neto;
  private BigDecimal total;
  private long cae;
  private LocalDateTime vencimientoCae;
  private long numSerieAfip;
  private long numNotaAfip;

}
