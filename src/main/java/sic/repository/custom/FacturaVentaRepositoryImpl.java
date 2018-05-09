package sic.repository.custom;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import sic.modelo.BusquedaFacturaVentaCriteria;
import sic.modelo.FacturaVenta;
import sic.modelo.TipoDeComprobante;
import sic.repository.FacturaVentaRepositoryCustom;
import sic.util.FormatterFechaHora;

@Repository
public class FacturaVentaRepositoryImpl implements FacturaVentaRepositoryCustom {
    
    @PersistenceContext
    private EntityManager em;

    @Override
    public BigDecimal calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria) {
        String query = "SELECT SUM(f.total) FROM FacturaVenta f WHERE f.empresa = :empresa AND f.eliminada = false";
        //Fecha
        if (criteria.isBuscaPorFecha()) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            query += " AND f.fecha BETWEEN '" + formateadorFecha.format(criteria.getFechaDesde()) + "' AND '" + formateadorFecha.format(criteria.getFechaHasta()) + "'";
        }
        //Cliente
        if (criteria.isBuscaCliente()) {
            query += " AND f.cliente = " + criteria.getCliente().getId_Cliente();
        }
        //Tipo de Factura
        if (criteria.isBuscaPorTipoComprobante()) {
            query += " AND f.tipoComprobante = " + "\'" + criteria.getTipoComprobante() + "\'";
        }
        //Usuario
        if (criteria.isBuscaUsuario()) {
            query += " AND f.usuario = " + criteria.getUsuario().getId_Usuario();
        }
        if (criteria.isBuscaViajante()) {
            query += " AND f.cliente.viajante = " + criteria.getViajante().getId_Usuario();
        }
        //Nro de Factura
        if (criteria.isBuscaPorNumeroFactura()) {
            query += " AND f.numSerie = " + criteria.getNumSerie() + " AND f.numFactura = " + criteria.getNumFactura();
        }
        //Pedido
        if (criteria.isBuscarPorPedido()) {
            query += " AND f.pedido.nroPedido = " + criteria.getNroPedido();
        }
        query += " ORDER BY f.fecha DESC";
        TypedQuery<BigDecimal> typedQuery = em.createQuery(query, BigDecimal.class);
        typedQuery.setParameter("empresa", criteria.getEmpresa());
        //si es 0, recupera TODOS los registros
        if (criteria.getCantRegistros() != 0) {
            typedQuery.setMaxResults(criteria.getCantRegistros());
        }
        return (typedQuery.getSingleResult() == null) ? BigDecimal.ZERO : typedQuery.getSingleResult();
    }

    @Override
    public BigDecimal calcularIVAVenta(BusquedaFacturaVentaCriteria criteria, TipoDeComprobante[] tipoComprobante) {
        String query = "SELECT SUM(f.iva_105_neto + f.iva_21_neto) FROM FacturaVenta f WHERE f.empresa = :empresa AND f.eliminada = false";
        //Fecha
        if (criteria.isBuscaPorFecha()) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            query += " AND f.fecha BETWEEN '" + formateadorFecha.format(criteria.getFechaDesde())
                    + "' AND '" + formateadorFecha.format(criteria.getFechaHasta()) + "'";
        }
        //Cliente
        if (criteria.isBuscaCliente()) {
            query += " AND f.cliente = " + criteria.getCliente().getId_Cliente();
        }
        //Tipo de Factura
        if (criteria.isBuscaPorTipoComprobante()) {
            query += " AND f.tipoComprobante = " + "\'" + criteria.getTipoComprobante() + "\'";
        }
        for (int i = 0; i < tipoComprobante.length; i++) {
            if (i == 0) {
                query += " AND ( f.tipoComprobante = \'" + tipoComprobante[i] + "\'";
            } else {
                query += " OR f.tipoComprobante = \'" + tipoComprobante[i] + "\'";
            }
        }
        query += " )";
        //Usuario
        if (criteria.isBuscaUsuario()) {
            query += " AND f.usuario = " + criteria.getUsuario().getId_Usuario();
        }
        if (criteria.isBuscaViajante()) {
            query += " AND f.cliente.viajante = " + criteria.getViajante().getId_Usuario();
        }
        //Nro de Factura
        if (criteria.isBuscaPorNumeroFactura()) {
            query += " AND f.numSerie = " + criteria.getNumSerie() + " AND f.numFactura = " + criteria.getNumFactura();
        }
        //Pedido
        if (criteria.isBuscarPorPedido()) {
            query += " AND f.pedido.nroPedido = " + criteria.getNroPedido();
        }
        query += " ORDER BY f.fecha DESC";
        TypedQuery<BigDecimal> typedQuery = em.createQuery(query, BigDecimal.class);
        typedQuery.setParameter("empresa", criteria.getEmpresa());
        //si es 0, recupera TODOS los registros
        if (criteria.getCantRegistros() != 0) {
            typedQuery.setMaxResults(criteria.getCantRegistros());
        }
        return (typedQuery.getSingleResult() == null) ? BigDecimal.ZERO : typedQuery.getSingleResult();
    }

    @Override
    public BigDecimal calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria) {
        String query = "SELECT SUM(r.ganancia_neto * r.cantidad) FROM FacturaVenta f LEFT JOIN f.renglones r WHERE f.empresa = :empresa AND f.eliminada = false";
        //Fecha
        if (criteria.isBuscaPorFecha()) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            query += " AND f.fecha BETWEEN '" + formateadorFecha.format(criteria.getFechaDesde())
                    + "' AND '" + formateadorFecha.format(criteria.getFechaHasta()) + "'";
        }
        //Cliente
        if (criteria.isBuscaCliente()) {
            query += " AND f.cliente = " + criteria.getCliente().getId_Cliente();
        }
        //Tipo de Factura
        if (criteria.isBuscaPorTipoComprobante()) {
            query += " AND f.tipoComprobante = " + "\'" + criteria.getTipoComprobante() + "\'";
        }
        //Usuario
        if (criteria.isBuscaUsuario()) {
            query += " AND f.usuario = " + criteria.getUsuario().getId_Usuario();
        }
        if (criteria.isBuscaViajante()) {
            query += " AND f.cliente.viajante = " + criteria.getViajante().getId_Usuario();
        }
        //Nro de Factura
        if (criteria.isBuscaPorNumeroFactura()) {
            query += " AND f.numSerie = " + criteria.getNumSerie() + " AND f.numFactura = " + criteria.getNumFactura();
        }
        //Pedido
        if (criteria.isBuscarPorPedido()) {
            query += " AND f.pedido.nroPedido = " + criteria.getNroPedido();
        }
        query += " ORDER BY f.fecha DESC";
        TypedQuery<BigDecimal> typedQuery = em.createQuery(query, BigDecimal.class);
        typedQuery.setParameter("empresa", criteria.getEmpresa());
        //si es 0, recupera TODOS los registros
        if (criteria.getCantRegistros() != 0) {
            typedQuery.setMaxResults(criteria.getCantRegistros());
        }
        return (typedQuery.getSingleResult() == null) ? BigDecimal.ZERO : typedQuery.getSingleResult();
    }

    @Override
    public Page<FacturaVenta> buscarFacturasVenta(BusquedaFacturaVentaCriteria criteria) {
        String queryCount = "SELECT COUNT(f)";
        String queryData = "SELECT f";
        String query = " FROM FacturaVenta f WHERE f.empresa = :empresa AND f.eliminada = false";
        //Fecha
        if (criteria.isBuscaPorFecha()) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            query += " AND f.fecha BETWEEN '" + formateadorFecha.format(criteria.getFechaDesde())
                    + "' AND '" + formateadorFecha.format(criteria.getFechaHasta()) + "'";
        }
        //Cliente
        if (criteria.isBuscaCliente()) {
            query += " AND f.cliente = " + criteria.getCliente().getId_Cliente();
        }
        //Tipo de Factura
        if (criteria.isBuscaPorTipoComprobante()) {
            query += " AND f.tipoComprobante = " + "\'" + criteria.getTipoComprobante() + "\'";
        }
        //Usuario
        if (criteria.isBuscaUsuario()) {
            query += " AND f.usuario = " + criteria.getUsuario().getId_Usuario();
        }
        if (criteria.isBuscaViajante()) {
            query += " AND f.cliente.viajante = " + criteria.getViajante().getId_Usuario();
        }
        //Nro de Factura
        if (criteria.isBuscaPorNumeroFactura()) {
            query += " AND f.numSerie = " + criteria.getNumSerie() + " AND f.numFactura = " + criteria.getNumFactura();
        }
        //Pedido
        if (criteria.isBuscarPorPedido()) {
            query += " AND f.pedido.nroPedido = " + criteria.getNroPedido();
        }     
        queryCount += query;
        if (criteria.getPageable().getSort().getOrderFor("fecha").getDirection() == Sort.Direction.ASC) {//name().equals("ASC")) {
            queryData += query + " ORDER BY f.fecha ASC";
        } else {
            queryData += query + " ORDER BY f.fecha DESC";
        }  
        TypedQuery<FacturaVenta> typedQueryData = em.createQuery(queryData, FacturaVenta.class);        
        typedQueryData.setParameter("empresa", criteria.getEmpresa());
        typedQueryData.setFirstResult(criteria.getPageable().getOffset());
        typedQueryData.setMaxResults(criteria.getPageable().getPageSize());
        List<FacturaVenta> facturas = typedQueryData.getResultList();        
        TypedQuery<Long> typedQueryCount = em.createQuery(queryCount, Long.class);
        typedQueryCount.setParameter("empresa", criteria.getEmpresa());
        long total = typedQueryCount.getSingleResult();
        return new PageImpl<>(facturas, criteria.getPageable(), total);
    }
}
