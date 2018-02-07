package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "notadebito")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "renglonesNotaDebito")
public class NotaDebito extends Nota implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idNota")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private List<RenglonNotaDebito> renglonesNotaDebito;
    
    @Column(precision = 25, scale = 15)
    private BigDecimal montoNoGravado;
    
    @ManyToOne
    @JoinColumn(name = "idRecibo", referencedColumnName = "idRecibo")
    private Recibo recibo;
    
    private boolean pagada;

    public NotaDebito() {}

    public NotaDebito(long idNota, long serie, FacturaVenta facturaVenta, List<Pago> pagos, long nroNota, boolean eliminada,
            TipoDeComprobante tipoDeComprobante, Date fecha, Empresa empresa, Cliente cliente,
            Usuario usuario, String motivo, List<RenglonNotaDebito> renglones, BigDecimal subTotalBruto,
            BigDecimal iva21Neto, BigDecimal iva105Neto, BigDecimal total, BigDecimal montoNoGravado, long CAE, Date vencimientoCAE,
            long numSerieAfip, long numNotaAfip, Recibo recibo, boolean pagado) {

        super(idNota, serie, nroNota, eliminada, tipoDeComprobante, fecha, empresa, cliente, usuario,
                facturaVenta, pagos, motivo, subTotalBruto, iva21Neto, iva105Neto, total, CAE, vencimientoCAE, numSerieAfip, numNotaAfip);
        this.montoNoGravado = montoNoGravado;
        this.renglonesNotaDebito = renglones;
        this.recibo = recibo;
        this.pagada = pagado;
    }

}
