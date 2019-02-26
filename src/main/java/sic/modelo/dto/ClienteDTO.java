package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(exclude = {"id_Cliente", "idEmpresa", "nombreEmpresa", "fechaAlta", "idCredencial", "nombreCredencial", "ubicacionFacturacion", "ubicacionEnvio"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteDTO implements Serializable {

  private long id_Cliente;
  private BigDecimal bonificacion;
  private String nombreFiscal;
  private String nombreFantasia;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private String email;
  private String telefono;
  private UbicacionDTO ubicacionFacturacion;
  private UbicacionDTO ubicacionEnvio;
  private String contacto;
  private Date fechaAlta;
  private Long idEmpresa;
  private String nombreEmpresa;
  private Long idViajante;
  private String nombreViajante;
  private Long idCredencial;
  private String nombreCredencial;
  private boolean predeterminado;
  private BigDecimal saldoCuentaCorriente;
  private Date fechaUltimoMovimiento;

}
