package org.opencommercial.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenglonCuentaCorrienteTest {

  private Long idRenglonCuentaCorriente;
  private Long idMovimiento;
  private TipoDeComprobante tipoComprobante;
  private long serie;
  private long numero;
  private long descripcion;
  private boolean eliminado;
  private BigDecimal monto;
  private Long idSucursal;
  private String nombreSucursal;
  private Double saldo;
}
