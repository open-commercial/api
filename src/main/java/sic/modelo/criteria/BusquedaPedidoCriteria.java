package sic.modelo.criteria;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import sic.modelo.EstadoPedido;
import sic.modelo.TipoDeEnvio;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaPedidoCriteria {

    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private boolean buscaCliente;
    private Long idCliente;
    private boolean buscaUsuario;
    private Long idUsuario;
    private boolean buscaPorViajante;
    private Long idViajante;
    private boolean buscaPorNroPedido;
    private long nroPedido;
    private boolean buscaPorEstadoPedido;
    private EstadoPedido estadoPedido;
    private boolean buscaPorEnvio;
    private TipoDeEnvio tipoDeEnvio;
    private boolean buscaPorProducto;
    private Long idProducto;
    private Long idSucursal;
    private int cantRegistros;    
    private Pageable pageable;
}