package org.opencommercial.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.config.Views;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Encargado.class)
public class EntidadMontoDTO {

  private String entidad;

  private BigDecimal monto;

}
