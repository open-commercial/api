package sic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenCard {

    private String id;
    private String public_key;
    private Long first_six_digits;
    private Long last_six_digits;
    //cardholder?
    private String status;
    private Date date_created;
    private Date date_last_updated;
    private Date date_due;
    private boolean luhn_validation;
    private boolean live_mode;
    private boolean require_esc;
    private int card_number_lenght;
}
