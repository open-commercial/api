package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;
import sic.modelo.TipoDeOperacion;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Encargado.class)
public class ChangeDTO {

    LocalDateTime date;
    String usuario;
    String tipoDeOperacion;
    HashMap<String, List<ValueChangeDTO>> changes;

}
