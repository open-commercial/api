package sic.model;

import lombok.*;
import sic.domain.CategoriaIVA;

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
public class Cliente {

  private long idCliente;
  private String nroCliente;
  private String nombreFiscal;
  private String nombreFantasia;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private Ubicacion ubicacionFacturacion;
  private Ubicacion ubicacionEnvio;
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
