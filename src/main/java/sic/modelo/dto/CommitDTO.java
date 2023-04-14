package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Encargado.class)
public class CommitDTO {

    String idCommit;
    String nombreDeClase;
    LocalDateTime fecha;
    String usuario;
    String tipoDeOperacion;
    List<CambioDTO> cambios;
    String idCommitRelacionado;

}
