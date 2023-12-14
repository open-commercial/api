package sic.service.impl;

import com.google.gson.internal.LinkedTreeMap;
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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import sic.modelo.TipoDeOperacion;
import sic.modelo.dto.CambioDTO;
import sic.modelo.dto.CommitDTO;
import sic.service.IAuditService;
import sic.service.IUsuarioService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class JaversAuditServiceImpl implements IAuditService {

    private final IUsuarioService usuarioService;
    private final Javers javers;
    private final MessageSource messageSource;
    private static final String ID_COMMIT_RELACIONADO = "idCommitRelacionado";
    private static final String INITIAL_VALUE_CHANGE = "InitialValueChange";
    private static final String LIST_CHANGE = "ListChange";
    private static final String COLLECTION_CHANGE = "CollectionChange";
    private static final String REFERENCE_CHANGE = "ReferenceChange";
    private static final String OBJECT_REMOVED = "ObjectRemoved";
    private static final String VALUE_CHANGE = "ValueChange";
    private static final String PROPERTY_RENGLONES="renglones";

    @Autowired
    public JaversAuditServiceImpl(IUsuarioService usuarioService, Javers javers, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.javers = javers;
        this.messageSource = messageSource;
    }

    @Override
    public String auditar(String idUsuario, Object objeto, Map<String, String> propiedades) {
        return javers.commit(idUsuario, objeto, propiedades).getId().value();
    }

    @Override
    public <T> List<CommitDTO> getCambios(T objeto) {
        var changesDTO = new ArrayList<CommitDTO>();
        var changes = javers.findChanges(QueryBuilder.byInstance(objeto).build());
        changes.groupByCommit().forEach(changesByCommit -> {
            var author = usuarioService.getUsuarioNoEliminadoPorId(Long.parseLong(changesByCommit.getCommit().getAuthor()));
            changesDTO.add(CommitDTO.builder()
                    .idCommit(changesByCommit.getCommit().getId().value())
                    .idCommitRelacionado(changesByCommit.getCommit().getProperties().get(ID_COMMIT_RELACIONADO))
                    .fecha(changesByCommit.getCommit().getCommitDate())
                    .usuario(author.getApellido() + " " + author.getNombre() + " (" + author.getUsername() + ")")
                    .cambios(this.getDetallesCambios(changesByCommit.get()))
                    .tipoDeOperacion(changesByCommit.getCommit().getProperties().get(TipoDeOperacion.class.getSimpleName()))
                    .build());
        });
        return changesDTO;
    }

    @Override
    public List<CommitDTO> getCambios(String idCommit) {
        var changesDTO = new ArrayList<CommitDTO>();
        var query = QueryBuilder.anyDomainObject().withCommitId(CommitId.valueOf(idCommit)).build();
        var changes = javers.findChanges(query).groupByCommit();
        changes.forEach(changesByCommit -> {
            var author = usuarioService.getUsuarioNoEliminadoPorId(Long.parseLong(changesByCommit.getCommit().getAuthor()));
            changesDTO.add(CommitDTO.builder()
                    .idCommit(changesByCommit.getCommit().getId().value())
                    .fecha(changesByCommit.getCommit().getCommitDate())
                    .usuario(author.getApellido() + " " + author.getNombre() + " (" + author.getUsername() + ")")
                    .cambios(this.getDetallesCambios(changesByCommit.get()))
                    .tipoDeOperacion(changesByCommit.getCommit().getProperties().get(TipoDeOperacion.class.getSimpleName()))
                    .build());
        });
        return changesDTO;
    }

    private List<CambioDTO> getDetallesCambios(List<Change> changes) {
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
                case LIST_CHANGE -> valuesChanges.addAll(this.procesarCambiosDeListas((ListChange) change));
                case INITIAL_VALUE_CHANGE -> {
                    var initialValueChange = (InitialValueChange) change;
                    valuesChanges.add(CambioDTO.builder()
                            .atributo(initialValueChange.getPropertyName())
                            .valorAnterior("")
                            .valorSiguiente(initialValueChange.getRight().toString())
                            .build());
                }
                //default -> throw new BusinessServiceException(
                //        messageSource.getMessage("mensaje_tipo_de_cambio_no_valido", null, Locale.getDefault()));
            }
        });
        return valuesChanges;
    }

   
    private CambioDTO buildCambioDTO(String atributo, String valorAnterior, String valorSiguiente) {
        return CambioDTO.builder()
                .atributo(atributo)
                .valorAnterior(valorAnterior)
                .valorSiguiente(valorSiguiente)
                .build();
    }

    public List<CambioDTO> procesarCambiosDeListas(ListChange listChange) {
        List<CambioDTO> valuesChanges = new ArrayList<>();
        if (Objects.equals(listChange.getPropertyName(), PROPERTY_RENGLONES)) {
            this.procesarElementoDeLista(listChange.getRight().get(0), listChange, valuesChanges);
        } else {
            listChange.getRight().forEach(right -> this.procesarElementoDeLista(right, listChange, valuesChanges));
        }
        return valuesChanges;
    }

    public void procesarElementoDeLista(Object right, ListChange listChange, List<CambioDTO> valuesChanges) {
        if (right instanceof InstanceId) {
            valuesChanges.add(this.buildCambioDTO(listChange.getPropertyName(),
                    String.valueOf(listChange.getLeft().size()), String.valueOf(listChange.getRight().size())));
        }
        if (right instanceof LinkedTreeMap<?, ?> mapRight) {
            var mapLeft = listChange.getLeft() != null && !listChange.getLeft().isEmpty() ?
                    (LinkedTreeMap<?, ?>) listChange.getLeft().get(0) : new LinkedTreeMap<>();
            var keySet = mapRight.keySet();
            keySet.forEach(key -> {
                var valorSiguiente = mapRight.get(key);
                var valorAnterior = mapLeft.get(key);
                valuesChanges.add(this.buildCambioDTO(key.toString(),
                        valorAnterior != null ? valorAnterior.toString() : "",
                        valorSiguiente != null ? valorSiguiente.toString() : ""));
            });
        }
    }

}
