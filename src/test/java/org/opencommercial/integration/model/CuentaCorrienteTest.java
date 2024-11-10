package org.opencommercial.integration.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "fechaApertura")
@ToString(exclude = {"renglones"})
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "idCuentaCorriente",
    scope = CuentaCorrienteTest.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CuentaCorrienteClienteTest.class),
  // @JsonSubTypes.Type(value = CuentaCorrienteProveedor.class)
})
public class CuentaCorrienteTest {

  private Long idCuentaCorriente;
  private boolean eliminada;
  private LocalDateTime fechaApertura;
  private BigDecimal saldo;
  private LocalDateTime fechaUltimoMovimiento;
  private List<RenglonCuentaCorrienteTest> renglones;
}
