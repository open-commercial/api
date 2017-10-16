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
public class BusquedaPedidoCriteria {

    private boolean buscaPorFecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private boolean buscaCliente;
    private Cliente cliente;
    private boolean buscaUsuario;
    private Usuario usuario;
    private boolean buscaPorNroPedido;
    private long nroPedido;
    private Empresa empresa;
    private int cantRegistros;    
    private Pageable pageable;
}