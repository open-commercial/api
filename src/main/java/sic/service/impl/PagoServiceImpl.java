package sic.service.impl;

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
import sic.modelo.FormaDePago;
import sic.modelo.Pago;
import sic.modelo.TipoDeOperacion;
import sic.service.IFacturaService;
import sic.service.IPagoService;
import sic.service.BusinessServiceException;
import sic.repository.PagoRepository;
import sic.service.ICuentaCorrienteService;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;
import sic.service.INotaService;
import sic.util.Utilidades;

@Service
public class PagoServiceImpl implements IPagoService {

    private final PagoRepository pagoRepository;
    private final IFacturaService facturaService;
    private final IEmpresaService empresaService;
    private final IFormaDePagoService formaDePagoService;
    private final INotaService notaService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Lazy
    @Autowired
    public PagoServiceImpl(PagoRepository pagoRepository,
            IEmpresaService empresaService,
            IFormaDePagoService formaDePagoService,
            IFacturaService facturaService,
            INotaService notaService, 
            ICuentaCorrienteService cuentaCorrienteService) {

        this.empresaService = empresaService;
        this.formaDePagoService = formaDePagoService;
        this.pagoRepository = pagoRepository;
        this.facturaService = facturaService;
        this.notaService = notaService;
        this.cuentaCorrienteService = cuentaCorrienteService;
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
    public Double getTotalPagosDeLaFactura(long idFactura) {
        Double total = pagoRepository.getTotalPagosDeFactura(idFactura);
        return (total != null) ? total : 0.0;
    }
    
    @Override
    public List<Pago> getPagosDeNotas(long idNota) {
        return pagoRepository.findByNotaDebitoAndEliminado(notaService.getNotaPorId(idNota), false);
    }
    
    @Override
    public Double getTotalPagosDeNota(long idNota) {
        Double total = pagoRepository.getTotalPagosDeNota(idNota);
        return (total != null) ? total : 0.0;
    }

    @Override
    public double getSaldoAPagarFactura(long idFactura) {
        return facturaService.getTotalById(idFactura) - this.getTotalPagosDeLaFactura(idFactura);
    }
    
    @Override
    public double getSaldoAPagarNotaDebito(long idNota) {
        return notaService.getTotalById(idNota) - this.getTotalPagosDeNota(idNota);
    }

    @Override
    public List<Pago> getPagosEntreFechasYFormaDePago(long id_Empresa, long id_FormaDePago, Date desde, Date hasta) {
        return pagoRepository.findByFechaBetweenAndEmpresaAndFormaDePagoAndEliminado(desde, hasta, 
                empresaService.getEmpresaPorId(id_Empresa), formaDePagoService.getFormasDePagoPorId(id_FormaDePago), false);
    }
    
    @Override
    public Page<Pago> getPagosPorClienteEntreFechas(long idCliente, Date desde, Date hasta, Pageable page) {
        return pagoRepository.getPagosPorClienteEntreFechas(idCliente, desde, hasta, page);
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
        if (pago.getNotaDebito() != null && pago.getFactura() == null) {
            this.cuentaCorrienteService.asentarEnCuentaCorriente(pago, TipoDeOperacion.ALTA, pago.getNotaDebito().getCliente().getId_Cliente());
        } else {
            this.cuentaCorrienteService.asentarEnCuentaCorriente(pago, TipoDeOperacion.ALTA, null);
        }
        if (pago.getFactura() != null && pago.getNotaDebito() == null) {
            facturaService.actualizarFacturaEstadoPago(pago.getFactura());
        }
        LOGGER.warn("El Pago " + pago + " se guardó correctamente.");
        return pago;
    }

    @Override
    @Transactional
    public void eliminar(long idPago) {
        Pago pago = this.getPagoPorId(idPago);
        if (notaService.getNotaDebitoPorPago(idPago) != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_no_se_puede_eliminar"));
        }
        pago.setEliminado(true);
        this.cuentaCorrienteService.asentarEnCuentaCorriente(pago, TipoDeOperacion.ELIMINACION, null);
        pagoRepository.save(pago);
        if (pago.getFactura() != null) {
            facturaService.actualizarFacturaEstadoPago(pago.getFactura());
        }
        LOGGER.warn("El Pago " + pago + " se eliminó correctamente.");
    }
    
    @Override
    public double calcularTotalPagos(List<Pago> pagos) {
        double total = 0.0;
        for (Pago pago : pagos) {
            if (pago.getFactura() instanceof FacturaVenta) {
                total += pago.getMonto();
            }
            if (pago.getFactura() instanceof FacturaCompra) {
                total -= pago.getMonto();
            }
        }
        return total;
    }

    @Override
    public double calcularTotalAdeudadoFacturasVenta(List<FacturaVenta> facturasVenta) {
        List<Factura> facturas = new ArrayList<>();
        facturas.addAll(facturasVenta);
        return this.calcularTotalAdeudadoFacturas(facturas);
    }

    @Override
    public double calcularTotalAdeudadoFacturasCompra(List<FacturaCompra> facturasCompra) {
        List<Factura> facturas = new ArrayList<>();
        facturas.addAll(facturasCompra);
        return this.calcularTotalAdeudadoFacturas(facturas);
    }

    @Override
    public double calcularTotalAdeudadoFacturas(List<Factura> facturas) {
        double total = 0.0;
        total = facturas.stream().map(f -> f.getTotal() - this.getTotalPagosDeLaFactura(f.getId_Factura())).reduce(total, (accumulator, _item) -> accumulator + _item);
        return total;
    }
    
    @Override
    public double getSaldoPagosPorCliente(long idCliente, Date hasta) {
        Double saldo = pagoRepository.getSaldoPagosPorCliente(idCliente, hasta);
        return (saldo == null) ? 0 : saldo;
    }

    @Override
    @Transactional
    public void pagarMultiplesFacturas(List<Factura> facturas, double monto, FormaDePago formaDePago, String nota) {
        if (monto <= this.calcularTotalAdeudadoFacturas(facturas)) {
            List<Factura> facturasOrdenadas = facturaService.ordenarFacturasPorFechaAsc(facturas);
            for (Factura factura : facturasOrdenadas) {
                if (monto > 0.0) {
                    factura.setPagos(this.getPagosDeLaFactura(factura.getId_Factura()));
                    Pago nuevoPago = new Pago();
                    nuevoPago.setFormaDePago(formaDePago);
                    nuevoPago.setFactura(factura);
                    nuevoPago.setEmpresa(factura.getEmpresa());
                    nuevoPago.setNota(nota);
                    double saldoAPagar = this.getSaldoAPagarFactura(factura.getId_Factura());
                    if (saldoAPagar <= monto) {
                        monto = monto - saldoAPagar;
                        // Se utiliza round por un problema de presicion de la maquina ej: 828.65 - 614.0 = 214.64999...
                        monto = Math.round(monto * 100.0) / 100.0;
                        nuevoPago.setMonto(saldoAPagar);
                    } else {
                        nuevoPago.setMonto(monto);
                        monto = 0.0;
                    }
                    this.guardar(nuevoPago);
                }
            }
        } else {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pago_mayorADeuda_monto"));
        }
    }

    @Override
    public void validarOperacion(Pago pago) {
        //Requeridos
        if (pago.getMonto() <= 0) {
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
            if (Utilidades.round(pago.getMonto(), 2) > Utilidades.round(this.getSaldoAPagarFactura(pago.getFactura().getId_Factura()), 2)) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_pago_mayorADeuda_monto"));
            }
        }
    }

}
