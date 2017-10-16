package sic.modelo;

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
@Table(name = "formadepago")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre", "empresa"})
public class FormaDePago implements Serializable {

    @Id
    @GeneratedValue
    private long id_FormaDePago;

    @Column(nullable = false)
    private String nombre;

    private boolean afectaCaja;

    private boolean predeterminado;
    
    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    private boolean eliminada;

    @Override
    public String toString() {
        return nombre;
    }

}
