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
public class NuevoReciboDepositoDTO {

    private long idPedido;
    private String concepto;
    private byte[] imagen;
    private BigDecimal monto;
}
