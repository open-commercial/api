package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.Nota;
import sic.modelo.TipoDeComprobante;

@Data
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "serie", "nroNota", "empresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idNota", scope = Nota.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FacturaCompraDTO.class, name = "NotaCredito"),
        @JsonSubTypes.Type(value = FacturaVentaDTO.class, name = "NotaDebito"),
})
public abstract class NotaDTO implements Serializable {

  //    private Long idNota = 0L;
  //    private long serie = 0;
  //    private long nroNota = 1;
  //    private boolean eliminada = false;
  //    private TipoDeComprobante tipoComprobante;
  //    private Date fecha;
  //    private EmpresaDTO empresa = EmpresaDTO.builder().build();
  //    private UsuarioDTO usuario = UsuarioDTO.builder().build();
  //    private ClienteDTO cliente;
  //    private FacturaVentaDTO facturaVenta;
  //    private ProveedorDTO proveedor;
  //    private FacturaCompraDTO facturaCompra;
  //    private String motivo = "Nota por default";
  //    private BigDecimal subTotalBruto = new BigDecimal("6500");
  //    private BigDecimal iva21Neto = new BigDecimal("1365");
  //    private BigDecimal iva105Neto = BigDecimal.ZERO;
  //    private BigDecimal total = new BigDecimal("7865");
  //    private long CAE = 0L;
  //    private Date vencimientoCAE = new Date();
  //    private long numSerieAfip = 0L;
  //    private long numNotaAfip= 0L;
  //

  private Long idNota;
  private long serie;
  private long nroNota;
  private boolean eliminada;
  private TipoDeComprobante tipoComprobante;
  private Date fecha;
  private EmpresaDTO empresa;
  private UsuarioDTO usuario;
  private ClienteDTO cliente;
  //private FacturaVentaDTO facturaVenta;
  private ProveedorDTO proveedor;
  private FacturaCompraDTO facturaCompra;
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
