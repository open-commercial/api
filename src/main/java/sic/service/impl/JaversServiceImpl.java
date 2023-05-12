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
import org.javers.core.diff.changetype.container.CollectionChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    public JaversServiceImpl(IUsuarioService usuarioService, Javers javers) {
        this.usuarioService = usuarioService;
        this.javers = javers;
    }

    @Override
    @Transactional
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
                    .cambios(this.getValoresCambiadosDTO(changesByCommit.get()))
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
                    .cambios(this.getValoresCambiadosDTO(changesByCommit.get()))
                    .tipoDeOperacion(changesByCommit.getCommit().getProperties().get(TipoDeOperacion.class.getSimpleName()))
                    .build());
        });
        return changesDTO;
    }

    private List<CambioDTO> getValoresCambiadosDTO(List<Change> changes) {
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
                    var listChange = (ListChange) change;
                    if (listChange.getRight().get(0) instanceof InstanceId) {
                        valuesChanges.add(CambioDTO.builder()
                                .atributo(listChange.getPropertyName())
                                .valorAnterior(String.valueOf(listChange.getLeft().size()))
                                .valorSiguiente(String.valueOf(listChange.getRight().size()))
                                .build());
                        System.out.println(CambioDTO.builder()
                                .atributo(listChange.getPropertyName())
                                .valorAnterior(listChange.getLeft().toString())
                                .valorSiguiente(listChange.getRight().toString())
                                .build());
                    }
                    if (listChange.getRight().get(0) instanceof LinkedTreeMap) {
                        var mapRight = (LinkedTreeMap) listChange.getRight().get(0);
                        var mapLeft = listChange.getLeft() != null && !listChange.getLeft().isEmpty() ?
                                (LinkedTreeMap) listChange.getLeft().get(0) : new LinkedTreeMap<>();
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

    private <T> List<CambioDTO> getCambios(T object) {
        return this.getValoresCambiadosDTO(this.getChanges(object));
    }

    private <T> Changes getChanges(T object) {
        var queryBuilder = QueryBuilder.byInstance(object).build();
        return javers.findChanges(queryBuilder);
    }
}
