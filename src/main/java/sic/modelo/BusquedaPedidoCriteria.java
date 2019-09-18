package sic.modelo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaPedidoCriteria {

    private Date fechaDesde;
    private Date fechaHasta;
    private Long idCliente;
    private Long idUsuario;
    private Long idViajante;
    private Long nroPedido;
    private EstadoPedido estadoPedido;
    private TipoDeEnvio tipoDeEnvio;
    private Long idProducto;
    private Long idEmpresa;
    private Integer pagina;
    private String ordenarPor;
    private String sentido;
}