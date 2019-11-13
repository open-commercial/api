package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"idProveedor", "idEmpresa", "nombreEmpresa", "nroProveedor"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProveedorDTO implements Serializable {

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
  private UbicacionDTO ubicacion;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminado;
  private BigDecimal saldoCuentaCorriente;
  private LocalDateTime fechaUltimoMovimiento;
}
