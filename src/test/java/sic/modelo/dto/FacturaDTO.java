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
import sic.modelo.Pago;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;

@Data
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "numSerie", "numFactura", "nombreEmpresa"})
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
    private List<Pago> pagos;
    private BigDecimal subTotal = new BigDecimal(6500);
    private BigDecimal recargo_porcentaje = BigDecimal.ZERO;
    private BigDecimal recargo_neto = BigDecimal.ZERO;
    private BigDecimal descuento_porcentaje = BigDecimal.ZERO;
    private BigDecimal descuento_neto = BigDecimal.ZERO;
    private BigDecimal subTotal_bruto = new BigDecimal(6500);
    private BigDecimal iva_105_neto = BigDecimal.ZERO;
    private BigDecimal iva_21_neto = new BigDecimal(1365);
    private BigDecimal impuestoInterno_neto = BigDecimal.ZERO;
    private BigDecimal total = new BigDecimal(7865);
    private String observaciones = "Factura por Default";
    private boolean pagada = false;
    private String nombreEmpresa = "Globo Corporation";
    private boolean eliminada = false;
}
