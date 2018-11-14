package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReciboDTO implements Serializable {

  private Long idRecibo = 0L;
  private long serie = 1L;
  private long nroRecibo = 1L;
  private Date fecha;
  private boolean eliminado = false;
  private String concepto = "Recibo Test";
  private String nombreFormaDePago = "Efectivo";
  private String nombreEmpresa = "Globo De Oro";
  private String nombreFiscalCliente = "Construcciones S.A.";
  private String razonSocialProveedor = "";
  private String nombreUsuario = "test";
  private double monto = 15000;
}
