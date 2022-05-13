package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.CategoriaIVA;
import sic.modelo.Ubicacion;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevaSucursalDTO {

    @NotNull(message = "{mensaje_sucursal_nombre_vacio}")
    @NotEmpty(message = "{mensaje_sucursal_nombre_vacio}")
    private String nombre;
    private String lema;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{mensaje_sucursal_condicion_iva_vacia}")
    private CategoriaIVA categoriaIVA;
    private Long idFiscal;
    private Long ingresosBrutos;
    private LocalDateTime fechaInicioActividad;
    @Email(message = "{mensaje_correo_formato_incorrecto}")
    @NotBlank(message = "{mensaje_correo_vacio}")
    private String email;
    private String telefono;
    private UbicacionDTO ubicacion;
    private byte[] imagen;
}
