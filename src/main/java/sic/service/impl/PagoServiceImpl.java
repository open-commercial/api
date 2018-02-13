package sic.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Factura;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.NotaDebito;
import sic.modelo.Pago;
import sic.service.IFacturaService;
import sic.service.IPagoService;
import sic.service.BusinessServiceException;
import sic.repository.PagoRepository;
import sic.service.IEmpresaService;
import sic.service.INotaService;
import sic.service.IReciboService;

@Service
public class PagoServiceImpl implements IPagoService {

    private final PagoRepository pagoRepository;
    private final IFacturaService facturaService;
    private final IEmpresaService empresaService;    
    private final INotaService notaService;
    private final IReciboService reciboService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Lazy
    @Autowired
    public PagoServiceImpl(PagoRepository pagoRepository,
            IEmpresaService empresaService,            
            IFacturaService facturaService,
            INotaService notaService, 
            IReciboService reciboService) {
        this.empresaService = empresaService;        
        this.pagoRepository = pagoRepository;
        this.facturaService = facturaService;
        this.notaService = notaService;
        this.reciboService = reciboService;
    }

    @Override
    public Pago getPagoPorId(long idPago) {
        Pago pago = this.pagoRepository.findById(idPago);
        if (pago == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pago_no_existente"));
        }
        return pago;
    }
    
    @Override
    public List<Pago> getPagosDeLaFactura(long idFactura) {
        return pagoRepository.findByFacturaAndEliminado(facturaService.getFacturaPorId(idFactura), false);
    }
    
    @Override
    public BigDecimal getTotalPagosDeLaFactura(long idFactura) {
        BigDecimal total = pagoRepository.getTotalPagosDeFactura(idFactura);
        return (total != null) ? total : BigDecimal.ZERO;
    }
    
    @Override
    public List<Pago> getPagosDeNotas(long idNota) {
        return pagoRepository.findByNotaDebitoAndEliminado(notaService.getNotaPorId(idNota), false);
    }
    
    @Override
    public BigDecimal getTotalPagosDeNota(long idNota) {
        BigDecimal total = pagoRepository.getTotalPagosDeNota(idNota);
        return (total != null) ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getSaldoAPagarFactura(long idFactura) {
        return (facturaService.getTotalById(idFactura).subtract(this.getTotalPagosDeLaFactura(idFactura)));
    }
    
    @Override
    public BigDecimal getSaldoAPagarNotaDebito(long idNota) {
        return notaService.getTotalById(idNota).subtract(this.getTotalPagosDeNota(idNota));
    }

    @Override
    public List<Pago> getPagosCompraEntreFechasYFormaDePago(long id_Empresa, long id_FormaDePago, Date desde, Date hasta) {
        return pagoRepository.getPagosComprasPorClienteEntreFechas(id_Empresa, id_FormaDePago, desde, hasta);
    }
    
    @Override
    public Page<Pago> getPagosPorClienteEntreFechas(long idCliente, Date desde, Date hasta, Pageable page) {
        return pagoRepository.getPagosPorClienteEntreFechas(idCliente, desde, hasta, page);
    }
    
    @Override
    public List<Pago> getPagosRelacionadosAlRecibo(long idRecibo) {
        return this.pagoRepository.findAllByReciboAndEliminado(reciboService.getById(idRecibo), false);
    }

    @Override
    public long getSiguienteNroPago(Long idEmpresa) {
        Pago pago = pagoRepository.findTopByEmpresaOrderByNroPagoDesc(empresaService.getEmpresaPorId(idEmpresa));
        if (pago == null) {
            return 1; // No existe ningun Pago anterior
        } else {
            return 1 + pago.getNroPago();
        }
    }
    
    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        this.validarOperacion(pago);
        pago.setNroPago(this.getSiguienteNroPago(pago.getEmpresa().getId_Empresa()));
        Calendar fechaPago = Calendar.getInstance();
        fechaPago.add(Calendar.SECOND, 1);
        pago.setFecha(fechaPago.getTime());
        pago = pagoRepository.save(pago);
        if (pago.getFactura() != null && pago.getNotaDebito() == null) {
            facturaService.actualizarFacturaEstadoPago(pago.getFactura());
        } else if (pago.getFactura() == null && pago.getNotaDebito() != null) {
            notaService.actualizarNotaDebitoEstadoPago((NotaDebito)pago.getNotaDebito());
        }
        LOGGER.warn("El Pago " + pago + " se guardó correctamente.");
        return pago;
    }

    @Override
    @Transactional
    public void eliminar(long idPago) {
        Pago pago = this.getPagoPorId(idPago);
        pago.setEliminado(true);
        if (pago.getFactura() != null) {
            facturaService.actualizarFacturaEstadoPago(pago.getFactura());
        }
        if (pago.getNotaDebito() != null) {
            notaService.actualizarNotaDebitoEstadoPago((NotaDebito) pago.getNotaDebito());
        }
        pago.getRecibo().setSaldoSobrante(pago.getMonto().add(pago.getRecibo().getSaldoSobrante()));
        pagoRepository.save(pago);
        LOGGER.warn("El Pago " + pago + " se eliminó correctamente.");
    }


    @Override
    public BigDecimal calcularTotalPagos(List<Pago> pagos) {
        BigDecimal total = BigDecimal.ZERO;
        pagos.stream().map((pago) -> {
            if (pago.getFactura() instanceof FacturaVenta) {
                total.add(pago.getMonto());
            }
            return pago;
        }).filter((pago) -> (pago.getFactura() instanceof FacturaCompra)).forEachOrdered((pago) -> {
            total.subtract(pago.getMonto());
        });
        return total;
    }

    @Override
    public BigDecimal calcularTotalAdeudadoFacturasVenta(List<FacturaVenta> facturasVenta) {
        List<Factura> facturas = new ArrayList<>();
        facturas.addAll(facturasVenta);
        return this.calcularTotalAdeudadoFacturas(facturas);
    }

    @Override
    public BigDecimal calcularTotalAdeudadoFacturasCompra(List<FacturaCompra> facturasCompra) {
        List<Factura> facturas = new ArrayList<>();
        facturas.addAll(facturasCompra);
        return this.calcularTotalAdeudadoFacturas(facturas);
    }

    @Override
    public BigDecimal calcularTotalAdeudadoFacturas(List<Factura> facturas) {
        BigDecimal total = BigDecimal.ZERO;
        facturas.forEach((f) -> {
            total.add(this.getTotalPagosDeLaFactura(f.getId_Factura()));
        });
        return total;
    }
    
    @Override
    public BigDecimal getSaldoPagosPorCliente(long idCliente, Date hasta) {
        BigDecimal saldo = pagoRepository.getSaldoPagosPorCliente(idCliente, hasta);
        return (saldo == null) ? BigDecimal.ZERO : saldo;
    }

    @Override
    public void validarOperacion(Pago pago) {
        //Requeridos
        if (pago.getMonto().compareTo(BigDecimal.ZERO) <= 0.0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pago_mayorQueCero_monto"));
        }
        if (pago.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pago_empresa_vacia"));
        }
        if (pago.getFormaDePago() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pago_formaDePago_vacia"));
        }
        if (pago.getFactura() != null) {
            if (pago.getFactura().isPagada() == true) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_pagada"));
            }
            if (pago.getMonto().setScale(3, RoundingMode.HALF_UP).compareTo(this.getSaldoAPagarFactura(pago.getFactura().getId_Factura()).setScale(3, RoundingMode.HALF_UP)) > 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_pago_mayorADeuda_monto"));
            }
        }
        if (pago.getNotaDebito() != null) {
            if (((NotaDebito) pago.getNotaDebito()).isPagada() == true) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_nota_debito_pagada"));
            }
            if (pago.getMonto().setScale(3, RoundingMode.HALF_UP).compareTo(this.getSaldoAPagarNotaDebito(pago.getNotaDebito().getIdNota()).setScale(3, RoundingMode.HALF_UP)) > 0) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_pago_mayorADeuda_monto"));
            }
        }
    }

}
