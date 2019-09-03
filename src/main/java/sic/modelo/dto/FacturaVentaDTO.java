package sic.modelo.dto;

import java.io.Serializable;

import lombok.*;
import sic.modelo.CategoriaIVA;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {
      "idCliente",
      "nombreFiscalCliente",
      "nroDeCliente",
      "categoriaIVACliente",
      "idViajanteCliente",
      "nombreViajanteCliente",
      "ubicacionCliente"
    })
@Builder
public class FacturaVentaDTO extends FacturaDTO implements Serializable {

  private Long idCliente;
  private String nombreFiscalCliente;
  private String nroDeCliente;
  private CategoriaIVA categoriaIVACliente;
  private Long idViajanteCliente;
  private String nombreViajanteCliente;
  private String ubicacionCliente;
}
