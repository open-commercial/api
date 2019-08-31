package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.controller.Views;

@Entity
@Table(name = "cuentacorrientecliente")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonView(Views.Comprador.class)
public class CuentaCorrienteCliente extends CuentaCorriente implements Serializable {

    @OneToOne
    @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
    @QueryInit({"viajante", "ubicacionFacturacion.localidad.provincia", "ubicacionEnvio.localidad.provincia"})
    @NotNull(message = "{mensaje_cuenta_corriente_cliente_vacio}")
    private Cliente cliente;

    public CuentaCorrienteCliente() {}

    public CuentaCorrienteCliente(long idCuentaCorriente, boolean eliminada, Date fechaApertura,
            BigDecimal saldo, Date fechaUltimoMovimiento, List<RenglonCuentaCorriente> renglones, Cliente cliente) {
        super(idCuentaCorriente, eliminada, fechaApertura, saldo, fechaUltimoMovimiento, renglones);
        this.cliente = cliente;
    }

}
