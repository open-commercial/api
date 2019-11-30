package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"idCliente", "nroCliente", "fechaAlta", "idCredencial", "nombreCredencial", "ubicacionFacturacion", "ubicacionEnvio"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteDTO implements Serializable {

  private long idCliente;
  private BigDecimal bonificacion;
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
  private boolean predeterminado;
  private BigDecimal saldoCuentaCorriente;
  private LocalDateTime fechaUltimoMovimiento;

}
