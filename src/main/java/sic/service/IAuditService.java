package sic.service;

import sic.modelo.dto.CommitDTO;
import java.util.List;
import java.util.Map;

public interface IAuditService {

    String auditar(String idUsuario, Object objeto, Map<String, String> propiedades);

    <T> List<CommitDTO> getCambios(T objeto);

    List<CommitDTO> getCambios(String idCommit);

}
