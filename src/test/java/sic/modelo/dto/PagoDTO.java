package sic.modelo.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.Nota;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoDTO {
    
    private Long id_Pago = 0L;    
    private long nroPago = 0;
    private String nombreFormaDePago = "efectivo";
    private Nota notaDebito = null;
    private double monto = 100;
    private Date fecha = new Date();
    private String nota = "pago dto";
    private String nombreEmpresa = "Globo De Oro";
    private boolean eliminado = false;
    
}
