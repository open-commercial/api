package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sic.modelo.dto.ClienteDTO;

@Entity
@Table(name = "facturaventa")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties({"clienteDTO", "usuario", "empresa", "pedido", "transportista"})
public class FacturaVenta extends Factura implements Serializable {

    @Embedded
    private ClienteDTO clienteDTO;

    public FacturaVenta() {}

    public FacturaVenta(ClienteDTO clienteDTO, Usuario usuario, long id_Factura, Date fecha,
            TipoDeComprobante tipoComprobante, long numSerie, long numFactura, Date fechaVencimiento,
            Pedido pedido, Transportista transportista, List<RenglonFactura> renglones, BigDecimal subTotal,
            BigDecimal recargoPorcentaje, BigDecimal recargoNeto, BigDecimal descuentoPorcentaje,
            BigDecimal descuentoNeto, BigDecimal subTotalNeto, BigDecimal iva105Neto, BigDecimal iva21Neto,
            BigDecimal impuestoInternoNeto, BigDecimal total, String observaciones, Empresa empresa,
            boolean eliminada, long CAE, Date vencimientoCAE, long numSerieAfip, long numFacturaAfip) {
        
        super(id_Factura, usuario, fecha, tipoComprobante, numSerie, numFactura, fechaVencimiento,
                pedido, transportista, renglones, subTotal, recargoPorcentaje,
                recargoNeto, descuentoPorcentaje, descuentoNeto, subTotalNeto,
                iva105Neto, iva21Neto, impuestoInternoNeto, total, observaciones,
                empresa, eliminada, CAE, vencimientoCAE, numSerieAfip, numFacturaAfip);
        this.clienteDTO = clienteDTO;
    }

    @JsonGetter("idCliente")
    public Long getIdCliente() {
        return clienteDTO.getId_Cliente();
    }

    @JsonGetter("nombreFiscalCliente")
    public String getNombreFiscalCliente() {
        return clienteDTO.getNombreFiscal();
    }

    @JsonGetter("idViajante")
    public Long getIdViajante() {
        return clienteDTO.getIdViajante();
    }

    @JsonGetter("nombreViajante")
    public String getNombreViajante() {
        return clienteDTO.getNombreViajante();
    }

}
