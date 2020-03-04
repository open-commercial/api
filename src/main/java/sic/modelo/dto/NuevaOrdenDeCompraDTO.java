package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeEnvio;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NuevaOrdenDeCompraDTO {

  private Long idSucursal;
  //private Long idUsuario;
  //private Long idCliente;
  private TipoDeEnvio tipoDeEnvio;
  //private String observaciones;
  //private NuevoPagoMercadoPagoDTO nuevoPagoMercadoPago;
}
