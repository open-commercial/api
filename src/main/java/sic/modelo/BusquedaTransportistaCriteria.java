package sic.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusquedaTransportistaCriteria {

    private boolean buscarPorNombre;
    private String nombre;
    private boolean buscarPorPais;
    private Pais pais;
    private boolean buscarPorProvincia;
    private Provincia provincia;
    private boolean buscarPorLocalidad;
    private Localidad localidad;
    private Empresa empresa;
}
