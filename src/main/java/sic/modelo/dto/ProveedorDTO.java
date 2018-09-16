package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProveedorDTO implements Serializable {

  private long id_Proveedor;
  private String codigo;
  private String razonSocial;
  private String direccion;
  private CondicionIVADTO condicionIVA;
  private String idFiscal;
  private String telPrimario;
  private String telSecundario;
  private String contacto;
  private String email;
  private String web;
  private LocalidadDTO localidad;
  private EmpresaDTO empresa;
  private boolean eliminado;
  private BigDecimal saldoCuentaCorriente;
  private Date fechaUltimoMovimiento;
}
