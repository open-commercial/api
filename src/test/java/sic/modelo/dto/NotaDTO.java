package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.builder.ClienteBuilder;
import sic.builder.EmpresaBuilder;
import sic.builder.FacturaVentaBuilder;
import sic.builder.UsuarioBuilder;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Usuario;

@Data
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "serie", "nroNota", "empresa", "cliente"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idNota", scope = Nota.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class NotaDTO implements Serializable {
    
    
    private Long idNota = 0L;
    private long serie = 0;
    private long nroNota = 1;    
    private boolean eliminada = false;
    private TipoDeComprobante tipoComprobante;
    private Date fecha;
    private Empresa empresa = new EmpresaBuilder().build();
    private Cliente cliente = new ClienteBuilder().build();
    private Usuario usuario = new UsuarioBuilder().build();
    private FacturaVenta facturaVenta = new FacturaVentaBuilder().build();
    private List<Pago> pagos;
    private String motivo = "Nota por default";
    private BigDecimal subTotalBruto = new BigDecimal("6500"); 
    private BigDecimal iva21Neto = new BigDecimal("1365");     
    private BigDecimal iva105Neto = BigDecimal.ZERO;
    private BigDecimal total = new BigDecimal("7865");    
    private long CAE = 1l;
    private Date vencimientoCAE = new Date();   
    private long numSerieAfip = 1l;
    private long numFacturaAfip= 000000011l;   
    
}
