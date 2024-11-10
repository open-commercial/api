package org.opencommercial.integration.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(
    exclude = {
      "idRecibo",
      "numSerie",
      "numRecibo",
      "fecha",
      "nombreSucursal",
      "razonSocialProveedor",
      "nombreUsuario",
      "nombreFormaDePago",
      "nombreFiscalCliente"
    })
public class ReciboTest {

  private Long idRecibo;
  private long numSerie;
  private long numRecibo;
  private LocalDateTime fecha;
  private boolean eliminado;
  private String concepto;
  private long idFormaDePago;
  private String nombreFormaDePago;
  private long idSucursal;
  private String nombreSucursal;
  private Long idCliente;
  private String nombreFiscalCliente;
  private Long idProveedor;
  private String razonSocialProveedor;
  private String nombreUsuario;
  private Long idViajante;
  private String nombreViajante;
  private double monto;
}
