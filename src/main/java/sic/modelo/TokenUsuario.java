package sic.modelo;

import javax.persistence.*;

//@Entity
//@Table(name = "token")
@Embeddable
public class TokenUsuario {

    @Column(length = 300)
    private String token;

    @Enumerated(EnumType.STRING)
    private Aplicacion aplicacion;
}
