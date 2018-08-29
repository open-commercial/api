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
import sic.modelo.BusquedaFacturaCompraCriteria;
import sic.modelo.FacturaCompra;
import sic.modelo.TipoDeComprobante;
import sic.repository.FacturaCompraRepositoryCustom;
import sic.util.FormatterFechaHora;

@Repository
public class FacturaCompraRepositoryImpl implements FacturaCompraRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria) {
        String query = "SELECT SUM(f.total) FROM FacturaCompra f WHERE f.empresa.id_Empresa = :idEmpresa AND f.eliminada = false";
        //Fecha Factura
        if (criteria.isBuscaPorFecha()) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            query += " AND f.fecha BETWEEN '" + formateadorFecha.format(criteria.getFechaDesde()) + "' AND '" + formateadorFecha.format(criteria.getFechaHasta()) + "'";
        }
        //Proveedor
        if (criteria.isBuscaPorProveedor()) {
            query += " AND f.proveedor = " + criteria.getIdProveedor();
        }
        //Tipo de Factura
        if (criteria.isBuscaPorTipoComprobante()) {
            query += " AND f.tipoComprobante = " + "\'" + criteria.getTipoComprobante() + "\'";
        }
        //Nro de Factura
        if (criteria.isBuscaPorNumeroFactura()) {
            query += " AND f.numSerie = " + criteria.getNumSerie() + " AND f.numFactura = " + criteria.getNumFactura();
        }
        query += " ORDER BY f.fecha DESC";
        TypedQuery<BigDecimal> typedQuery = em.createQuery(query, BigDecimal.class);
        typedQuery.setParameter("idEmpresa", criteria.getIdEmpresa());
        //si es 0, recupera TODOS los registros
        if (criteria.getCantRegistros() != 0) {
            typedQuery.setMaxResults(criteria.getCantRegistros());
        }
        return (typedQuery.getSingleResult() == null) ? BigDecimal.ZERO : typedQuery.getSingleResult();
    }

    @Override
    public BigDecimal calcularIVACompra(BusquedaFacturaCompraCriteria criteria, TipoDeComprobante[] tipoComprobante) {
        String query = "SELECT SUM(f.iva_105_neto + f.iva_21_neto) FROM FacturaCompra f WHERE f.empresa.id_Empresa = :idEmpresa AND f.eliminada = false";
        //Fecha Factura
        if (criteria.isBuscaPorFecha()) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            query += " AND f.fecha BETWEEN '" + formateadorFecha.format(criteria.getFechaDesde())
                    + "' AND '" + formateadorFecha.format(criteria.getFechaHasta()) + "'";
        }
        //Proveedor
        if (criteria.isBuscaPorProveedor()) {
            query += " AND f.proveedor = " + criteria.getIdProveedor();
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
        //Nro de Factura
        if (criteria.isBuscaPorNumeroFactura()) {
            query += " AND f.numSerie = " + criteria.getNumSerie() + " AND f.numFactura = " + criteria.getNumFactura();
        }
        query += " ORDER BY f.fecha DESC";
        TypedQuery<BigDecimal> typedQuery = em.createQuery(query, BigDecimal.class);
        typedQuery.setParameter("idEmpresa", criteria.getIdEmpresa());
        //si es 0, recupera TODOS los registros
        if (criteria.getCantRegistros() != 0) {
            typedQuery.setMaxResults(criteria.getCantRegistros());
        }
        return (typedQuery.getSingleResult() == null) ? BigDecimal.ZERO : typedQuery.getSingleResult();
    }

  @Override
  public Page<FacturaCompra> buscarFacturasCompra(BusquedaFacturaCompraCriteria criteria) {
    String queryCount = "SELECT COUNT(f)";
    String queryData = "SELECT f ";
    String query =
        "FROM FacturaCompra f WHERE f.empresa.id_Empresa = :idEmpresa AND f.eliminada = false";
    // Fecha Factura
    if (criteria.isBuscaPorFecha()) {
      FormatterFechaHora formateadorFecha =
          new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
      query +=
          " AND f.fecha BETWEEN '"
              + formateadorFecha.format(criteria.getFechaDesde())
              + "' AND '"
              + formateadorFecha.format(criteria.getFechaHasta())
              + "'";
    }
    // Proveedor
    if (criteria.isBuscaPorProveedor()) {
      query += " AND f.proveedor = " + criteria.getIdProveedor();
    }
    // Tipo de Factura
    if (criteria.isBuscaPorTipoComprobante()) {
      query += " AND f.tipoComprobante = " + "\'" + criteria.getTipoComprobante() + "\'";
    }
    // Nro de Factura
    if (criteria.isBuscaPorNumeroFactura()) {
      query +=
          " AND f.numSerie = "
              + criteria.getNumSerie()
              + " AND f.numFactura = "
              + criteria.getNumFactura();
    }
    queryCount += query;
    if (criteria.getPageable().getSort().getOrderFor("fecha") != null) {
      if (criteria.getPageable().getSort().getOrderFor("fecha").getDirection()
          == Sort.Direction.ASC) {
        queryData += query + " ORDER BY f.fecha ASC";
      } else {
        queryData += query + " ORDER BY f.fecha DESC";
      }
    } else if (criteria.getPageable().getSort().getOrderFor("total") != null){
        if (criteria.getPageable().getSort().getOrderFor("total").getDirection()
                == Sort.Direction.ASC) {
            queryData += query + " ORDER BY f.total ASC";
        } else {
            queryData += query + " ORDER BY f.total DESC";
        }
    }
    TypedQuery<FacturaCompra> typedQueryData = em.createQuery(queryData, FacturaCompra.class);
    typedQueryData.setParameter("idEmpresa", criteria.getIdEmpresa());
    typedQueryData.setFirstResult(criteria.getPageable().getOffset());
    typedQueryData.setMaxResults(criteria.getPageable().getPageSize());
    List<FacturaCompra> facturas = typedQueryData.getResultList();
    TypedQuery<Long> typedQueryCount = em.createQuery(queryCount, Long.class);
    typedQueryCount.setParameter("idEmpresa", criteria.getIdEmpresa());
    long total = typedQueryCount.getSingleResult();
    return new PageImpl<>(facturas, criteria.getPageable(), total);
  }
}
