package sic.service;

import org.javers.core.Changes;
import org.javers.core.commit.Commit;
import org.javers.core.diff.Change;
import org.javers.repository.jql.JqlQuery;
import sic.modelo.dto.CambioDTO;
import sic.modelo.dto.CommitDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IAuditService {

    Commit auditar(String string, Object objeto, Map<String, String> propiedades);

    Commit auditar(String string, Object objeto);
    List<CommitDTO> getCambiosDTO(Changes changes);

    List<CommitDTO> getCambiosDTO(Changes changes, String idCommitRelacionado);

    List<CambioDTO> getValoresCambiadosDTO(List<Change> changes);

    Changes getCambios(JqlQuery jqlQuery);
}
