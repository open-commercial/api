package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaProveedorCriteria {
  
  private boolean buscaPorCodigo;
  private String codigo;
  private boolean buscaPorRazonSocial;
  private String razonSocial;
  private boolean buscaPorIdFiscal;
  private Long idFiscal;
  private boolean buscaPorPais;
  private Long idPais;
  private boolean buscaPorProvincia;
  private Long idProvincia;
  private boolean buscaPorLocalidad;
  private Long idLocalidad;
  private Long idEmpresa;
  private Pageable pageable;
  private boolean conSaldo;
}
