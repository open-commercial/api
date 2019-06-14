package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoMercadoPagoDTO {

  private String token;
  private String paymentMethodId;
  private String issuerId;
  private Integer installments;
}
