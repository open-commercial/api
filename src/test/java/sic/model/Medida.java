package sic.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "idMedida")
@Builder
public class Medida {

    private long idMedida;
    private String nombre;
    private boolean eliminada;
}
