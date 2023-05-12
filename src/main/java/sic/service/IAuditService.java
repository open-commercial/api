package sic.service;

import sic.modelo.dto.CommitDTO;
import java.util.List;
import java.util.Map;

public interface IAuditService {

    String auditar(String idUsuario, Object objeto, Map<String, String> propiedades);

    String auditar(String idUsuario, Object objeto);

    <T> List<CommitDTO> getCambiosDTO(T objeto);

    List<CommitDTO> getCambiosDTO(String idCommit);

}
