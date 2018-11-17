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
public class BusquedaCuentaCorrienteClienteCriteria {

  private boolean buscaPorNombreFiscal;
  private String nombreFiscal;
  private boolean buscaPorNombreFantasia;
  private String nombreFantasia;
  private boolean buscaPorIdFiscal;
  private Long idFiscal;
  private boolean buscaPorViajante;
  private Long idViajante;
  private boolean buscaPorPais;
  private Long idPais;
  private boolean buscaPorProvincia;
  private Long idProvincia;
  private boolean buscaPorLocalidad;
  private Long idLocalidad;
  private boolean buscarPorNroDeCliente;
  private String nroDeCliente;
  private Long idEmpresa;
  private Pageable pageable;
}
