package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Formula;
import sic.config.Views;

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

    @NotNull(message = "{mensaje_renglon_cuenta_corriente_fecha_vacia}")
    private LocalDateTime fecha;

    @Column(precision = 25, scale = 15)
    private BigDecimal monto;
    
    @ManyToOne
    @JoinColumn(name = "id_cuenta_corriente", referencedColumnName = "id_cuenta_corriente")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    @OneToOne
    @JoinColumn(name = "idRemito", referencedColumnName = "idRemito")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Remito remito;

    @JsonGetter("idSucursal")
    public Long getIdSucursal() {
        Long idSucursal = null;
        if (factura != null) idSucursal = factura.getIdSucursal();
        if (nota != null) idSucursal = nota.getIdSucursal();
        if (recibo != null) idSucursal = recibo.getIdSucursal();
        if (remito != null) idSucursal = remito.getIdSucursal();
        return idSucursal;
    }

    @JsonGetter("nombreSucursal")
    public String getNombreSucursal() {
        String nombreSucursal = "";
        if (factura != null) nombreSucursal = factura.getNombreSucursal();
        if (nota != null) nombreSucursal = nota.getNombreSucursal();
        if (recibo != null) nombreSucursal = recibo.getNombreSucursal();
        if (remito != null) nombreSucursal = remito.getNombreSucursal();
        return nombreSucursal;
    }

    // Formula de Hibernate no coloca en la consulta el mismo alias para los campos que Spring Data.
    @Formula(value = "(SELECT SUM(r.monto) "
            + "FROM rengloncuentacorriente r "
            + "WHERE r.id_cuenta_corriente = id_cuenta_corriente AND r.eliminado = false "
            + "AND r.id_renglon_cuenta_corriente <= id_renglon_cuenta_corriente)")
    private Double saldo;
    
}
