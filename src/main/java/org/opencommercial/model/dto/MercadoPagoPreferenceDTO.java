package org.opencommercial.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.config.Views;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Comprador.class)
public class MercadoPagoPreferenceDTO {

    private String id;
    private String initPoint;
}
