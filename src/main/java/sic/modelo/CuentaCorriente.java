package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuentacorriente")
@Data
@EqualsAndHashCode(of = {"fechaApertura", "cliente", "empresa"})
@AllArgsConstructor
@NoArgsConstructor
public class CuentaCorriente implements Serializable {
    
    @Id
    @GeneratedValue
    private Long idCuentaCorriente;
    
    private boolean eliminada;
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaApertura;
            
    @OneToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;
    
    @Transient
    private double saldo;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cuentaCorriente")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<RenglonCuentaCorriente> renglones;

}
