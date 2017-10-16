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
public class BusquedaProductoCriteria {

    private boolean buscarPorCodigo;
    private String codigo;
    private boolean buscarPorDescripcion;
    private String descripcion;
    private boolean buscarPorRubro;
    private Rubro rubro;
    private boolean buscarPorProveedor;
    private Proveedor proveedor;
    private Empresa empresa;
    private int cantRegistros;
    private boolean listarSoloFaltantes;
    private Pageable pageable;
}
