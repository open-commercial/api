package org.opencommercial.integration.model;

import lombok.Builder;
import lombok.Data;
import org.opencommercial.model.CategoriaIVA;
import org.opencommercial.model.dto.UbicacionDTO;

import java.time.LocalDateTime;

@Data
@Builder
public class NuevaSucursalTest {

    private String nombre;
    private String lema;
    private CategoriaIVA categoriaIVA;
    private Long idFiscal;
    private Long ingresosBrutos;
    private LocalDateTime fechaInicioActividad;
    private String email;
    private String telefono;
    private UbicacionDTO ubicacion;
    private byte[] imagen;
}
