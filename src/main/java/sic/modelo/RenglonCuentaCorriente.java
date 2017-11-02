package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "rengloncuentacorriente")
@Data
@EqualsAndHashCode(of = {"idRenglonCuentaCorriente"})
@ToString(exclude = {"cuentaCorriente"})
@AllArgsConstructor
@NoArgsConstructor
public class RenglonCuentaCorriente implements Serializable  {
    
    @Id
    @GeneratedValue
    private Long idRenglonCuentaCorriente;
    
    @Column(nullable = false)
    private Long idMovimiento;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;
    
    @Column(nullable = false)
    private String comprobante;
    
    private String descripcion;
    
    private boolean eliminado;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimiento;

    @Column(nullable = false)
    private double monto;
    
    @ManyToOne
    @JoinColumn(name = "idCuentaCorriente", referencedColumnName = "idCuentaCorriente")
    private CuentaCorriente cuentaCorriente;
    
    @OneToOne
    @JoinColumn(name = "id_Factura", referencedColumnName = "id_Factura")  
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Factura factura;
  
    @OneToOne
    @JoinColumn(name = "id_Pago", referencedColumnName = "id_Pago")  
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Pago pago;
    
    @OneToOne
    @JoinColumn(name = "idNota", referencedColumnName = "idNota")  
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Nota nota;
    
    @Transient
    private long CAE;
    
    @Transient
    private double saldo;
    
}
