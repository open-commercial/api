package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "cuentacorrientecliente")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({"empresa"})
public class CuentaCorrienteCliente extends CuentaCorriente implements Serializable {

    @OneToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    private Cliente cliente;

    @JsonGetter("nombreFiscalCliente")
    public String getNombreFiscalCliente() {
        return cliente.getNombreFiscal();
    }

    public CuentaCorrienteCliente() {}

    public CuentaCorrienteCliente(long idCuentaCorriente, boolean eliminada, Date fechaApertura, Empresa empresa,
            BigDecimal saldo, Date fechaUltimoMovimiento, List<RenglonCuentaCorriente> renglones, Cliente cliente) {

        super(idCuentaCorriente, eliminada, fechaApertura, empresa, saldo, fechaUltimoMovimiento, renglones);
        this.cliente = cliente;
    }

}
