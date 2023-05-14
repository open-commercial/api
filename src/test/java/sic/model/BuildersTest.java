package sic.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.dto.CambioDTO;
import sic.modelo.dto.CommitDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ExtendWith(SpringExtension.class)
class BuildersTest {

    @Test
    void shouldTestCommitDTOBuilder() {
        String idCommit = "1.00";
        String idCommitRelacionado = "2.00";
        LocalDateTime fecha = LocalDateTime.now();
        String usuario = "Usuario Test(test)";
        String tipoDeOperacion = "ACTUALIZACION";
        List<CambioDTO> cambios = new ArrayList<>();
        CommitDTO commitDTO = CommitDTO.builder()
                .idCommit(idCommit)
                .idCommitRelacionado(idCommitRelacionado)
                .fecha(fecha)
                .usuario(usuario)
                .tipoDeOperacion(tipoDeOperacion)
                .cambios(cambios)
                .build();
        Assertions.assertNotNull(commitDTO);
        Assertions.assertEquals(idCommit, commitDTO.getIdCommit());
        Assertions.assertEquals(idCommitRelacionado, commitDTO.getIdCommitRelacionado());
        Assertions.assertEquals(fecha, commitDTO.getFecha());
        Assertions.assertEquals(usuario, commitDTO.getUsuario());
        Assertions.assertEquals(tipoDeOperacion, commitDTO.getTipoDeOperacion());
        Assertions.assertEquals(cambios, commitDTO.getCambios());
    }

    @Test
    void shouldTestCambioDTOBuilder() {
        String atributo = "total";
        String valorAnterior = "100";
        String valorSiguiente = "20000";
        CambioDTO cambioDTO = new CambioDTO(atributo, valorAnterior, valorSiguiente);
        Assertions.assertNotNull(cambioDTO);
        Assertions.assertEquals(atributo, cambioDTO.getAtributo());
        Assertions.assertEquals(valorAnterior, cambioDTO.getValorAnterior());
        Assertions.assertEquals(valorSiguiente, cambioDTO.getValorSiguiente());
    }
}
