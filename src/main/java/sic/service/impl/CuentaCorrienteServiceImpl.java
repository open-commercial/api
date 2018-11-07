package sic.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
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
import sic.repository.RenglonCuentaCorrienteRepository;
import sic.service.*;

@Service
public class CuentaCorrienteServiceImpl implements ICuentaCorrienteService {

  private final CuentaCorrienteRepository cuentaCorrienteRepository;
  private final CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository;
  private final CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository;
  private final RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository;
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  @Lazy
  public CuentaCorrienteServiceImpl(
      CuentaCorrienteRepository cuentaCorrienteRepository,
      CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository,
      CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository,
      RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository) {

    this.cuentaCorrienteRepository = cuentaCorrienteRepository;
    this.cuentaCorrienteClienteRepository = cuentaCorrienteClienteRepository;
    this.cuentaCorrienteProveedorRepository = cuentaCorrienteProveedorRepository;
    this.renglonCuentaCorrienteRepository = renglonCuentaCorrienteRepository;
  }

  @Override
  public CuentaCorrienteCliente guardarCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente) {
    cuentaCorrienteCliente.setFechaApertura(cuentaCorrienteCliente.getCliente().getFechaAlta());
    this.validarCuentaCorriente(cuentaCorrienteCliente);
    cuentaCorrienteCliente = cuentaCorrienteClienteRepository.save(cuentaCorrienteCliente);
    LOGGER.warn("La Cuenta Corriente Cliente {} se guardó correctamente.", cuentaCorrienteCliente);
    return cuentaCorrienteCliente;
  }

  @Override
  public CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(
      CuentaCorrienteProveedor cuentaCorrienteProveedor) {
    cuentaCorrienteProveedor.setFechaApertura(new Date());
    this.validarCuentaCorriente(cuentaCorrienteProveedor);
    cuentaCorrienteProveedor = cuentaCorrienteProveedorRepository.save(cuentaCorrienteProveedor);
    LOGGER.warn(
        "La Cuenta Corriente Proveedor {} se guardó correctamente.", cuentaCorrienteProveedor);
    return cuentaCorrienteProveedor;
  }

  @Override
  public void validarCuentaCorriente(CuentaCorriente cuentaCorriente) {
    // Entrada de Datos
    // Requeridos
    if (cuentaCorriente.getFechaApertura() == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cuenta_corriente_fecha_vacia"));
    }
    if (cuentaCorriente.getEmpresa() == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_caja_empresa_vacia"));
    }
    if (cuentaCorriente instanceof CuentaCorrienteCliente) {
      if (((CuentaCorrienteCliente) cuentaCorriente).getCliente() == null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio"));
      }
    } else if (cuentaCorriente instanceof CuentaCorrienteProveedor) {
      if (((CuentaCorrienteProveedor) cuentaCorriente).getProveedor() == null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_proveedor_vacio"));
      }
    }
    // Duplicados
    if (cuentaCorriente.getIdCuentaCorriente() != null) {
      if (cuentaCorrienteRepository.findById(cuentaCorriente.getIdCuentaCorriente()) != null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_cuenta_corriente_duplicada"));
      }
    }
  }

  @Override
  public CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente) {
    CuentaCorrienteCliente cc =
        cuentaCorrienteClienteRepository.findByClienteAndEmpresaAndEliminada(
            cliente, cliente.getEmpresa(), false);
    cc.setFechaUltimoMovimiento(this.getFechaUltimoMovimiento(cc.getIdCuentaCorriente()));
    return cc;
  }

  @Override
  public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor) {
    return cuentaCorrienteProveedorRepository.findByProveedorAndEmpresaAndEliminada(
            proveedor, proveedor.getEmpresa(), false);
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
      cc.setSaldo(cc.getSaldo().add(rcc.getMonto()));
      rcc.setCuentaCorriente(cc);
      this.renglonCuentaCorrienteRepository.save(rcc);
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_guardado"),
          rcc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      RenglonCuentaCorriente rcc = this.getRenglonCuentaCorrienteDeFactura(facturaVenta, false);
      rcc.setEliminado(true);
      CuentaCorriente cc = this.getCuentaCorrientePorCliente(facturaVenta.getCliente());
      cc.setSaldo(cc.getSaldo().add(rcc.getMonto().negate()));
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_eliminado"),
          rcc);
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
      cc.setSaldo(cc.getSaldo().add(rcc.getMonto()));
      rcc.setCuentaCorriente(cc);
      this.renglonCuentaCorrienteRepository.save(rcc);
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_guardado"),
          rcc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      RenglonCuentaCorriente rcc = this.getRenglonCuentaCorrienteDeFactura(facturaCompra, false);
      rcc.setEliminado(true);
      CuentaCorriente cc = this.getCuentaCorrientePorProveedor(facturaCompra.getProveedor());
      cc.setSaldo(cc.getSaldo().add(rcc.getMonto().negate()));
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_eliminado"),
          rcc);
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
      CuentaCorriente cc = this.getCuentaCorrientePorNota(nota);
      if (nota instanceof NotaCredito) {
        rcc.setMonto(nota.getTotal());
        cc.setSaldo(cc.getSaldo().add(rcc.getMonto()));
      }
      if (nota instanceof NotaDebito) {
        rcc.setMonto(nota.getTotal().negate());
        cc.setSaldo(cc.getSaldo().add(rcc.getMonto()));
      }
      rcc.setDescripcion(nota.getMotivo());
      rcc.setNota(nota);
      rcc.setFecha(nota.getFecha());
      rcc.setIdMovimiento(nota.getIdNota());
      if (nota.getMovimiento() == Movimiento.COMPRA) rcc.setCAE(nota.getCAE());
      cc.getRenglones().add(rcc);
      rcc.setCuentaCorriente(cc);
      this.renglonCuentaCorrienteRepository.save(rcc);
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_guardado"),
          rcc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      RenglonCuentaCorriente rcc = this.getRenglonCuentaCorrienteDeNota(nota, false);
      rcc.setEliminado(true);
      CuentaCorriente cc = this.getCuentaCorrientePorNota(nota);
      if (nota instanceof NotaCredito) {
        cc.setSaldo(cc.getSaldo().subtract(rcc.getMonto()));
      }
      if (nota instanceof NotaDebito) {
        cc.setSaldo(cc.getSaldo().subtract(rcc.getMonto()));
      }
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_eliminado"),
          rcc);
    }
  }

  private CuentaCorriente getCuentaCorrientePorNota(Nota nota) {
    if (nota.getMovimiento().equals(Movimiento.VENTA)) {
      return this.getCuentaCorrientePorCliente(
          nota.getCliente());
    } else {
      return this.getCuentaCorrientePorProveedor(nota.getProveedor());
    }
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
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes")
                .getString("mensaje_cuenta_corriente_no_existente"));
      }
      cc.getRenglones().add(rcc);
      cc.setSaldo(cc.getSaldo().add(recibo.getMonto()));
      rcc.setCuentaCorriente(cc);
      this.renglonCuentaCorrienteRepository.save(rcc);
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_guardado"),
          rcc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      rcc = this.getRenglonCuentaCorrienteDeRecibo(recibo, false);
      rcc.setEliminado(true);
      CuentaCorriente cc = null;
      if (recibo.getCliente() != null) {
        cc = this.getCuentaCorrientePorCliente(recibo.getCliente());
      } else if (recibo.getProveedor() != null) {
        cc = this.getCuentaCorrientePorProveedor(recibo.getProveedor());
      }
      if (null == cc) {
        throw new BusinessServiceException(
                ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_cuenta_corriente_no_existente"));
      }
      cc.setSaldo(cc.getSaldo().subtract(recibo.getMonto()));
      LOGGER.warn(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_reglon_cuenta_corriente_eliminado"),
          rcc);
    }
  }

  @Override
  public byte[] getReporteCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page, String formato) {
    ClassLoader classLoader = CuentaCorrienteServiceImpl.class.getClassLoader();
    InputStream isFileReport =
        classLoader.getResourceAsStream("sic/vista/reportes/CuentaCorriente.jasper");
    page = new PageRequest(0, (page.getPageNumber() + 1) * page.getPageSize());
    JRBeanCollectionDataSource ds =
        new JRBeanCollectionDataSource(
            this.getRenglonesCuentaCorriente(cuentaCorrienteCliente.getIdCuentaCorriente(), page)
                .getContent());
    Map<String, Object> params = new HashMap<>();
    params.put("cuentaCorrienteCliente", cuentaCorrienteCliente);
    if (cuentaCorrienteCliente.getEmpresa().getLogo() != null && !cuentaCorrienteCliente.getEmpresa().getLogo().isEmpty()) {
      try {
        params.put(
            "logo",
            new ImageIcon(ImageIO.read(new URL(cuentaCorrienteCliente.getEmpresa().getLogo())))
                .getImage());
      } catch (IOException ex) {
        LOGGER.error(ex.getMessage());
        throw new ServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_empresa_404_logo"), ex);
      }
    }
    switch (formato) {
      case "xlsx":
        try {
          return xlsReportToArray(JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
          LOGGER.error(ex.getMessage());
          throw new ServiceException(
              ResourceBundle.getBundle("Mensajes").getString("mensaje_error_reporte"), ex);
        }
      case "pdf":
        try {
          return JasperExportManager.exportReportToPdf(
              JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
          LOGGER.error(ex.getMessage());
          throw new ServiceException(
              ResourceBundle.getBundle("Mensajes").getString("mensaje_error_reporte"), ex);
        }
      default:
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_formato_no_valido"));
    }
  }

  private byte[] xlsReportToArray(JasperPrint jasperPrint) {
    byte[] bytes = null;
    try {
      JRXlsxExporter jasperXlsxExportMgr = new JRXlsxExporter();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      SimpleOutputStreamExporterOutput simpleOutputStreamExporterOutput =
          new SimpleOutputStreamExporterOutput(out);
      jasperXlsxExportMgr.setExporterInput(new SimpleExporterInput(jasperPrint));
      jasperXlsxExportMgr.setExporterOutput(simpleOutputStreamExporterOutput);
      jasperXlsxExportMgr.exportReport();
      bytes = out.toByteArray();
      out.close();
    } catch (JRException ex) {
      LOGGER.error(ex.getMessage());
      throw new ServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_error_reporte"), ex);
    } catch (IOException ex) {
      LOGGER.error(ex.getMessage());
    }
    return bytes;
  }

  @Override
  public RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente) {
    return renglonCuentaCorrienteRepository.save(renglonCuentaCorriente);
  }

  @Override
  public RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(
      Factura factura, boolean eliminado) {
    return renglonCuentaCorrienteRepository.findByFacturaAndEliminado(factura, eliminado);
  }

  @Override
  public RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado) {
    return renglonCuentaCorrienteRepository.findByNotaAndEliminado(nota, eliminado);
  }

  @Override
  public RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(
      Recibo recibo, boolean eliminado) {
    return renglonCuentaCorrienteRepository.findByReciboAndEliminado(recibo, eliminado);
  }

  @Override
  public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(
      long idCuentaCorriente, Pageable page) {
    return renglonCuentaCorrienteRepository.findAllByCuentaCorrienteAndEliminado(
        idCuentaCorriente, page);
  }

  @Override
  public Date getFechaUltimoMovimiento(long idCuentaCorriente) {
    return renglonCuentaCorrienteRepository.getFechaUltimoMovimiento(idCuentaCorriente);
  }

  @Override
  public int updateCAEFactura(long idFactura, long CAE) {
    return renglonCuentaCorrienteRepository.updateCAEFactura(idFactura, CAE);
  }

  @Override
  public int updateCAENota(long idNota, long CAE) {
    return renglonCuentaCorrienteRepository.updateCAENota(idNota, CAE);
  }
}
