package sic.modelo;

/**
 * Describe los distintos estados de un Pedido.
 */
public enum EstadoPedido {

    /**
     * Para el caso en que el pedido no haya pasado por ningún proceso de facturación
     */
    ABIERTO,
    /**
     * Para el caso en que el pedido fue facturado parcialmente.
     */
    ACTIVO,
    /**
     * Para el caso en que el pedido fue facturado totalmente.
     */
    CERRADO;
}
