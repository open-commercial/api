package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeCliente;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistracionClienteAndUsuarioDTO implements Serializable {

  private TipoDeCliente tipoDeCliente;
  private String nombre;
  private String apellido;
  private String email;
  private String telefono;
  private String idFiscal;
  private String razonSocial;
  private String password;
  private long idEmpresa;
}
