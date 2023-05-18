package sic.modelo;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.Hibernate;
import sic.controller.Views;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "envioDeCorreoGrupal")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonView(Views.Encargado.class)
@Builder
public class EnvioDeCorreoGrupal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idEnvioDeCorreoGrupal;

    private LocalDateTime fecha;

    @ElementCollection(targetClass = Rol.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "rol", joinColumns = @JoinColumn(name = "idEnvioDeCorreoGrupal"))
    @Enumerated(EnumType.STRING)
    @Column(name = "nombre")
    @NotEmpty(message = "{mensaje_usuario_no_selecciono_rol}")
    private List<Rol> roles;

    private String asunto;

    private String mensaje;

    private String descripcionAdjunto;

    @Lob
    private byte[] reporteAdjunto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EnvioDeCorreoGrupal that = (EnvioDeCorreoGrupal) o;
        return idEnvioDeCorreoGrupal != 0L && Objects.equals(idEnvioDeCorreoGrupal, that.idEnvioDeCorreoGrupal);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
