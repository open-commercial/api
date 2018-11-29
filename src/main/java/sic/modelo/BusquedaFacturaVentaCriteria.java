package sic.modelo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusquedaFacturaVentaCriteria {

    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private boolean buscaCliente;
    private Long idCliente;
    private boolean buscaPorTipoComprobante;
    private TipoDeComprobante tipoComprobante;
    private boolean buscaUsuario;
    private Long idUsuario;
    private boolean buscaViajante;
    private Long idViajante;
    private boolean buscaPorNumeroFactura;
    private long numSerie;
    private long numFactura;
    private boolean buscarPorPedido;
    private long nroPedido;
    private boolean buscaPoridProducto;
    private Long idProducto;
    private Long idEmpresa;
    private int cantRegistros;
    private Pageable pageable;
}
