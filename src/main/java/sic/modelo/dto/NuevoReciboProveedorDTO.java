package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoReciboProveedorDTO {

    private long idProveedor;
    private long idSucursal;
    private long idFormaDePago;
    private String concepto;
    private BigDecimal monto;
}
