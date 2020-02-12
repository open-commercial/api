package sic.model;

import sic.modelo.CategoriaIVA;
import sic.modelo.dto.UbicacionDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cliente implements Serializable {

    private long idCliente;;
    private String nroCliente;
    private String nombreFiscal;
    private String nombreFantasia;
    private CategoriaIVA categoriaIVA;
    private Long idFiscal;
    private UbicacionDTO ubicacionFacturacion;
    private UbicacionDTO ubicacionEnvio;
    private String email;
    private String telefono;
    private String contacto;
    private LocalDateTime fechaAlta;
    private Long idViajante;
    private String nombreViajante;
    private Long idCredencial;
    private String nombreCredencial;
    private boolean predeterminado;
    private BigDecimal saldoCuentaCorriente;
    private BigDecimal montoCompraMinima;
    private String detalleUbicacionDeFacturacion;
    private String detalleUbicacionDeEnvio;
}
