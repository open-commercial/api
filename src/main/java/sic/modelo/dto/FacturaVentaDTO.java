package sic.modelo.dto;

import java.io.Serializable;

import lombok.*;
import sic.modelo.CategoriaIVA;

@Data
@EqualsAndHashCode(
    callSuper = true,
    exclude = {
      "idCliente",
      "nombreFiscalCliente",
      "nroDeCliente",
      "categoriaIVA",
      "idViajante",
      "nombreViajante",
      "ubicacion"
    })
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacturaVentaDTO extends FacturaDTO implements Serializable {

  private Long idCliente;
  private String nombreFiscalCliente;
  private String nroDeCliente;
  private CategoriaIVA categoriaIVA;
  private Long idViajante;
  private String nombreViajante;
  private String ubicacion;
}
