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
@Table(name = "cuentacorrienteproveedor")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonView(Views.Comprador.class)
public class CuentaCorrienteProveedor extends CuentaCorriente implements Serializable {

  @ManyToOne
  @JoinColumn(name = "id_Proveedor")
  @QueryInit("ubicacion.localidad.provincia")
  @NotNull(message = "{mensaje_cuenta_corriente_proveedor_vacio}")
  private Proveedor proveedor;

  public CuentaCorrienteProveedor() {}

  public CuentaCorrienteProveedor(
      long idCuentaCorriente,
      boolean eliminada,
      LocalDateTime fechaApertura,
      BigDecimal saldo,
      LocalDateTime fechaUltimoMovimiento,
      List<RenglonCuentaCorriente> renglones,
      Proveedor proveedor) {

    super(idCuentaCorriente, eliminada, fechaApertura, saldo, fechaUltimoMovimiento, renglones);
    this.proveedor = proveedor;
  }
}
