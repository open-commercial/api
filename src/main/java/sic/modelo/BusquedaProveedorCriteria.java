package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusquedaProveedorCriteria {

    private boolean buscaPorCodigo;
    private String codigo;
    private boolean buscaPorRazonSocial;
    private String razonSocial;
    private boolean buscaPorId_Fiscal;
    private String idFiscal;
    private boolean buscaPorPais;
    private Long idPais;
    private boolean buscaPorProvincia;
    private Long idProvincia;
    private boolean buscaPorLocalidad;
    private Long idLocalidad;
    private Long idEmpresa;
    private int cantRegistros;

}
