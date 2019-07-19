package sic.modelo.dto;

import com.mercadopago.resources.Payment;
import com.mercadopago.resources.datastructures.payment.Payer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagoMercadoPagoDTO {

  private String id;
  private Date dateCreated;
  private Date dateApproved;
  private Date dateLastUpdated;
  private Date moneyReleaseDate;
  private Payer payer;
  private Float transactionAmount;
  private Payment.Status status;
  private String issuerId;
  private Integer installments;
}
