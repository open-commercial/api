package org.opencommercial.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoTokenCardTest {

    private String email;
    private String cardNumber;
    private String securityCode;
    private int cardExpirationMonth;
    private int cardExpirationYear;
    private String cardHoldName;
    private String docType;
    private Long docNumber;
    private int installments;
}
