package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaClienteCriteria {

    private boolean buscaPorRazonSocial;
    private String razonSocial;
    private boolean buscaPorNombreFantasia;
    private String nombreFantasia;
    private boolean buscaPorId_Fiscal;
    private String idFiscal;
    private boolean buscaPorViajante;
    private Usuario viajante;
    private boolean buscaPorPais;
    private Pais pais;
    private boolean buscaPorProvincia;
    private Provincia provincia;
    private boolean buscaPorLocalidad;
    private Localidad localidad;
    private Empresa empresa;
    private Pageable pageable;
    private boolean conSaldo;
}
