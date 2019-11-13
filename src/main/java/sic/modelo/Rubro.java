package sic.modelo;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "rubro")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre", "empresa"})
@ToString
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties({"idEmpresa", "nombreEmpresa"})
public class Rubro implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Rubro")
    private long idRubro;

    @Column(nullable = false)
    @NotNull(message = "{mensaje_rubro_nombre_vacio}")
    @NotEmpty(message = "{mensaje_rubro_nombre_vacio}")
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    @NotNull(message = "{mensaje_rubro_empresa_vacia}")
    private Empresa empresa;

    private boolean eliminado;

    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }

    @JsonGetter("idEmpresa")
    public long getIdEmpresa() {
        return empresa.getIdEmpresa();
    }

}
