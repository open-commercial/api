package sic.modelo.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaProveedorCriteria {

  private String nroProveedor;
  private String razonSocial;
  private Long idFiscal;
  private Long idProvincia;
  private Long idLocalidad;
  private Long idEmpresa;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
