package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Formula;
import sic.controller.Views;

@Entity
@Table(name = "rengloncuentacorriente")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idRenglonCuentaCorriente"})
@JsonView(Views.Comprador.class)
@ToString
public class RenglonCuentaCorriente implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_renglon_cuenta_corriente")
    private Long idRenglonCuentaCorriente;
    
    @Column(nullable = false)
    private Long idMovimiento;
    
    @Column(nullable = false, name = "tipo_comprobante")
    @Enumerated(EnumType.STRING)
    private TipoDeComprobante tipoComprobante;

    private long serie;
    
    private long numero;
    
    private String descripcion;
    
    private boolean eliminado;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimiento;

    @Column(precision = 25, scale = 15)
    private BigDecimal monto;
    
    @ManyToOne
    @JoinColumn(name = "id_cuenta_corriente", referencedColumnName = "id_cuenta_corriente")
    private CuentaCorriente cuentaCorriente;
    
    @OneToOne
    @JoinColumn(name = "id_Factura", referencedColumnName = "id_Factura")  
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Factura factura;
    
    @OneToOne
    @JoinColumn(name = "idNota", referencedColumnName = "idNota")  
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Nota nota;
    
    @OneToOne
    @JoinColumn(name = "idRecibo", referencedColumnName = "idRecibo")  
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Recibo recibo;
    
    private Long CAE;

    // Formula de Hibernate no coloca en la consulta el mismo alias para los campos que Spring Data.
    @Formula(value = "(SELECT SUM(r.monto) "
            + "FROM rengloncuentacorriente r "
            + "WHERE r.id_cuenta_corriente = id_cuenta_corriente AND r.eliminado = false "
            + "AND r.id_renglon_cuenta_corriente <= id_renglon_cuenta_corriente)")
    private Double saldo;
    
}
