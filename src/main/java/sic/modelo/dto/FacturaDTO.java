package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;

@Data
@EqualsAndHashCode(of = {"tipoComprobante", "numSerie", "numFactura", "nombreEmpresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Factura", scope = FacturaDTO.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
   @JsonSubTypes.Type(value = FacturaCompraDTO.class, name = "FacturaCompra"),
   @JsonSubTypes.Type(value = FacturaVentaDTO.class, name = "FacturaVenta"),    
})
public abstract class FacturaDTO implements Serializable {
    
    private long id_Factura = 0L;
    private Date fecha;
    private TipoDeComprobante tipoComprobante = TipoDeComprobante.FACTURA_A;
    private long numSerie = 0;
    private long numFactura = 1;
    private Date fechaVencimiento;    
    private String nombreTransportista = "Correo OCA";
    private List<RenglonFactura> renglones;
    private BigDecimal subTotal = new BigDecimal("6500");
    private BigDecimal recargoPorcentaje = BigDecimal.ZERO;
    private BigDecimal recargoNeto = BigDecimal.ZERO;
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;
    private BigDecimal descuentoNeto = BigDecimal.ZERO;
    private BigDecimal subTotalBruto = new BigDecimal("6500");
    private BigDecimal iva105Neto = BigDecimal.ZERO;
    private BigDecimal iva21Neto = new BigDecimal("1365");
    private BigDecimal impuestoInternoNeto = BigDecimal.ZERO;
    private BigDecimal total = new BigDecimal("7865");
    private String observaciones = "Factura por Default";
    private String nombreEmpresa = "Globo Corporation";
    private boolean eliminada = false;
}
