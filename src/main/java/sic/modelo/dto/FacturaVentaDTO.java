package sic.modelo.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FacturaVentaDTO extends FacturaDTO implements Serializable {

      private String razonSocialCliente = "Construcciones S.A.";
      private String nombreUsuario = "Daenerys Targaryen";

}
