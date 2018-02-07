package sic.builder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import sic.modelo.EstadoPedido;
import sic.modelo.Factura;
import sic.modelo.dto.PedidoDTO;
import sic.modelo.dto.RenglonPedidoDTO;

public class PedidoBuilder {

    private long id_Pedido = 0L;
    private long nroPedido = 46L;
    private Date fecha = new Date();
    private Date fechaVencimiento;
    private String observaciones = "Los precios se encuentran sujetos a modificaciones.";
    private String nombreEmpresa = "Globo Corporation";
    private boolean eliminado = false;
    private String razonSocialCliente = "Construcciones S.A.";
    private String nombreUsuario = "Daenerys Targaryen";
    private List<Factura> facturas;
    private List<RenglonPedidoDTO> renglones;
    private BigDecimal totalEstimado = new BigDecimal(544.5);
    private BigDecimal totalActual = new BigDecimal(544.5);
    private EstadoPedido estado = EstadoPedido.ABIERTO;

    public PedidoDTO build() {
        if (renglones == null) {
            RenglonPedidoDTO renglon1 = new RenglonPedidoBuilder().build();
            RenglonPedidoDTO renglon2 = new RenglonPedidoBuilder()
                                            .withCantidad(BigDecimal.ONE)
                                            .withIdRenglonPedido(90L)
                                            .withProducto(new ProductoBuilder()
                                                .withId_Producto(77L)
                                                .withDescripcion("Pack 6 Vasos")
                                                .build())
                                            .build();
            List<RenglonPedidoDTO> renglonesPedido = new ArrayList<>();
            renglonesPedido.add(renglon1);
            renglonesPedido.add(renglon2);
            this.renglones = renglonesPedido;
        }
        return new PedidoDTO(id_Pedido, nroPedido, fecha, fechaVencimiento, observaciones, nombreEmpresa,
                eliminado, razonSocialCliente, nombreUsuario, facturas, renglones, totalEstimado, totalActual, estado);
    }

    public PedidoBuilder withIdPedido(long idPedido) {
        this.id_Pedido = idPedido;
        return this;
    }

    public PedidoBuilder withNroPedido(long nroPedido) {
        this.nroPedido = nroPedido;
        return this;
    }

    public PedidoBuilder withFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public PedidoBuilder withFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
        return this;
    }

    public PedidoBuilder withObservaciones(String observaciones) {
        this.observaciones = observaciones;
        return this;
    }

    public PedidoBuilder withNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
        return this;
    }

    public PedidoBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }

    public PedidoBuilder withRazonSocialCliente(String razonSocialCliente) {
        this.razonSocialCliente = razonSocialCliente;
        return this;
    }

    public PedidoBuilder withNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        return this;
    }

    public PedidoBuilder withFacturas(List<Factura> facturas) {
        this.facturas = facturas;
        return this;
    }

    public PedidoBuilder withRenglones(List<RenglonPedidoDTO> renglones) {
        this.renglones = renglones;
        return this;
    }

    public PedidoBuilder withTotalEstimado(BigDecimal totalEstimado) {
        this.totalEstimado = totalEstimado;
        return this;
    }

    public PedidoBuilder withTotalActual(BigDecimal totalActual) {
        this.totalActual = totalActual;
        return this;
    }

    public PedidoBuilder withEstado(EstadoPedido estadoPedido) {
        this.estado = estadoPedido;
        return this;
    }

}
