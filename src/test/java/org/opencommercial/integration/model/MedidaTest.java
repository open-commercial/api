package org.opencommercial.integration.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "idMedida")
@Builder
public class MedidaTest {

    private long idMedida;
    private String nombre;
    private boolean eliminada;
}
