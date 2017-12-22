package sic.modelo.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReciboDTO {
    
    private Long idRecibo = 0L;    
    private long nroRecibo = 1L;
    private boolean eliminado = false;
    private String observacion = "Recibo Test";
    private String nombreFormaDePago = "Efectivo";
    private String nombreEmpresa = "Globo De Oro";
    private String razonSocialCliente = "Construcciones S.A.";
    private String nombreUsuario = "test";
    private Date fecha = new Date();
    private double monto = 15000;
    private double saldoSobrante = 0;
    
}
