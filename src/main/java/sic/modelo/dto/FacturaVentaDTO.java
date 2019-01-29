package sic.modelo.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"idCliente", "nombreFiscalCliente", "idViajante", "nombreViajante"})
@Builder
public class FacturaVentaDTO extends FacturaDTO implements Serializable {

      private Long idCliente;
      private String nombreFiscalCliente;
      private Long idViajante;
      private String nombreViajante;

}
