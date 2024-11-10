package org.opencommercial.integration.model;

import lombok.*;
import org.opencommercial.model.CategoriaIVA;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"idProveedor", "nroProveedor"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProveedorTest {

  private long idProveedor;
  private String nroProveedor;
  private String razonSocial;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private String telPrimario;
  private String telSecundario;
  private String contacto;
  private String email;
  private String web;
  private UbicacionTest ubicacion;
  private boolean eliminado;
  private BigDecimal saldoCuentaCorriente;
  private LocalDateTime fechaUltimoMovimiento;
}
