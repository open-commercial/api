package org.opencommercial.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaFacturaCompraDTO {

    private Long idSucursal;
    private Long idProveedor;
    private Long idTransportista;
    private Long numSerie;
    private Long numFactura;
    private LocalDateTime fecha;
    private LocalDate fechaVencimiento;
    private TipoDeComprobante tipoDeComprobante;
    private String observaciones;
    private List<NuevoRenglonFacturaDTO> renglones;
    private BigDecimal recargoPorcentaje;
    private BigDecimal descuentoPorcentaje;
}
