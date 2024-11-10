package org.opencommercial.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CuentaCorrienteClienteTest extends CuentaCorrienteTest {

  private ClienteTest cliente;
}
