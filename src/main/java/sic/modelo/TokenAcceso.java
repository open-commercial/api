package sic.modelo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Embeddable
@Data
@EqualsAndHashCode(of = "aplicacion")
public class TokenAcceso {

    @Column(length = 300)
    private String token;

    @Enumerated(EnumType.STRING)
    private Aplicacion aplicacion;
}
