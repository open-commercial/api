package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import sic.modelo.BusquedaProductoCriteria;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Empresa;
import sic.modelo.Medida;
import sic.modelo.Movimiento;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.QProducto;
import sic.modelo.Rubro;
import sic.service.IProductoService;
import sic.service.BusinessServiceException;
import sic.service.ServiceException;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.ProductoRepository;

@Service
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final static BigDecimal CIEN = new BigDecimal("100");

    @Autowired
    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    private void validarOperacion(TipoDeOperacion operacion, Producto producto) {
        //Duplicados
        //Codigo
        if (!producto.getCodigo().equals("")) {
            Producto productoDuplicado = this.getProductoPorCodigo(producto.getCodigo(), producto.getEmpresa());
            if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
                    && productoDuplicado != null
                    && productoDuplicado.getId_Producto() != producto.getId_Producto()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_producto_duplicado_codigo"));
            }
            if (operacion.equals(TipoDeOperacion.ALTA)
                    && productoDuplicado != null
                    && !producto.getCodigo().equals("")) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_producto_duplicado_codigo"));
            }
        }
        //Descripcion
        Producto productoDuplicado = this.getProductoPorDescripcion(producto.getDescripcion(), producto.getEmpresa());
        if (operacion.equals(TipoDeOperacion.ALTA) && productoDuplicado != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_duplicado_descripcion"));
        }
        if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (productoDuplicado != null && productoDuplicado.getId_Producto() != producto.getId_Producto()) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_producto_duplicado_descripcion"));
            }
        }
        //Calculos
        Double[] IVAs = {10.5, 21.0, 0.0};
        if (!Arrays.asList(IVAs).contains(producto.getIva_porcentaje().doubleValue())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_ganancia_neta_incorrecta"));
        }
        if (producto.getGanancia_neto().setScale(3, RoundingMode.DOWN)
                .compareTo(this.calcularGananciaNeto(producto.getPrecioCosto(), producto.getGanancia_porcentaje())
                        .setScale(3, RoundingMode.DOWN)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_ganancia_neta_incorrecta"));
        }
        if (producto.getPrecioVentaPublico().setScale(3, RoundingMode.DOWN)
                .compareTo(this.calcularPVP(producto.getPrecioCosto(), producto.getGanancia_porcentaje())
                        .setScale(3, RoundingMode.DOWN)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_precio_venta_publico_incorrecto"));
        }
        if (producto.getImpuestoInterno_neto().setScale(3, RoundingMode.DOWN)
                .compareTo(this.calcularImpInternoNeto(producto.getPrecioVentaPublico(), producto.getImpuestoInterno_porcentaje())
                        .setScale(3, RoundingMode.DOWN)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_impuesto_interno_neto_incorrecto"));
        }
        if (producto.getIva_neto().setScale(3, RoundingMode.DOWN)
                .compareTo(this.calcularIVANeto(producto.getPrecioVentaPublico(), producto.getIva_porcentaje())
                        .setScale(3, RoundingMode.DOWN)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_iva_neto_incorrecto"));
        }
        if (producto.getPrecioLista().setScale(3, RoundingMode.DOWN)
                .compareTo(this.calcularPrecioLista(producto.getPrecioVentaPublico(), producto.getIva_porcentaje(), producto.getImpuestoInterno_porcentaje())
                        .setScale(3, RoundingMode.DOWN)) != 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_precio_lista_incorrecto"));
        }
    }

    @Override
    public Page<Producto> buscarProductos(BusquedaProductoCriteria criteria) {
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Rubro
        if (criteria.isBuscarPorRubro() && criteria.getRubro() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_vacio_rubro"));
        }
        //Proveedor
        if (criteria.isBuscarPorProveedor() && criteria.getProveedor() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_vacio_proveedor"));
        }
        QProducto qproducto = QProducto.producto;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qproducto.empresa.eq(criteria.getEmpresa()).and(qproducto.eliminado.eq(false)));
        if (criteria.isBuscarPorCodigo() && criteria.isBuscarPorDescripcion()) {
            builder.and(qproducto.codigo.containsIgnoreCase(criteria.getCodigo())
                    .or(this.buildPredicadoDescripcion(criteria.getDescripcion(), qproducto)));
        } else {
            if (criteria.isBuscarPorCodigo()) {
                builder.and(qproducto.codigo.containsIgnoreCase(criteria.getCodigo()));
            }
            if (criteria.isBuscarPorDescripcion()) {
                builder.and(this.buildPredicadoDescripcion(criteria.getDescripcion(), qproducto));
            }
        }
        if (criteria.isBuscarPorRubro()) {
            builder.and(qproducto.rubro.eq(criteria.getRubro()));
        }
        if (criteria.isBuscarPorProveedor()) {
            builder.and(qproducto.proveedor.eq(criteria.getProveedor()));
        }
        if (criteria.isListarSoloFaltantes()) {
            builder.and(qproducto.cantidad.loe(qproducto.cantMinima)).and(qproducto.ilimitado.eq(false));
        }
        int pageNumber = 0;
        int pageSize = Integer.MAX_VALUE;
        if (criteria.getPageable() != null) {
            pageNumber = criteria.getPageable().getPageNumber();
            pageSize = criteria.getPageable().getPageSize();
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, new Sort(Sort.Direction.ASC, "descripcion"));
        return productoRepository.findAll(builder, pageable);
    }

    private BooleanBuilder buildPredicadoDescripcion(String descripcion, QProducto qproducto) {
        String[] terminos = descripcion.split(" ");
        BooleanBuilder descripcionProducto = new BooleanBuilder();
        for (String termino : terminos) {
            descripcionProducto.and(qproducto.descripcion.containsIgnoreCase(termino));
        }
        return descripcionProducto;
    }

    @Override
    @Transactional
    public Producto guardar(Producto producto) {
        if (producto.getCodigo() == null) {
            producto.setCodigo("");
        }
        this.validarOperacion(TipoDeOperacion.ALTA, producto);
        producto.setFechaAlta(new Date());
        producto.setFechaUltimaModificacion(new Date());
        producto = productoRepository.save(producto);
        LOGGER.warn("El Producto " + producto + " se guardó correctamente.");
        return producto;
    }

    @Override
    @Transactional
    public void actualizar(Producto producto) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, producto);
        producto.setFechaUltimaModificacion(new Date());
        productoRepository.save(producto);
        LOGGER.warn("El Producto " + producto + " se modificó correctamente.");
    }

    @Override
    public void actualizarStock(HashMap<Long, BigDecimal> idsYCantidades, TipoDeOperacion operacion, Movimiento movimiento) {
        idsYCantidades.entrySet().forEach(entry -> {
            Producto producto = productoRepository.findById(entry.getKey());
            if (producto == null) {
                LOGGER.warn("Se intenta actualizar el stock de un producto eliminado.");
            }
            if (producto != null && !producto.isIlimitado()) {
                if (movimiento.equals(Movimiento.VENTA)) {
                    if (operacion == TipoDeOperacion.ALTA) {
                        producto.setCantidad(producto.getCantidad().subtract(entry.getValue()));
                    }

                    if (operacion == TipoDeOperacion.ELIMINACION || operacion == TipoDeOperacion.ACTUALIZACION) {
                        producto.setCantidad(producto.getCantidad().add(entry.getValue()));
                    }
                } else if (movimiento.equals(Movimiento.COMPRA)) {
                    if (operacion == TipoDeOperacion.ALTA) {
                        producto.setCantidad(producto.getCantidad().add(entry.getValue()));
                    }

                    if (operacion == TipoDeOperacion.ELIMINACION) {
                        BigDecimal result = producto.getCantidad().subtract(entry.getValue());
                        if (result.compareTo(BigDecimal.ZERO) < 0) {
                            result = BigDecimal.ZERO;
                        }
                        producto.setCantidad(result);
                    }
                }
                productoRepository.save(producto);
            }
        });
    }

    @Override
    @Transactional
    public void eliminarMultiplesProductos(long[] idProducto) {
        if (Validator.tieneDuplicados(idProducto)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_ids_duplicados"));
        }
        List<Producto> productos = new ArrayList<>();
        for (Long i : idProducto) {
            Producto producto = this.getProductoPorId(i);
            if (producto == null) {
                throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_producto_no_existente"));
            }
            producto.setEliminado(true);
            productos.add(producto);
        }
        productoRepository.save(productos);
    }

    @Override
    @Transactional
    public List<Producto> actualizarMultiples(long[] idProducto,
                                              boolean checkPrecios,
                                              BigDecimal gananciaNeto,
                                              BigDecimal gananciaPorcentaje,
                                              BigDecimal impuestoInternoNeto,
                                              BigDecimal impuestoInternoPorcentaje,
                                              BigDecimal IVANeto,
                                              BigDecimal IVAPorcentaje,
                                              BigDecimal precioCosto,
                                              BigDecimal precioLista,
                                              BigDecimal precioVentaPublico,
                                              boolean checkMedida, Medida medida,
                                              boolean checkRubro, Rubro rubro,
                                              boolean checkProveedor, Proveedor proveedor) {
        //Requeridos
        if (checkMedida && medida == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_vacio_medida"));
        }
        if (checkRubro && rubro == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_vacio_rubro"));
        }
        if (checkProveedor && proveedor == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_vacio_proveedor"));
        }
        if (Validator.tieneDuplicados(idProducto)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_ids_duplicados"));
        }
        List<Producto> productos = new ArrayList<>();
        for (long i : idProducto) {
            productos.add(this.getProductoPorId(i));
        }
        productos.forEach(p -> {
            if (checkMedida) p.setMedida(medida);
            if (checkRubro) p.setRubro(rubro);
            if (checkProveedor) p.setProveedor(proveedor);
            if (checkPrecios) {
                p.setPrecioCosto(precioCosto);
                p.setGanancia_porcentaje(gananciaPorcentaje);
                p.setGanancia_neto(gananciaNeto);
                p.setPrecioVentaPublico(precioVentaPublico);
                p.setIva_porcentaje(IVAPorcentaje);
                p.setIva_neto(IVANeto);
                p.setImpuestoInterno_porcentaje(impuestoInternoPorcentaje);
                p.setImpuestoInterno_neto(impuestoInternoNeto);
                p.setPrecioLista(precioLista);
            }
            if (checkMedida || checkRubro || checkProveedor || checkPrecios) {
                p.setFechaUltimaModificacion(new Date());                
            }
            this.validarOperacion(TipoDeOperacion.ACTUALIZACION, p);
        });
        productoRepository.save(productos);
        LOGGER.warn("Los Productos " + productos + " se modificaron correctamente.");
        return productos;
    }

    @Override
    public Producto getProductoPorId(long idProducto) {
        Producto producto = productoRepository.findOne(idProducto);
        if (producto == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_producto_no_existente"));
        }
        return producto;
    }

    @Override
    public Producto getProductoPorCodigo(String codigo, Empresa empresa) {
        if (codigo.isEmpty()|| empresa == null) {
            return null;
        } else {
            return productoRepository.findByCodigoAndEmpresaAndEliminado(codigo, empresa, false);
        }
    }

    @Override
    public Producto getProductoPorDescripcion(String descripcion, Empresa empresa) {
        return productoRepository.findByDescripcionAndEmpresaAndEliminado(descripcion, empresa, false);
    }

    @Override
    public BigDecimal calcularValorStock(BusquedaProductoCriteria criteria) {
        return productoRepository.calcularValorStock(criteria);
    }

    @Override
    public Map<Long, BigDecimal> getProductosSinStockDisponible(long[] idProducto, BigDecimal[] cantidad) {
        Map<Long, BigDecimal> productos = new HashMap<>();
        int longitudIds = idProducto.length;
        int longitudCantidades = cantidad.length;
        if (longitudIds == longitudCantidades) {
            for (int i = 0; i < longitudIds; i++) {
                Producto p = this.getProductoPorId(idProducto[i]);
                if (!p.isIlimitado() && p.getCantidad().compareTo(cantidad[i]) < 0) {
                    productos.put(p.getId_Producto(), cantidad[i]);
                }
            }
        } else {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_logitudes_arrays"));
        }
        return productos;
    }

    @Override
    public Map<Long, BigDecimal> getProductosNoCumplenCantidadVentaMinima(long[] idProducto, BigDecimal[] cantidad) {
        Map<Long, BigDecimal> productos = new HashMap<>();
        int longitudIds = idProducto.length;
        int longitudCantidades = cantidad.length;
        if (longitudIds == longitudCantidades) {
            for (int i = 0; i < longitudIds; i++) {
                Producto p = this.getProductoPorId(idProducto[i]);
                if (p.getVentaMinima().compareTo(cantidad[i]) > 0) {
                    productos.put(p.getId_Producto(), cantidad[i]);
                }
            }
        } else {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_logitudes_arrays"));
        }
        return productos;
    }

    @Override
    public BigDecimal calcularGananciaPorcentaje(BigDecimal precioDeListaNuevo,
                                                 BigDecimal precioDeListaAnterior, BigDecimal pvp, BigDecimal ivaPorcentaje,
                                                 BigDecimal impInternoPorcentaje, BigDecimal precioCosto, boolean ascendente) {
        //evita la division por cero
        if (precioCosto.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal resultado;
        if (!ascendente) {
            resultado = pvp.subtract(precioCosto).divide(precioCosto, 15, RoundingMode.HALF_UP).multiply(CIEN);
        } else if (precioDeListaAnterior.compareTo(BigDecimal.ZERO) == 0 || precioCosto.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            resultado = precioDeListaNuevo;
            BigDecimal porcentajeIncremento = precioDeListaNuevo.divide(precioDeListaAnterior, 15, RoundingMode.HALF_UP);
            resultado = resultado.subtract(porcentajeIncremento.multiply(impInternoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP).multiply(pvp)));
            resultado = resultado.subtract(porcentajeIncremento.multiply(ivaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP).multiply(pvp)));
            resultado = resultado.subtract(precioCosto).multiply(CIEN).divide(precioCosto, 15, RoundingMode.HALF_UP);
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularGananciaNeto(BigDecimal precioCosto, BigDecimal gananciaPorcentaje) {
        return precioCosto.multiply(gananciaPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularPVP(BigDecimal precioCosto, BigDecimal gananciaPorcentaje) {
        return precioCosto.add(precioCosto.multiply(gananciaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)));
    }

    @Override
    public BigDecimal calcularIVANeto(BigDecimal pvp, BigDecimal ivaPorcentaje) {
        return pvp.multiply(ivaPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularImpInternoNeto(BigDecimal pvp, BigDecimal impInternoPorcentaje) {
        return pvp.multiply(impInternoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularPrecioLista(BigDecimal PVP, BigDecimal ivaPorcentaje, BigDecimal impInternoPorcentaje) {
        BigDecimal resulIVA = PVP.multiply(ivaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP));
        BigDecimal resultImpInterno = PVP.multiply(impInternoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP));
        return PVP.add(resulIVA).add(resultImpInterno);
    }

    @Override
    public byte[] getListaDePreciosPorEmpresa(List<Producto> productos, Empresa empresa, String formato) {
        ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
        InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/ListaPreciosProductos.jasper");
        Map<String, Object> params = new HashMap<>();
        params.put("empresa", empresa);
        if (!empresa.getLogo().isEmpty()) {
            try {
                params.put("logo", new ImageIcon(ImageIO.read(new URL(empresa.getLogo()))).getImage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_empresa_404_logo"), ex);
            }
        }
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(productos);
        switch (formato) {
            case "xlsx":
                try {
                    return xlsReportToArray(JasperFillManager.fillReport(isFileReport, params, ds));
                } catch (JRException ex) {
                    LOGGER.error(ex.getMessage());
                    throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_error_reporte"), ex);
                }
            case "pdf":
                try {
                    return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(isFileReport, params, ds));
                } catch (JRException ex) {
                    LOGGER.error(ex.getMessage());
                    throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                            .getString("mensaje_error_reporte"), ex);
                }
            default:
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_formato_no_valido"));
        }
    }

    private byte[] xlsReportToArray(JasperPrint jasperPrint) {
        byte[] bytes = null;
        try {
            JRXlsxExporter jasperXlsxExportMgr = new JRXlsxExporter();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SimpleOutputStreamExporterOutput simpleOutputStreamExporterOutput = new SimpleOutputStreamExporterOutput(out);
            jasperXlsxExportMgr.setExporterInput(new SimpleExporterInput(jasperPrint));
            jasperXlsxExportMgr.setExporterOutput(simpleOutputStreamExporterOutput);
            jasperXlsxExportMgr.exportReport();
            bytes = out.toByteArray();
            out.close();
        } catch (JRException ex){
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return bytes;
    }


}
