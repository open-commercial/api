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

    private String idCommit;
    private String idCommitRelacionado;
    private LocalDateTime fecha;
    private String usuario;
    private String tipoDeOperacion;
    private List<CambioDTO> cambios;

}
