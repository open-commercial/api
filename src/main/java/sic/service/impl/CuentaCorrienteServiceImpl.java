package sic.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.validation.Valid;

import com.querydsl.core.BooleanBuilder;
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
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.repository.CuentaCorrienteClienteRepository;
import sic.repository.CuentaCorrienteProveedorRepository;
import sic.repository.CuentaCorrienteRepository;
import sic.repository.RenglonCuentaCorrienteRepository;
import sic.service.*;

@Service
@Validated
public class CuentaCorrienteServiceImpl implements ICuentaCorrienteService {

  private final CuentaCorrienteRepository cuentaCorrienteRepository;
  private final CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository;
  private final CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository;
  private final RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  @Lazy
  public CuentaCorrienteServiceImpl(
      CuentaCorrienteRepository cuentaCorrienteRepository,
      CuentaCorrienteClienteRepository cuentaCorrienteClienteRepository,
      CuentaCorrienteProveedorRepository cuentaCorrienteProveedorRepository,
      RenglonCuentaCorrienteRepository renglonCuentaCorrienteRepository,
      IUsuarioService usuarioService, IClienteService clienteService) {

    this.cuentaCorrienteRepository = cuentaCorrienteRepository;
    this.cuentaCorrienteClienteRepository = cuentaCorrienteClienteRepository;
    this.cuentaCorrienteProveedorRepository = cuentaCorrienteProveedorRepository;
    this.renglonCuentaCorrienteRepository = renglonCuentaCorrienteRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
  }

  @Override
  @Transactional
  public CuentaCorrienteCliente guardarCuentaCorrienteCliente(
      @Valid CuentaCorrienteCliente cuentaCorrienteCliente) {
    this.validarOperacion(cuentaCorrienteCliente);
    cuentaCorrienteCliente = cuentaCorrienteClienteRepository.save(cuentaCorrienteCliente);
    logger.warn("La Cuenta Corriente Cliente {} se guardó correctamente.", cuentaCorrienteCliente);
    return cuentaCorrienteCliente;
  }

  @Override
  @Transactional
  public CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(
      @Valid CuentaCorrienteProveedor cuentaCorrienteProveedor) {
    this.validarOperacion(cuentaCorrienteProveedor);
    cuentaCorrienteProveedor = cuentaCorrienteProveedorRepository.save(cuentaCorrienteProveedor);
    logger.warn(
        "La Cuenta Corriente Proveedor {} se guardó correctamente.", cuentaCorrienteProveedor);
    return cuentaCorrienteProveedor;
  }

  @Override
  public void validarOperacion(CuentaCorriente cuentaCorriente) {
    // Duplicados
    if (cuentaCorriente.getIdCuentaCorriente() != null && cuentaCorrienteRepository.findById(cuentaCorriente.getIdCuentaCorriente()).isPresent()) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_cuenta_corriente_duplicada"));
    }
  }

  @Override
  public void eliminarCuentaCorrienteCliente(long idCliente) {
    cuentaCorrienteClienteRepository.eliminarCuentaCorrienteCliente(idCliente);
  }

  @Override
  public void eliminarCuentaCorrienteProveedor(long idProveedor) {
    cuentaCorrienteProveedorRepository.eliminarCuentaCorrienteProveedor(idProveedor);
  }

  @Override
  public Page<CuentaCorrienteCliente> buscarCuentaCorrienteCliente(
      BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn) {
    QCuentaCorrienteCliente qCuentaCorrienteCliente =
        QCuentaCorrienteCliente.cuentaCorrienteCliente;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorNombreFiscal()) {
      String[] terminos = criteria.getNombreFiscal().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qCuentaCorrienteCliente.cliente.nombreFiscal.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorNombreFantasia()) {
      String[] terminos = criteria.getNombreFantasia().split(" ");
      BooleanBuilder nfPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        nfPredicate.and(qCuentaCorrienteCliente.cliente.nombreFantasia.containsIgnoreCase(termino));
      }
      builder.or(nfPredicate);
    }
    if (criteria.isBuscaPorIdFiscal())
      builder.or(qCuentaCorrienteCliente.cliente.idFiscal.eq(criteria.getIdFiscal()));
    if (criteria.isBuscarPorNroDeCliente())
      builder.or(
          qCuentaCorrienteCliente.cliente.nroCliente.containsIgnoreCase(
              criteria.getNroDeCliente()));
    if (criteria.isBuscaPorViajante())
      builder.and(qCuentaCorrienteCliente.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.isBuscaPorLocalidad())
      builder.and(
          qCuentaCorrienteCliente.cliente.ubicacionFacturacion.localidad.idLocalidad.eq(
              criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(
          qCuentaCorrienteCliente.cliente.ubicacionFacturacion.localidad.provincia.idProvincia.eq(
              criteria.getIdProvincia()));
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
    if (!usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLogueado.getRoles().contains(Rol.VENDEDOR)
        && !usuarioLogueado.getRoles().contains(Rol.ENCARGADO)) {
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (Rol rol : usuarioLogueado.getRoles()) {
        switch (rol) {
          case VIAJANTE:
            rsPredicate.or(qCuentaCorrienteCliente.cliente.viajante.eq(usuarioLogueado));
            break;
          case COMPRADOR:
            Cliente clienteRelacionado =
                clienteService.getClientePorIdUsuarioYidEmpresa(
                    idUsuarioLoggedIn, criteria.getIdEmpresa());
            if (clienteRelacionado != null) {
              rsPredicate.or(qCuentaCorrienteCliente.cliente.eq(clienteRelacionado));
            }
            break;
          default:
            rsPredicate.or(qCuentaCorrienteCliente.cliente.isNull());
            break;
        }
      }
      builder.and(rsPredicate);
    }
    builder.and(
        qCuentaCorrienteCliente
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qCuentaCorrienteCliente.eliminada.eq(false)));
    return cuentaCorrienteClienteRepository.findAll(builder, criteria.getPageable());
  }

  @Override
  public Page<CuentaCorrienteProveedor> buscarCuentaCorrienteProveedor(
      BusquedaCuentaCorrienteProveedorCriteria criteria) {
    QCuentaCorrienteProveedor qCuentaCorrienteProveedor =
        QCuentaCorrienteProveedor.cuentaCorrienteProveedor;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorNroProveedor())
      builder.or(
          qCuentaCorrienteProveedor.proveedor.nroProveedor.containsIgnoreCase(
              criteria.getNroProveedor()));
    if (criteria.isBuscaPorRazonSocial()) {
      String[] terminos = criteria.getRazonSocial().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(
            qCuentaCorrienteProveedor.proveedor.razonSocial.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorIdFiscal())
      builder.or(qCuentaCorrienteProveedor.proveedor.idFiscal.eq(criteria.getIdFiscal()));
    if (criteria.isBuscaPorLocalidad())
      builder.and(
          qCuentaCorrienteProveedor.proveedor.ubicacion.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(
          qCuentaCorrienteProveedor.proveedor.ubicacion.localidad.provincia.idProvincia.eq(
              criteria.getIdProvincia()));
    builder.and(
        qCuentaCorrienteProveedor
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qCuentaCorrienteProveedor.eliminada.eq(false)));
    return cuentaCorrienteProveedorRepository.findAll(builder, criteria.getPageable());
  }

  @Override
  public CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente) {
    return cuentaCorrienteClienteRepository.findByClienteAndEmpresaAndEliminada(
            cliente, cliente.getEmpresa(), false);
  }

  @Override
  public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor) {
    return cuentaCorrienteProveedorRepository.findByProveedorAndEmpresaAndEliminada(
            proveedor, proveedor.getEmpresa(), false);
  }

  @Override
  @Transactional
  public void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo) {
    CuentaCorriente cc = this.getCuentaCorrientePorCliente(facturaVenta.getCliente());
    if (null == cc) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_cuenta_corriente_no_existente"));
    }
    if (tipo == TipoDeOperacion.ALTA) {
      this.altaRenglonCuentaCorrienteDeFactura(facturaVenta, cc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      cc = this.getCuentaCorrientePorCliente(facturaVenta.getCliente());
      cc.setSaldo(cc.getSaldo().subtract(facturaVenta.getTotal()));
      RenglonCuentaCorriente rcc = this.getRenglonCuentaCorrienteDeFactura(facturaVenta, false);
      this.cambiarFechaUltimoComprobante(cc, rcc);
      rcc.setEliminado(true);
      logger.warn(RESOURCE_BUNDLE.getString("mensaje_reglon_cuenta_corriente_eliminado"), rcc);
    }
  }

  @Override
  @Transactional
  public void asentarEnCuentaCorriente(FacturaCompra facturaCompra, TipoDeOperacion tipo) {
    CuentaCorriente cc = this.getCuentaCorrientePorProveedor(facturaCompra.getProveedor());
    if (null == cc) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_cuenta_corriente_no_existente"));
    }
    if (tipo == TipoDeOperacion.ALTA) {
      this.altaRenglonCuentaCorrienteDeFactura(facturaCompra, cc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      cc = this.getCuentaCorrientePorProveedor(facturaCompra.getProveedor());
      cc.setSaldo(cc.getSaldo().subtract(facturaCompra.getTotal()));
      RenglonCuentaCorriente rcc = this.getRenglonCuentaCorrienteDeFactura(facturaCompra, false);
      this.cambiarFechaUltimoComprobante(cc, rcc);
      rcc.setEliminado(true);
      logger.warn(RESOURCE_BUNDLE.getString("mensaje_reglon_cuenta_corriente_eliminado"), rcc);
    }
  }

  private void altaRenglonCuentaCorrienteDeFactura(Factura factura, CuentaCorriente cc) {
    RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
    rcc.setTipoComprobante(factura.getTipoComprobante());
    rcc.setSerie(factura.getNumSerie());
    rcc.setNumero(factura.getNumFactura());
    rcc.setFactura(factura);
    rcc.setFecha(factura.getFecha());
    rcc.setFechaVencimiento(factura.getFechaVencimiento());
    rcc.setIdMovimiento(factura.getId_Factura());
    rcc.setMonto(factura.getTotal().negate());
    cc.getRenglones().add(rcc);
    cc.setSaldo(cc.getSaldo().add(rcc.getMonto()));
    cc.setFechaUltimoMovimiento(factura.getFecha());
    rcc.setCuentaCorriente(cc);
    this.renglonCuentaCorrienteRepository.save(rcc);
    logger.warn(RESOURCE_BUNDLE.getString("mensaje_reglon_cuenta_corriente_guardado"), rcc);
  }

  @Override
  @Transactional
  public void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo) {
    CuentaCorriente cc = this.getCuentaCorrientePorNota(nota);
    if (tipo == TipoDeOperacion.ALTA) {
      RenglonCuentaCorriente rcc = new RenglonCuentaCorriente();
      rcc.setTipoComprobante(nota.getTipoComprobante());
      rcc.setSerie(nota.getSerie());
      rcc.setNumero(nota.getNroNota());
      if (nota instanceof NotaCredito) {
        rcc.setMonto(nota.getTotal());
      }
      if (nota instanceof NotaDebito) {
        rcc.setMonto(nota.getTotal().negate());
      }
      cc.setSaldo(cc.getSaldo().add(rcc.getMonto()));
      cc.setFechaUltimoMovimiento(nota.getFecha());
      rcc.setDescripcion(nota.getMotivo());
      rcc.setNota(nota);
      rcc.setFecha(nota.getFecha());
      rcc.setIdMovimiento(nota.getIdNota());
      if (nota.getMovimiento() == Movimiento.COMPRA) rcc.setCAE(nota.getCAE());
      cc.getRenglones().add(rcc);
      rcc.setCuentaCorriente(cc);
      this.renglonCuentaCorrienteRepository.save(rcc);
      logger.warn(RESOURCE_BUNDLE.getString("mensaje_reglon_cuenta_corriente_guardado"), rcc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      RenglonCuentaCorriente rcc = this.getRenglonCuentaCorrienteDeNota(nota, false);
      cc.setSaldo(cc.getSaldo().subtract(rcc.getMonto()));
      this.cambiarFechaUltimoComprobante(cc, rcc);
      rcc.setEliminado(true);
      logger.warn(RESOURCE_BUNDLE.getString("mensaje_reglon_cuenta_corriente_eliminado"), rcc);
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
      if (cc == null) {
        throw new BusinessServiceException(
                RESOURCE_BUNDLE.getString("mensaje_cuenta_corriente_no_existente"));
      }
      cc.getRenglones().add(rcc);
      cc.setSaldo(cc.getSaldo().add(recibo.getMonto()));
      cc.setFechaUltimoMovimiento(recibo.getFecha());
      rcc.setCuentaCorriente(cc);
      this.renglonCuentaCorrienteRepository.save(rcc);
      logger.warn(
              RESOURCE_BUNDLE.getString("mensaje_reglon_cuenta_corriente_guardado"), rcc);
    }
    if (tipo == TipoDeOperacion.ELIMINACION) {
      CuentaCorriente cc = null;
      if (recibo.getCliente() != null) {
        cc = this.getCuentaCorrientePorCliente(recibo.getCliente());
      } else if (recibo.getProveedor() != null) {
        cc = this.getCuentaCorrientePorProveedor(recibo.getProveedor());
      }
      if (null == cc) {
        throw new BusinessServiceException(
                RESOURCE_BUNDLE.getString("mensaje_cuenta_corriente_no_existente"));
      }
      cc.setSaldo(cc.getSaldo().subtract(recibo.getMonto()));
      rcc = this.getRenglonCuentaCorrienteDeRecibo(recibo, false);
      this.cambiarFechaUltimoComprobante(cc, rcc);
      rcc.setEliminado(true);
      logger.warn(
              RESOURCE_BUNDLE.getString("mensaje_reglon_cuenta_corriente_eliminado"), rcc);
    }
  }

  private void cambiarFechaUltimoComprobante(CuentaCorriente cc, RenglonCuentaCorriente rcc) {
    List<RenglonCuentaCorriente> ultimosDosMovimientos = this.getUltimosDosMovimientos(cc);
    if (ultimosDosMovimientos.size() == 2 && ultimosDosMovimientos.get(0).getIdRenglonCuentaCorriente().equals(rcc.getIdRenglonCuentaCorriente())) {
      cc.setFechaUltimoMovimiento(ultimosDosMovimientos.get(1).getFecha());
    } else if (ultimosDosMovimientos.size() == 1) {
      cc.setFechaUltimoMovimiento(null);
    }
  }

  @Override
  public byte[] getReporteCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page, String formato) {
    ClassLoader classLoader = CuentaCorrienteServiceImpl.class.getClassLoader();
    InputStream isFileReport =
        classLoader.getResourceAsStream("sic/vista/reportes/CuentaCorriente.jasper");
    page = PageRequest.of(0, (page.getPageNumber() + 1) * page.getPageSize());
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
        logger.error(ex.getMessage());
        throw new ServiceException(
                RESOURCE_BUNDLE.getString("mensaje_empresa_404_logo"), ex);
      }
    }
    switch (formato) {
      case "xlsx":
        try {
          return xlsReportToArray(JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
          logger.error(ex.getMessage());
          throw new ServiceException(
                  RESOURCE_BUNDLE.getString("mensaje_error_reporte"), ex);
        }
      case "pdf":
        try {
          return JasperExportManager.exportReportToPdf(
                  JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
          logger.error(ex.getMessage());
          throw new ServiceException(
                  RESOURCE_BUNDLE.getString("mensaje_error_reporte"), ex);
        }
      default:
        throw new BusinessServiceException(
                RESOURCE_BUNDLE.getString("mensaje_formato_no_valido"));
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
      logger.error(ex.getMessage());
      throw new ServiceException(
              RESOURCE_BUNDLE.getString("mensaje_error_reporte"), ex);
    } catch (IOException ex) {
      logger.error(ex.getMessage());
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
  public List<RenglonCuentaCorriente> getUltimosDosMovimientos(CuentaCorriente cuentaCorriente) {
    return renglonCuentaCorrienteRepository.findTop2ByAndCuentaCorrienteAndEliminadoOrderByIdRenglonCuentaCorrienteDesc(cuentaCorriente, false);
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
