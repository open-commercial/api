package sic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.*;
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
  private TipoDeComprobante tipoComprobante;
  private Long idCliente;
  private String nombreFiscalCliente;
  private String nroDeCliente;
  private CategoriaIVA categoriaIVACliente;
  private long idSucursal;
  private String nombreSucursal;
  private long idUsuario;
  private String nombreUsuario;
  private String detalleEnvio;
  private BigDecimal total;
  private boolean contraEntrega;
}
