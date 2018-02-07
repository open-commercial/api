package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.modelo.EstadoPedido;
import sic.modelo.Factura;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"facturas", "renglones"})
@EqualsAndHashCode(of = {"nroPedido", "nombreEmpresa"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id_Pedido", scope = PedidoDTO.class)
public class PedidoDTO implements Serializable {
    
    private long id_Pedido;
    private long nroPedido;    
    private Date fecha;    
    private Date fechaVencimiento;    
    private String observaciones;    
    private String nombreEmpresa;
    private boolean eliminado;    
    private String razonSocialCliente;  
    private String nombreUsuario;        
    private List<Factura> facturas;        
    private List<RenglonPedidoDTO> renglones;
    private BigDecimal totalEstimado;
    private BigDecimal totalActual;
    private EstadoPedido estado;
}
