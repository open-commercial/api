package sic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenglonRemito {

  private long idRenglonRemito;
  private String codigoItem;
  private String descripcionItem;
  private String medidaItem;
  private BigDecimal cantidad;
}
