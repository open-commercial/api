package sic.modelo.dto;

import lombok.*;
import sic.modelo.CategoriaIVA;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(
    exclude = {
      "id_Proveedor",
      "idEmpresa",
      "nombreEmpresa"
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProveedorDTO implements Serializable {

  private long id_Proveedor;
  private String codigo;
  private String razonSocial;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private String telPrimario;
  private String telSecundario;
  private String contacto;
  private String email;
  private String web;
  private Long idUbicacion;
  private String detalleUbicacion;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminado;
  private BigDecimal saldoCuentaCorriente;
  private Date fechaUltimoMovimiento;
}
