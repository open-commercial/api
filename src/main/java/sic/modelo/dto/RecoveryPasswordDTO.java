package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.Aplicacion;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoveryPasswordDTO {

    private String key;
    private long id;
    private Aplicacion aplicacion;

}
