package org.opencommercial.model.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaClienteCriteria {

    private String nombreFiscal;
    private String nombreFantasia;
    private String idFiscal;
    private Long idViajante;
    private Long idProvincia;
    private Long idLocalidad;
    private String nroDeCliente;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
}
