package sic.modelo;

import javax.persistence.*;

@Embeddable
public class TokenAcceso {

    @Column(length = 300)
    private String token;

    @Enumerated(EnumType.STRING)
    private Aplicacion aplicacion;
}
