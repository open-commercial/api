package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeDTO {

    long idUSuario;
    String nombreUsuario;
    Instant fecha;
    long revisionNumber;
}
