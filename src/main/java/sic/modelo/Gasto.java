package sic.modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gasto")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroGasto", "empresa"})
public class Gasto implements Serializable {

    @Id
    @GeneratedValue
    private long id_Gasto;

    private long nroGasto;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Column(nullable = false)
    private String concepto;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @OneToOne
    @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;

    @OneToOne
    @JoinColumn(name = "id_FormaDePago", referencedColumnName = "id_FormaDePago")
    private FormaDePago formaDePago;

    private double monto;

    private boolean eliminado;

}
