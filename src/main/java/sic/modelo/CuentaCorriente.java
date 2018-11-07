package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "cuentacorriente")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"fechaApertura", "empresa"})
@ToString(exclude = {"renglones"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idCuentaCorriente", scope = CuentaCorriente.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CuentaCorrienteCliente.class), 
  @JsonSubTypes.Type(value = CuentaCorrienteProveedor.class) 
})
public abstract class CuentaCorriente implements Serializable {
    
    @Id
    @GeneratedValue
    @Column(name = "id_cuenta_corriente")
    private Long idCuentaCorriente;
    
    private boolean eliminada;
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaApertura;
    
    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    @Column(precision = 25, scale = 15)
    @ColumnDefault("0")
    private BigDecimal saldo;
    
    @Transient
    private Date fechaUltimoMovimiento;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cuentaCorriente")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<RenglonCuentaCorriente> renglones;

}
