package org.opencommercial.model.dto;

import lombok.*;
import org.opencommercial.model.CategoriaIVA;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteDTO implements Serializable {

  private long idCliente;
  private String nroCliente;
  private String nombreFiscal;
  private String nombreFantasia;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private UbicacionDTO ubicacionFacturacion;
  private UbicacionDTO ubicacionEnvio;
  private String email;
  private String telefono;
  private String contacto;
  private LocalDateTime fechaAlta;
  private Long idViajante;
  private String nombreViajante;
  private Long idCredencial;
  private String nombreCredencial;
  private Boolean predeterminado;
  private Boolean puedeComprarAPlazo;
  private BigDecimal saldoCuentaCorriente;
  private BigDecimal montoCompraMinima;
  private String detalleUbicacionDeFacturacion;
  private String detalleUbicacionDeEnvio;

}
