package sic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReciboDTO implements Serializable {

  private Long idRecibo;
  private long serie;
  private long nroRecibo;
  private LocalDateTime fecha;
  private boolean eliminado;
  private String concepto;
  private long idFormaDePago;
  private String nombreFormaDePago;
  private long idSucursal;
  private String nombreSucursal;
  private Long idCliente;
  private String nombreFiscalCliente;
  private Long idProveedor;
  private String razonSocialProveedor;
  private String nombreUsuario;
  private Long idViajante;
  private String nombreViajante;
  private double monto;
}
