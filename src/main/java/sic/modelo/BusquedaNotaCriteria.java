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
public class BusquedaNotaCriteria {
    
    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private Long idEmpresa;
    private int cantidadDeRegistros;
    private boolean buscaPorNumeroNota;
    private long numSerie;
    private long numNota;
    private boolean buscaPorTipoComprobante;
    private TipoDeComprobante tipoComprobante;
    private boolean buscaVentas;
    private boolean buscaCompras;
    private boolean buscaUsuario;
    private Long idUsuario;
    private boolean buscaProveedor;
    private Long idProveedor;
    private boolean buscaCliente;
    private Long idCliente;
    private Pageable pageable;
    
}
