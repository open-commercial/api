package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NuevoPagoMercadoPagoDTO { //NuevaPreferenceMercadoPagoDTO

  private String token;
  @NotBlank(message = "{mensaje_pago_sin_payment_method_id}")
  private String paymentMethodId;
  private String paymentTypeId;
  private String issuerId;
  private Integer installments;
  private long idCliente;
  @NotNull(message = "{mensaje_pago_sin_sucursal}")
  private Long idSucursal;
  private Float monto;
}
