package sic.modelo;

import lombok.*;

import javax.persistence.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "aplicacion")
@Builder
public class TokenAcceso {

    @Column(length = 300)
    private String token;

    @Enumerated(EnumType.STRING)
    private Aplicacion aplicacion;
}
