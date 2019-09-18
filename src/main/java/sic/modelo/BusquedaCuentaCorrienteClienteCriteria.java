package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaCuentaCorrienteClienteCriteria {

  private String nombreFiscal;
  private String nombreFantasia;
  private Long idFiscal;
  private Long idViajante;
  private Long idProvincia;
  private Long idLocalidad;
  private String nroDeCliente;
  private Long idEmpresa;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
