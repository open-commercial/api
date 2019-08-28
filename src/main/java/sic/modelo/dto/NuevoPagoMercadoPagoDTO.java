package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NuevoPagoMercadoPagoDTO {

  private String token;
  private String paymentMethodId;
  private String paymentTypeId;
  private String issuerId;
  private Integer installments;
  private long idCliente;
  private long idSucursal;
  private Float monto;
}
