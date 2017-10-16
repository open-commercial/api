package sic.modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
public class Empresa implements Serializable {

    @Id
    @GeneratedValue
    private long id_Empresa;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String lema;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "id_CondicionIVA", referencedColumnName = "id_CondicionIVA")
    private CondicionIVA condicionIVA;

    private long cuip;

    private long ingresosBrutos;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicioActividad;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefono;

    @ManyToOne
    @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
    private Localidad localidad;
    
    private String logo;

    private boolean eliminada;

    @Override
    public String toString() {
        return nombre;
    }

}
