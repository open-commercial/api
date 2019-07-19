package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificacionMercadoPagoDTO {

  private String id;
  private boolean live_mode;
  private String type;
  //La fecha enviada tiene el siguiente formato -> 2017-03-10 02:02:02 +0000 UTC. y no serializa a java.util.Date
  private String date_created;
  private String application_id;
  private String user_id;
  private String version;
  private String api_version;
  private String action;
  private Data data;

  @lombok.Data
  @AllArgsConstructor
  @NoArgsConstructor
  public class Data {
    private String id;
  }
}
