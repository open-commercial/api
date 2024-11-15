package org.opencommercial.model.criteria;

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
  private String idFiscal;
  private Long idProvincia;
  private Long idLocalidad;
  private Integer pagina;
  private String ordenarPor;
  private String sentido;
}
