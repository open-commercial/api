package sic.service.impl;

import com.google.gson.internal.LinkedTreeMap;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.commit.Commit;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.InitialValueChange;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.CollectionChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.repository.jql.JqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.modelo.dto.CambioDTO;
import sic.modelo.dto.CommitDTO;
import sic.service.IAuditService;
import sic.service.IUsuarioService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuditServiceImpl implements IAuditService {

    private IUsuarioService usuarioService;

    private Javers javers;

    @Autowired
    public AuditServiceImpl(IUsuarioService usuarioService, Javers javers) {
        this.usuarioService = usuarioService;
        this.javers = javers;
    }

    @Override
    public Commit auditar(String string, Object objeto, Map<String, String> propiedades) {
        return javers.commit(string, objeto, propiedades);
    }

    @Override
    public Commit auditar(String string, Object objeto) {
        return javers.commit(string, objeto);
    }

    @Override
    public List<CommitDTO> getCambiosDTO(Changes changes) {
        var changesDTO = new ArrayList<CommitDTO>();
        final var nombreDeClase = changes.get(0).getAffectedGlobalId().getTypeName()
                .substring(changes.get(0).getAffectedGlobalId().getTypeName().lastIndexOf(".") + 1);
        changes.groupByCommit().forEach(changeByCommit -> {
            Usuario usuarioAuthor = usuarioService.getUsuarioNoEliminadoPorId(Long.parseLong(changeByCommit.getCommit().getAuthor()));
            changesDTO.add(CommitDTO.builder()
                    .idCommit(changeByCommit.getCommit().getId().value())
                    .nombreDeClase(nombreDeClase)
                    .fecha(changeByCommit.getCommit().getCommitDate())
                    .usuario(usuarioAuthor.getApellido() + " " + usuarioAuthor.getNombre() + "(" + usuarioAuthor.getUsername() + ")")
                    .cambios(this.getValoresCambiadosDTO(changeByCommit.get()))
                    .tipoDeOperacion(changeByCommit.getCommit().getProperties().get(TipoDeOperacion.class.getSimpleName()))
                    .build());
        });
        return changesDTO;
    }

    @Override
    public List<CambioDTO> getValoresCambiadosDTO(List<Change> changes) {
        var valuesChanges = new ArrayList<CambioDTO>();
        changes.forEach(change -> {
            switch (change.getClass().getSimpleName()) {
                case "ValueChange" -> {
                    var valueChange = (ValueChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .valorSiguiente(valueChange.getRight() == null ? "" : valueChange.getRight().toString())
                            .valorAnterior(valueChange.getLeft() == null ? "" : valueChange.getLeft().toString())
                            .atributo(valueChange.getPropertyName())
                            .build());
                }
                case "ObjectRemoved" -> {
                    var objectRemoved = (ObjectRemoved) change;
                    valuesChanges.add(CambioDTO.builder()
                            .atributo(objectRemoved.getAffectedObject().getClass().getSimpleName())
                            .build());
                }
                case "ReferenceChange" -> {
                    var referenceChange = (ReferenceChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .valorSiguiente(referenceChange.getRight() == null ? "" : referenceChange.getRight().toString())
                            .valorAnterior(referenceChange.getLeft() == null ? "" : referenceChange.getLeft().toString())
                            .atributo(referenceChange.getPropertyName())
                            .build());
                }
                case "CollectionChange" -> {
                    var collectionChange = (CollectionChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .valorSiguiente(collectionChange.getRight() == null ? "" : collectionChange.getRight().toString())
                            .valorAnterior(collectionChange.getLeft() == null ? "" : collectionChange.getLeft().toString())
                            .atributo(collectionChange.getPropertyName())
                            .build());
                }
                case "ListChange" -> {
                    var listChange = (ListChange) change; // agregar datos renglones
                    var mapRight = (LinkedTreeMap)listChange.getRight().get(0);
                    var mapLeft = listChange.getLeft() != null && !listChange.getLeft().isEmpty() ?
                            (LinkedTreeMap)listChange.getLeft().get(0) : new LinkedTreeMap<>();
                    var keySet = mapRight.keySet();
                    keySet.stream().forEach(key -> {
                        var valorSiguiente = mapRight.get(key);
                        var valorAnterior = mapLeft.get(key);
                        valuesChanges.add(CambioDTO.builder()
                                .valorSiguiente(valorSiguiente.toString())
                                .valorAnterior(valorAnterior != null ? valorAnterior.toString() : "")
                                .atributo(key.toString())
                                .build());
                    });
                }
                case "InitialValueChange" -> {
                    var initialValueChange = (InitialValueChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .atributo(initialValueChange.getPropertyName())
                            .valorAnterior("")
                            .valorSiguiente(initialValueChange.getRight().toString())
                            .build());
                }
                default -> {
                    break;
                }
            }
        });
        return valuesChanges;
    }

    @Override
    public Changes getCambios(JqlQuery jqlQuery) {
        return javers.findChanges(jqlQuery);
    }
}
