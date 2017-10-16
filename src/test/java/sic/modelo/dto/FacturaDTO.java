package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.builder.EmpresaBuilder;
import sic.builder.TransportistaBuilder;
import sic.modelo.Empresa;
import sic.modelo.Pago;
import sic.modelo.Pedido;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Transportista;

@Data
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "numSerie", "numFactura", "empresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Factura", scope = FacturaDTO.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
//   @JsonSubTypes.Type(value = FacturaCompra.class),
   @JsonSubTypes.Type(value = FacturaVentaDTO.class, name = "FacturaVenta"),    
})
public abstract class FacturaDTO implements Serializable {
    
    private long id_Factura = 0L;
    private Date fecha = new Date();
    private TipoDeComprobante tipoComprobante = TipoDeComprobante.FACTURA_A;
    private long numSerie = 0;
    private long numFactura = 1;
    private Date fechaVencimiento = new Date();    
    private Pedido pedido =  null;
    private Transportista transportista = new TransportistaBuilder().build();
    private List<RenglonFactura> renglones;
    private List<Pago> pagos;
    private double subTotal = 6500;
    private double recargo_porcentaje = 0.0;
    private double recargo_neto = 0.0;
    private double descuento_porcentaje = 0.0;
    private double descuento_neto = 0.0;
    private double subTotal_bruto = 6500;
    private double iva_105_neto = 0.0;
    private double iva_21_neto = 1365;
    private double impuestoInterno_neto = 0.0;
    private double total = 7865;
    private String observaciones = "Factura por Default";
    private boolean pagada = false;
    private Empresa empresa = new EmpresaBuilder().build();
    private boolean eliminada = false;
}
