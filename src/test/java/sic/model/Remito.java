package sic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.domain.CategoriaIVA;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Remito {

  private long idRemito;
  private LocalDateTime fecha;
  private long serie;
  private long nroRemito;
  private Long idCliente;
  private String nombreFiscalCliente;
  private String nroDeCliente;
  private CategoriaIVA categoriaIVACliente;
  private long idSucursal;
  private String nombreSucursal;
  private long idUsuario;
  private String nombreUsuario;
  private long idTransportista;
  private String nombreTransportista;
  private String detalleEnvio;
  private BigDecimal costoDeEnvio;
  private BigDecimal totalFacturas;
  private BigDecimal total;
  private BigDecimal pesoTotalEnKg;
  private BigDecimal volumenTotalEnM3;
  private BigDecimal cantidadDeBultos;
  private String observaciones;
}
