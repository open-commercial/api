package sic.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.*;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.repository.CuentaCorrienteClienteRepository;
import sic.repository.CuentaCorrienteProveedorRepository;
import sic.repository.CuentaCorrienteRepository;
import sic.service.*;

@Service
public class CuentaCorrienteServiceImpl implements ICuentaCorrienteService {
    
    private final CuentaCorrienteRepository cuentaCorrienteRepository;
    private final CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository;
    private final CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository;
    private final IClienteService clienteService;
    private final IProveedorService proveedorService;
    private final IRenglonCuentaCorrienteService renglonCuentaCorrienteService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Lazy
    public CuentaCorrienteServiceImpl(CuentaCorrienteRepository cuentaCorrienteRepository,
                                      CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository,
                                      CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository,
                                      IClienteService clienteService, IProveedorService proveedorService,
                                      IRenglonCuentaCorrienteService renglonCuentaCorrienteService) {

                this.cuentaCorrienteRepository = cuentaCorrienteRepository;
                this.cuentaCorrienteClienteRepository = cuentaCorrienteClienteRepository;
                this.cuentaCorrienteProveedorRepository = cuentaCorrienteProveedorRepository;
                this.clienteService = clienteService;
                this.proveedorService = proveedorService;
                this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
    }

    @Override
    public void eliminar(Long idCuentaCorriente) {
        CuentaCorriente cuentaCorriente = this.getCuentaCorrientePorID(idCuentaCorriente);
        if (cuentaCorriente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cuenta_corriente_no_existente"));
        }
        cuentaCorriente.setEliminada(true);
        this.cuentaCorrienteRepository.save(cuentaCorriente);
    }

    @Override
    public CuentaCorriente getCuentaCorrientePorID(Long idCuentaCorriente) {
        CuentaCorriente cuentaCorriente = cuentaCorrienteRepository.findById(idCuentaCorriente);
        if (cuentaCorriente == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cuenta_corriente_no_existente"));
        }
        return cuentaCorriente;
    }

    @Override
    public CuentaCorrienteCliente guardarCuentaCorrienteCliente(CuentaCorrienteCliente cuentaCorrienteCliente) {
        cuentaCorrienteCliente.setFechaApertura(cuentaCorrienteCliente.getCliente().getFechaAlta());
        this.validarCuentaCorriente(cuentaCorrienteCliente);
        cuentaCorrienteCliente = cuentaCorrienteClienteRepository.save(cuentaCorrienteCliente);
        LOGGER.warn("La Cuenta Corriente Cliente " + cuentaCorrienteCliente + " se guardó correctamente." );
        return cuentaCorrienteCliente;
    }
    
    @Override
    public CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(CuentaCorrienteProveedor cuentaCorrienteProveedor) {
        cuentaCorrienteProveedor.setFechaApertura(new Date());
        this.validarCuentaCorriente(cuentaCorrienteProveedor);
        cuentaCorrienteProveedor = cuentaCorrienteProveedorRepository.save(cuentaCorrienteProveedor);
        LOGGER.warn("La Cuenta Corriente Proveedor " + cuentaCorrienteProveedor + " se guardó correctamente." );
        return cuentaCorrienteProveedor;
    }

    @Override
    public void validarCuentaCorriente(CuentaCorriente cuentaCorriente) {
        //Entrada de Datos
        //Requeridos
        if (cuentaCorriente.getFechaApertura() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cuenta_corriente_fecha_vacia"));
        }
        if (cuentaCorriente.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_empresa_vacia"));
        }
        if (cuentaCorriente instanceof CuentaCorrienteCliente) {
            if (((CuentaCorrienteCliente) cuentaCorriente).getCliente() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cliente_vacio"));
            }
        } else if (cuentaCorriente instanceof CuentaCorrienteProveedor) {
            if (((CuentaCorrienteProveedor) cuentaCorriente).getProveedor() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_proveedor_vacio"));
            }
        }
        //Duplicados        
        if (cuentaCorriente.getIdCuentaCorriente() != null) {
            if (cuentaCorrienteRepository.findById(cuentaCorriente.getIdCuentaCorriente()) != null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cuenta_corriente_duplicada"));
            }
        }
    }

    @Override
    public BigDecimal getSaldoCuentaCorriente(long idCuentaCorriente) {
        BigDecimal saldo = renglonCuentaCorrienteService.getSaldoCuentaCorriente(idCuentaCorriente);
        return (saldo != null) ? saldo : BigDecimal.ZERO;
    }
    
    @Override
    public CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente) {
        CuentaCorrienteCliente cc = cuentaCorrienteClienteRepository.findByClienteAndEmpresaAndEliminada(cliente, cliente.getEmpresa(), false);
        cc.setSaldo(this.getSaldoCuentaCorriente(cc.getIdCuentaCorriente()));
        cc.setFechaUltimoMovimiento(this.getFechaUltimoMovimiento(cc.getIdCuentaCorriente()));
        return cc;
    }
    
    @Override
    public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor) {
        CuentaCorrienteProveedor cc = cuentaCorrienteProveedorRepository.findByProveedorAndEmpresaAndEliminada(proveedor, proveedor.getEmpresa(), false);
        cc.setSaldo(this.getSaldoCuentaCorriente(cc.getIdCuentaCorriente()));
        cc.setFechaUltimoMovimiento(this.getFechaUltimoMovimiento(cc.getIdCuentaCorriente()));
        return cc;
    }
    
    @Override
    public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable pageable) {
        return renglonCuentaCorrienteService.getRenglonesCuentaCorriente(idCuentaCorriente, pageable);
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo) {
        if (tipo == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipoComprobante(facturaVenta.getTipoComprobante());
            rcc.setSerie(facturaVenta.getNumSerie());
            rcc.setNumero(facturaVenta.getNumFactura());
            rcc.setFactura(facturaVenta);
            rcc.setFecha(facturaVenta.getFecha());
            rcc.setFechaVencimiento(facturaVenta.getFechaVencimiento());
            rcc.setIdMovimiento(facturaVenta.getId_Factura());
            rcc.setMonto(facturaVenta.getTotal().negate());
            CuentaCorriente cc = this.getCuentaCorrientePorCliente(facturaVenta.getCliente());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (tipo == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeFactura(facturaVenta, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(FacturaCompra facturaCompra, TipoDeOperacion tipo) {
        if (tipo == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipoComprobante(facturaCompra.getTipoComprobante());
            rcc.setSerie(facturaCompra.getNumSerie());
            rcc.setNumero(facturaCompra.getNumFactura());
            rcc.setFactura(facturaCompra);
            rcc.setFecha(facturaCompra.getFecha());
            rcc.setFechaVencimiento(facturaCompra.getFechaVencimiento());
            rcc.setIdMovimiento(facturaCompra.getId_Factura());
            rcc.setMonto(facturaCompra.getTotal().negate());
            CuentaCorriente cc = this.getCuentaCorrientePorProveedor(facturaCompra.getProveedor());
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (tipo == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeFactura(facturaCompra, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }

    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo) {
        if (tipo == TipoDeOperacion.ALTA) {
            RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
            rcc.setTipoComprobante(nota.getTipoComprobante());
            rcc.setSerie(nota.getSerie());
            rcc.setNumero(nota.getNroNota());
            if (nota instanceof NotaCredito) {
                rcc.setMonto(nota.getTotal());
                rcc.setDescripcion(nota.getMotivo()); 
            }
            if (nota instanceof NotaDebito) {
                rcc.setMonto(nota.getTotal().negate());
                String descripcion = "";
                if (((NotaDebito) nota).getRecibo() != null) {
                    descripcion = ((NotaDebito) nota).getRecibo().getConcepto();
                }           
                rcc.setDescripcion(descripcion);
            }
            rcc.setNota(nota); 
            rcc.setFecha(nota.getFecha());
            rcc.setIdMovimiento(nota.getIdNota());
            CuentaCorriente cc = this.getCuentaCorrientePorNota(nota);
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente." );
        }
        if (tipo == TipoDeOperacion.ELIMINACION) {
            RenglonCuentaCorriente rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeNota(nota, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente." );
        }
    }
    
    private CuentaCorriente getCuentaCorrientePorNota(Nota nota) {
        CuentaCorriente cc = null;
        if (nota instanceof NotaCreditoCliente || nota instanceof NotaDebitoCliente) {
            if (nota instanceof NotaCreditoCliente) {
                cc = this.getCuentaCorrientePorCliente(((NotaCreditoCliente) nota).getCliente());
            } else if (nota instanceof NotaDebitoCliente) {
                cc = this.getCuentaCorrientePorCliente(((NotaDebitoCliente) nota).getCliente());
            }
        } else if (nota instanceof NotaCreditoProveedor || nota instanceof NotaDebitoProveedor) {
            if (nota instanceof NotaCreditoProveedor) {
                cc = this.getCuentaCorrientePorProveedor(((NotaCreditoProveedor) nota).getProveedor());
            } else if (nota instanceof NotaDebitoProveedor) {
                cc = this.getCuentaCorrientePorProveedor(((NotaDebitoProveedor) nota).getProveedor());
            }
        }
        return cc;
    }
    
    @Override
    @Transactional
    public void asentarEnCuentaCorriente(Recibo recibo, TipoDeOperacion tipo) {
        RenglonCuentaCorriente rcc;
        if (tipo == TipoDeOperacion.ALTA) {
            rcc = new RenglonCuentaCorriente();
            rcc.setRecibo(recibo);
            rcc.setTipoComprobante(TipoDeComprobante.RECIBO);
            rcc.setSerie(recibo.getNumSerie());
            rcc.setNumero(recibo.getNumRecibo());
            rcc.setDescripcion(recibo.getConcepto());
            rcc.setFecha(recibo.getFecha());
            rcc.setIdMovimiento(recibo.getIdRecibo());
            rcc.setMonto(recibo.getMonto());
            CuentaCorriente cc = null;
            if (recibo.getCliente() != null) {
                cc = this.getCuentaCorrientePorCliente(recibo.getCliente());
            } else if (recibo.getProveedor() != null) {
                cc = this.getCuentaCorrientePorProveedor(recibo.getProveedor());
            }
            if (null == cc) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cuenta_corriente_no_existente"));

            }
            cc.getRenglones().add(rcc);
            rcc.setCuentaCorriente(cc);
            this.renglonCuentaCorrienteService.guardar(rcc);
            LOGGER.warn("El renglon " + rcc + " se guardó correctamente.");
        }
        if (tipo == TipoDeOperacion.ELIMINACION) {
            rcc = this.renglonCuentaCorrienteService.getRenglonCuentaCorrienteDeRecibo(recibo, false);
            rcc.setEliminado(true);
            LOGGER.warn("El renglon " + rcc + " se eliminó correctamente.");
        }
    }

    @Override
    public Date getFechaUltimoMovimiento(long idCuentaCorriente) {
        return renglonCuentaCorrienteService.getFechaUltimoMovimiento(idCuentaCorriente);
    }

    @Override
    public byte[] getReporteCuentaCorrienteClienteXlsx(CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page) {
        ClassLoader classLoader = CuentaCorrienteServiceImpl.class.getClassLoader();
        InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/CuentaCorriente.jasper");
        Map<String, Object> params = new HashMap<>();
        page = new PageRequest(0, (page.getPageNumber() + 1) * page.getPageSize());
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(this.getRenglonesCuentaCorriente(cuentaCorrienteCliente.getIdCuentaCorriente(), page).getContent());
        try {
            return xlsReportToArray(JasperFillManager.fillReport(isFileReport,  this.agregarParametros(params, cuentaCorrienteCliente), ds));
        } catch (JRException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        }
    }

    @Override
    public byte[] getReporteCuentaCorrienteClientePDF(CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page) {
        ClassLoader classLoader = CuentaCorrienteServiceImpl.class.getClassLoader();
        InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/CuentaCorriente.jasper");
        Map<String, Object> params = new HashMap<>();
        page = new PageRequest(0, (page.getPageNumber() + 1) * page.getPageSize());
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(this.getRenglonesCuentaCorriente(cuentaCorrienteCliente.getIdCuentaCorriente(), page).getContent());
        try {
            return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(isFileReport, this.agregarParametros(params, cuentaCorrienteCliente), ds));
        } catch (JRException ex) {
            LOGGER.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        }
    }

    private Map<String, Object> agregarParametros(Map<String, Object> params, CuentaCorrienteCliente cuentaCorrienteCliente) {
        params.put("cuentaCorrienteCliente", cuentaCorrienteCliente);
        if (!cuentaCorrienteCliente.getEmpresa().getLogo().isEmpty()) {
            try {
                params.put("logo", new ImageIcon(ImageIO.read(new URL(cuentaCorrienteCliente.getEmpresa().getLogo()))).getImage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_empresa_404_logo"), ex);
            }
        }
        return params;
    }

    private byte[] xlsReportToArray(JasperPrint jasperPrint) {
        byte[] bytes = null;
        try{
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
