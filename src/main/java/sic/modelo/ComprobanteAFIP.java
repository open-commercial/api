package sic.modelo;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComprobanteAFIP {
    
    private Date fecha;
    private TipoDeComprobante tipoComprobante;
    private long CAE;
    private Date vencimientoCAE;
    private long numSerieAfip;
    private long numFacturaAfip;
    private Empresa empresa;    
    private Cliente cliente;    
    private double subtotalBruto;    
    private double iva105neto;    
    private double iva21neto;    
    private double montoNoGravado;
    private double total;
}
