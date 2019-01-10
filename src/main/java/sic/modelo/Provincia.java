package sic.modelo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "provincia")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
public class Provincia implements Serializable {

    @Id
    @GeneratedValue
    private long id_Provincia;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_Pais", referencedColumnName = "id_Pais")
    private Pais pais;

    private boolean eliminada;

    @JsonGetter("idPais")
    public long getIdPais() {
        return pais.getId_Pais();
    }

    @JsonGetter("nombrePais")
    public String getNombrePais() {
        return pais.getNombre();
    }

}