package sic.modelo;

import lombok.Data;

@Data
public class ReCaptchaResponse {

    private boolean success;
    private String challenge_ts;
    private String hostname;
}
