package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pago")
@Data
@EqualsAndHashCode(of = {"nroPago", "empresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Pago", scope = Pago.class)
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
        
    public Pago() {}

    public Pago(Long id_Pago, long nroPago, FormaDePago formaDePago, Factura factura, NotaDebito notaDebito,
            double monto, Date fecha, String nota, Empresa empresa, boolean eliminado) {
        
        this.id_Pago = id_Pago;
        this.nroPago = nroPago;
        this.formaDePago = formaDePago;
        this.factura = factura;
        this.notaDebito = notaDebito;
        this.monto = monto;
        this.fecha = fecha;
        this.nota = nota;
        this.empresa = empresa;
        this.eliminado = eliminado;
    }

}
