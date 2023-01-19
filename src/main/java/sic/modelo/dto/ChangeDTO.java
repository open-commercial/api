package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeDTO {

    LocalDateTime date;
    UsuarioDTO usuarioDTO;
    List<ValueChangeDTO> changes;

}
