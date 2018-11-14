package sic.modelo.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"nombreFiscalCliente", "nombreUsuario"})
@Builder
public class FacturaVentaDTO extends FacturaDTO implements Serializable {

      private String nombreFiscalCliente;
      private String nombreUsuario;

}
