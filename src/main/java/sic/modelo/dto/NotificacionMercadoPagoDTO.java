package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificacionMercadoPagoDTO {

  private String id;
  private boolean live_mode;
  private String type;
  private Date date_created;
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
