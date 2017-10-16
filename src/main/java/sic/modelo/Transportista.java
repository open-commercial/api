package sic.modelo;

import com.querydsl.core.annotations.QueryInit;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transportista")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre", "empresa"})
public class Transportista implements Serializable {

    @Id
    @GeneratedValue
    private long id_Transportista;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
    @QueryInit("provincia.pais")
    private Localidad localidad;

    @Column(nullable = false)
    private String web;

    @Column(nullable = false)
    private String telefono;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    private boolean eliminado;

    @Override
    public String toString() {
        return nombre;
    }

}
