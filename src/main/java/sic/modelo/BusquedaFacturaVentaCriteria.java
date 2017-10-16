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
    private Cliente cliente;
    private boolean buscaPorTipoComprobante;
    private TipoDeComprobante tipoComprobante;
    private boolean buscaUsuario;
    private Usuario usuario;
    private boolean buscaViajante;
    private Usuario viajante;
    private boolean buscaPorNumeroFactura;
    private long numSerie;
    private long numFactura;
    private boolean buscarPorPedido;
    private long nroPedido;
    private boolean buscaSoloImpagas;
    private boolean buscaSoloPagadas;
    private Empresa empresa;
    private int cantRegistros;
    private Pageable pageable;
}
