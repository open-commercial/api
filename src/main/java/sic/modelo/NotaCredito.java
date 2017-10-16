package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "notacredito")
@Data
@EqualsAndHashCode(callSuper = true)
public class NotaCredito extends Nota implements Serializable {
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idNota")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private List<RenglonNotaCredito> renglonesNotaCredito;
    
    @Column(nullable = false)
    private double subTotal;
    
    @Column(nullable = false)
    private double recargoPorcentaje;
    
    @Column(nullable = false)
    private double recargoNeto;
    
    @Column(nullable = false)
    private double descuentoPorcentaje;
    
    @Column(nullable = false)
    private double descuentoNeto;

    public NotaCredito() {}

    public NotaCredito(long idNota, long serie, FacturaVenta facturaVenta, List<Pago> pagos, long nroNota, boolean eliminada,
            TipoDeComprobante tipoDeComprobante, Date fecha, Empresa empresa, Cliente cliente,
            Usuario usuario, String motivo, List<RenglonNotaCredito> renglones, double subTotalBruto, double iva21Neto,
            double iva105Neto, double total, double montoNoGravado, long CAE, Date vencimientoCAE,
            long numSerieAfip, long numFacturaAfip) {

        super(idNota, serie, facturaVenta, pagos, nroNota, eliminada, tipoDeComprobante, fecha, empresa, cliente, usuario, motivo,
                subTotalBruto, iva21Neto, iva105Neto, total, CAE, vencimientoCAE, numSerieAfip, numFacturaAfip);
        this.renglonesNotaCredito = renglones;
    }

}
