package org.opencommercial.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.opencommercial.model.embeddable.ClienteEmbeddable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
public abstract class ComprobanteAutorizable {

    private long idComprobante;
    private LocalDateTime fecha;
    private Sucursal sucursal;
    private ClienteEmbeddable cliente;
    private BigDecimal subtotalBruto;
    private BigDecimal montoNoGravado;
    private BigDecimal total;
}
