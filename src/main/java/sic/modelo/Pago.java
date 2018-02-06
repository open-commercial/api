package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import lombok.ToString;

@Entity
@Table(name = "pago")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroPago", "empresa"})
@ToString
@JsonIgnoreProperties({"factura", "notaDebito","empresa", "formaDePago", "recibo"})
public class Pago implements Serializable {

    @Id
    @GeneratedValue
    private Long id_Pago;
    
    private long nroPago;

    @ManyToOne
    @JoinColumn(name = "id_FormaDePago", referencedColumnName = "id_FormaDePago")
    private FormaDePago formaDePago;

    @ManyToOne
    @JoinColumn(name = "id_Factura", referencedColumnName = "id_Factura")    
    private Factura factura;
    
    @ManyToOne
    @JoinColumn(name = "idNota", referencedColumnName = "idNota")    
    private Nota notaDebito;
    
    @ManyToOne
    @JoinColumn(name = "idRecibo", referencedColumnName = "idRecibo")
    private Recibo recibo;

    @Column(nullable = false)
    private double monto;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    private String nota;

    @ManyToOne
    @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
    private Empresa empresa;

    private boolean eliminado;
    
    @JsonGetter("nombreEmpresa")
    public String getNombreEmpresa() {
        return empresa.getNombre();
    }
    
    @JsonGetter("nombreFormaDePago")
    public String getNombreFormaDePago() {
        return this.formaDePago.getNombre();
    }

}