package sic.modelo;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "aplicacion")
@Builder
public class TokenAcceso implements Serializable {

    @Column(length = 300)
    private String token;

    @Enumerated(EnumType.STRING)
    private Aplicacion aplicacion;
}
