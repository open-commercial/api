package sic.modelo.dto;


import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Encargado.class)
public class ValueChangeDTO {

    String propertyName; // renglon producto
    String beforeValue; // poxi 4
    String afterValue; // poxi 2
    String prettyPrint;
}
