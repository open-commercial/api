package sic.repository.custom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import sic.modelo.BusquedaProductoCriteria;
import sic.repository.ProductoRepositoryCustom;

public class ProductoRepositoryImpl implements ProductoRepositoryCustom {
    
    @PersistenceContext
    private EntityManager em;
            
    @Override
    public double calcularValorStock(BusquedaProductoCriteria criteria) {
        String query = "SELECT SUM(p.cantidad * p.precioCosto) FROM Producto p WHERE p.empresa = :empresa "
                + "AND p.eliminado = false AND p.ilimitado = false";
        //Codigo y Descripcion
        if (criteria.isBuscarPorCodigo() == true && criteria.isBuscarPorDescripcion() == true) {
            query += " AND (p.codigo LIKE '%" + criteria.getCodigo() + "%' OR (";
            String[] terminos = criteria.getDescripcion().split(" ");
            for (int i = 0; i < terminos.length; i++) {
                query += "p.descripcion LIKE '%" + terminos[i] + "%'";
                if (i != (terminos.length - 1)) {
                    query += " AND ";
                }
            }
            query += ")) ";
        } else {
            //Codigo        
            if (criteria.isBuscarPorCodigo() == true) {
                query += " AND p.codigo LIKE '%" + criteria.getCodigo() + "%'";
            }
            //Descripcion
            if (criteria.isBuscarPorDescripcion() == true) {
                String[] terminos = criteria.getDescripcion().split(" ");
                query += " AND ";
                for (int i = 0; i < terminos.length; i++) {
                    query += "p.descripcion LIKE '%" + terminos[i] + "%'";
                    if (i != (terminos.length - 1)) {
                        query += " AND ";
                    }
                }
            }
        }
        //Rubro
        if (criteria.isBuscarPorRubro() == true) {
            query += " AND p.rubro = " + criteria.getRubro().getId_Rubro();
        }
        //Proveedor
        if (criteria.isBuscarPorProveedor()) {
            query += " AND p.proveedor = " + criteria.getProveedor().getId_Proveedor();
        }
        //Faltantes
        if (criteria.isListarSoloFaltantes() == true) {
            query += " AND p.cantidad <= p.cantMinima AND p.ilimitado = 0";
        }
        query += " ORDER BY p.descripcion ASC";
        TypedQuery<Double> typedQuery = em.createQuery(query, Double.class);
        typedQuery.setParameter("empresa", criteria.getEmpresa());
        //si es 0, recupera TODOS los registros
        if (criteria.getCantRegistros() != 0) {
            typedQuery.setMaxResults(criteria.getCantRegistros());
        }
        return (typedQuery.getSingleResult() == null) ? 0.0 : typedQuery.getSingleResult();
    }
    
}
