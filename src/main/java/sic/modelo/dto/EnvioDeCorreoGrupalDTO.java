package sic.modelo.dto;

import lombok.*;
import sic.modelo.Rol;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class EnvioDeCorreoGrupalDTO {

    private LocalDateTime fecha;

    private List<Rol> roles;

    private String asunto;

    private String mensaje;

    private byte[] reporteAdjunto;
}
