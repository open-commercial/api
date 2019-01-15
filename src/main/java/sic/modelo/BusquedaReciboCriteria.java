package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaReciboCriteria {

  private boolean buscaPorFecha;
  private Date fechaDesde;
  private Date fechaHasta;
  private Long idEmpresa;
  private boolean buscaPorNumeroRecibo;
  private long numSerie;
  private long numRecibo;
  private boolean buscaPorConcepto;
  private String concepto;
  private boolean buscaPorCliente;
  private Long idCliente;
  private boolean buscaPorProveedor;
  private Long idProveedor;
  private boolean buscaPorUsuario;
  private Long idUsuario;
  private Pageable pageable;
}
