package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sic.modelo.Nota;
import sic.modelo.TipoDeComprobante;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "serie", "nroNota"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idNota", scope = Nota.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
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
  private Date fecha;
  private long idEmpresa;
  private String nombreEmpresa;
  private UsuarioDTO usuario;
  private long idCliente;
  private String razonSocialCliente;
  private long idFacturaVenta;
  private long idProveedor;
  private String razonSocialProveedor;
  private long idFacturaCompra;
  private String motivo;
  private BigDecimal subTotalBruto;
  private BigDecimal iva21Neto;
  private BigDecimal iva105Neto;
  private BigDecimal total;
  private long CAE;
  private Date vencimientoCAE;
  private long numSerieAfip;
  private long numNotaAfip;

}
