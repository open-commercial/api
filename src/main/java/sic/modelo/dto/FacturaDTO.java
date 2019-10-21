package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id_Factura", "fecha", "numSerie", "numFactura", "nombreTransportista", "renglones", "nombreEmpresa", "idUsuario", "nombreUsuario", "cantidadArticulos"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Factura", scope = FacturaDTO.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
   @JsonSubTypes.Type(value = FacturaCompraDTO.class, name = "FacturaCompra"),
   @JsonSubTypes.Type(value = FacturaVentaDTO.class, name = "FacturaVenta"),    
})
public abstract class FacturaDTO implements Serializable {

    private long id_Factura;
    private LocalDateTime fecha;
    private TipoDeComprobante tipoComprobante;
    private long numSerie;
    private long numFactura;
    private LocalDateTime fechaVencimiento;
    private Long nroPedido;
    private long idTransportista;
    private String nombreTransportista;
    private List<RenglonFactura> renglones;
    private BigDecimal subTotal;
    private BigDecimal recargoPorcentaje;
    private BigDecimal recargoNeto;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoNeto;
    private BigDecimal subTotalBruto;
    private BigDecimal iva105Neto;
    private BigDecimal iva21Neto;
    private BigDecimal impuestoInternoNeto;
    private BigDecimal total;
    private String observaciones;
    private BigDecimal cantidadArticulos;
    private long idEmpresa;
    private String nombreEmpresa;
    private Long idUsuario;
    private String nombreUsuario;
    private boolean eliminada;
    private long cae;
    private LocalDateTime vencimientoCae;
}
