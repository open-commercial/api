package org.opencommercial.integration.model;

import lombok.*;
import org.opencommercial.model.CategoriaIVA;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(
    exclude = {
      "idCliente",
      "nroCliente",
      "fechaAlta",
      "idCredencial",
      "nombreCredencial",
      "ubicacionFacturacion",
      "ubicacionEnvio",
      "detalleUbicacionDeFacturacion",
      "detalleUbicacionDeEnvio"
    })
public class ClienteTest {

  private long idCliente;
  private String nroCliente;
  private String nombreFiscal;
  private String nombreFantasia;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private UbicacionTest ubicacionFacturacion;
  private UbicacionTest ubicacionEnvio;
  private String email;
  private String telefono;
  private String contacto;
  private LocalDateTime fechaAlta;
  private Long idViajante;
  private String nombreViajante;
  private Long idCredencial;
  private String nombreCredencial;
  private boolean predeterminado;
  private boolean puedeComprarAPlazo;
  private BigDecimal saldoCuentaCorriente;
  private BigDecimal montoCompraMinima;
  private String detalleUbicacionDeFacturacion;
  private String detalleUbicacionDeEnvio;
}
