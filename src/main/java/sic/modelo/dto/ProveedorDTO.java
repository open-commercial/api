package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import sic.modelo.CategoriaIVA;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(exclude = {"id_Proveedor", "idLocalidad", "nombreLocalidad", "nombreLocalidad", "idEmpresa", "nombreEmpresa"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProveedorDTO implements Serializable {

  private long id_Proveedor;
  private String codigo;
  private String razonSocial;
  private String direccion;
  private CategoriaIVA categoriaIVA;
  private Long idFiscal;
  private String telPrimario;
  private String telSecundario;
  private String contacto;
  private String email;
  private String web;
  private Long idLocalidad;
  private String nombreLocalidad;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminado;
  private BigDecimal saldoCuentaCorriente;
  private Date fechaUltimoMovimiento;
}
