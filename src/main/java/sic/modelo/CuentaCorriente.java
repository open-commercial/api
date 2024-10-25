package sic.modelo;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cuentacorriente")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "fechaApertura")
@ToString(exclude = {"renglones"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idCuentaCorriente", scope = CuentaCorriente.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
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

    @NotNull(message = "{mensaje_cuenta_corriente_fecha_vacia}")
    private LocalDateTime fechaApertura;

    @Column(precision = 25, scale = 15)
    private BigDecimal saldo;
    
    private LocalDateTime fechaUltimoMovimiento;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cuentaCorriente")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<RenglonCuentaCorriente> renglones;

}
