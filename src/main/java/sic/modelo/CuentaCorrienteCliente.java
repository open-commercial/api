package sic.modelo;

import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.config.Views;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cuentacorrientecliente")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonView(Views.Comprador.class)
public class CuentaCorrienteCliente extends CuentaCorriente implements Serializable {

  @ManyToOne
  @JoinColumn(name = "id_Cliente")
  @QueryInit({
    "viajante",
    "ubicacionFacturacion.localidad.provincia",
    "ubicacionEnvio.localidad.provincia"
  })
  @NotNull(message = "{mensaje_cuenta_corriente_cliente_vacio}")
  private Cliente cliente;

  public CuentaCorrienteCliente() {}

  public CuentaCorrienteCliente(
      long idCuentaCorriente,
      boolean eliminada,
      LocalDateTime fechaApertura,
      BigDecimal saldo,
      LocalDateTime fechaUltimoMovimiento,
      List<RenglonCuentaCorriente> renglones,
      Cliente cliente) {

    super(idCuentaCorriente, eliminada, fechaApertura, saldo, fechaUltimoMovimiento, renglones);
    this.cliente = cliente;
  }
}
