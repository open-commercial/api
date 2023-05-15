package sic.service.impl;

import com.google.gson.internal.LinkedTreeMap;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.commit.CommitId;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.InitialValueChange;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.modelo.dto.CambioDTO;
import sic.modelo.dto.CommitDTO;
import sic.service.IAuditService;
import sic.service.IUsuarioService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JaversServiceImpl implements IAuditService {

    private final IUsuarioService usuarioService;
    private final Javers javers;
    private static final String ID_COMMIT_RELACIONADO = "idCommitRelacionado";
    private static final String INITIAL_VALUE_CHANGE = "InitialValueChange";
    private static final String LIST_CHANGE = "ListChange";
    private static final String COLLECTION_CHANGE = "CollectionChange";
    private static final String REFERENCE_CHANGE = "ReferenceChange";
    private static final String OBJECT_REMOVED = "ObjectRemoved";
    private static final String VALUE_CHANGE = "ValueChange";

    @Autowired
    public JaversServiceImpl(IUsuarioService usuarioService, Javers javers) {
        this.usuarioService = usuarioService;
        this.javers = javers;
    }

    @Override
    public String auditar(String idUsuario, Object objeto, Map<String, String> propiedades) {
        return javers.commit(idUsuario, objeto, propiedades).getId().value();
    }

    @Override
    public String auditar(String idUsuario, Object objeto) {
        return javers.commit(idUsuario, objeto).getId().value();
    }

    @Override
    public <T> List<CommitDTO> getCambiosDTO(T objeto) {
        var changesDTO = new ArrayList<CommitDTO>();
        this.getChanges(objeto).groupByCommit().forEach(changesByCommit -> {
            Usuario usuarioAuthor = usuarioService.getUsuarioNoEliminadoPorId(Long.parseLong(changesByCommit.getCommit().getAuthor()));
            changesDTO.add(CommitDTO.builder()
                    .idCommit(changesByCommit.getCommit().getId().value())
                    .idCommitRelacionado(changesByCommit.getCommit().getProperties().get(ID_COMMIT_RELACIONADO))
                    .fecha(changesByCommit.getCommit().getCommitDate())
                    .usuario(usuarioAuthor.getApellido() + " " + usuarioAuthor.getNombre() + "(" + usuarioAuthor.getUsername() + ")")
                    .cambios(this.getDetallesCambiosDTO(changesByCommit.get()))
                    .tipoDeOperacion(changesByCommit.getCommit().getProperties().get(TipoDeOperacion.class.getSimpleName()))
                    .build());
        });
        return changesDTO;
    }

    @Override
    public List<CommitDTO> getCambiosDTO(String idCommit) {
        var changesDTO = new ArrayList<CommitDTO>();
        var query = QueryBuilder.anyDomainObject().withCommitId(CommitId.valueOf(idCommit)).build();
        var cambios = javers.findChanges(query).groupByCommit();
        cambios.forEach(changesByCommit -> {
            Usuario usuarioAuthor = usuarioService.getUsuarioNoEliminadoPorId(Long.parseLong(changesByCommit.getCommit().getAuthor()));
            changesDTO.add(CommitDTO.builder()
                    .idCommit(changesByCommit.getCommit().getId().value())
                    .fecha(changesByCommit.getCommit().getCommitDate())
                    .usuario(usuarioAuthor.getApellido() + " " + usuarioAuthor.getNombre() + "(" + usuarioAuthor.getUsername() + ")")
                    .cambios(this.getDetallesCambiosDTO(changesByCommit.get()))
                    .tipoDeOperacion(changesByCommit.getCommit().getProperties().get(TipoDeOperacion.class.getSimpleName()))
                    .build());
        });
        return changesDTO;
    }

    private List<CambioDTO> getDetallesCambiosDTO(List<Change> changes) {
        var valuesChanges = new ArrayList<CambioDTO>();
        changes.forEach(change -> {
            switch (change.getClass().getSimpleName()) {
                case VALUE_CHANGE -> {
                    var valueChange = (ValueChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .valorSiguiente(valueChange.getRight() == null ? "" : valueChange.getRight().toString())
                            .valorAnterior(valueChange.getLeft() == null ? "" : valueChange.getLeft().toString())
                            .atributo(valueChange.getPropertyName())
                            .build());
                }
                case OBJECT_REMOVED -> {
                    var objectRemoved = (ObjectRemoved) change;
                    valuesChanges.add(CambioDTO.builder()
                            .atributo(objectRemoved.getAffectedObject().getClass().getSimpleName())
                            .build());
                }
                case REFERENCE_CHANGE -> {
                    var referenceChange = (ReferenceChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .valorSiguiente(referenceChange.getRight() == null ? "" : referenceChange.getRight().toString())
                            .valorAnterior(referenceChange.getLeft() == null ? "" : referenceChange.getLeft().toString())
                            .atributo(referenceChange.getPropertyName())
                            .build());
                }
                case COLLECTION_CHANGE -> {
                    var collectionChange = (ListChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .valorSiguiente(collectionChange.getRight() == null ? "" : collectionChange.getRight().toString())
                            .valorAnterior(collectionChange.getLeft() == null ? "" : collectionChange.getLeft().toString())
                            .atributo(collectionChange.getPropertyName())
                            .build());
                }
                case LIST_CHANGE -> {
                    var listChange = (ListChange) change;
                    if (listChange.getRight().get(0) instanceof InstanceId) {
                        valuesChanges.add(CambioDTO.builder()
                                .atributo(listChange.getPropertyName())
                                .valorAnterior(String.valueOf(listChange.getLeft().size()))
                                .valorSiguiente(String.valueOf(listChange.getRight().size()))
                                .build());
                    }
                    if (listChange.getRight().get(0) instanceof LinkedTreeMap<?,?> mapRight) {
                        var mapLeft = listChange.getLeft() != null && !listChange.getLeft().isEmpty() ?
                                (LinkedTreeMap<?,?>) listChange.getLeft().get(0) : new LinkedTreeMap<>();
                        var keySet = mapRight.keySet();
                        keySet.forEach(key -> {
                            var valorSiguiente = mapRight.get(key);
                            var valorAnterior = mapLeft.get(key);
                            valuesChanges.add(CambioDTO.builder()
                                    .valorSiguiente(valorSiguiente != null ? valorSiguiente.toString() : "")
                                    .valorAnterior(valorAnterior != null ? valorAnterior.toString() : "")
                                    .atributo(key.toString())
                                    .build());
                        });
                    }
                }
                case INITIAL_VALUE_CHANGE -> {
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

    private <T> Changes getChanges(T object) {
        var queryBuilder = QueryBuilder.byInstance(object).build();
        return javers.findChanges(queryBuilder);
    }
}
