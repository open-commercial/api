package sic.model;

import lombok.Builder;
import lombok.Data;
import sic.domain.CategoriaIVA;
import sic.dto.UbicacionDTO;

import java.time.LocalDateTime;

@Data
@Builder
public class NuevaSucursal {

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
