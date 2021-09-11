package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleCompraDTO {

    private LocalDateTime fecha;
    private Long serie;
    private Long nroNota;
    private Long CAE;
}
