package sic.modelo;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cuentacorriente")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"fechaApertura", "sucursal"})
@ToString(exclude = {"renglones"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idCuentaCorriente", scope = CuentaCorriente.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CuentaCorrienteCliente.class), 
  @JsonSubTypes.Type(value = CuentaCorrienteProveedor.class) 
})
public abstract class CuentaCorriente implements Serializable {

    // bug: https://jira.spring.io/browse/DATAREST-304
    @JsonGetter(value = "type")
    public String getType() {
        return this.getClass().getSimpleName();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta_corriente")
    private Long idCuentaCorriente;
    
    private boolean eliminada;
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "{mensaje_cuenta_corriente_fecha_vacia}")
    private Date fechaApertura;

    @ManyToOne
    @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
    @NotNull(message = "{mensaje_cuenta_corriente_sucursal_vacia}")
    private Sucursal sucursal;

    @Column(precision = 25, scale = 15)
    private BigDecimal saldo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaUltimoMovimiento;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cuentaCorriente")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<RenglonCuentaCorriente> renglones;

}
