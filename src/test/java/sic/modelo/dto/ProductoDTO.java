package sic.modelo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;

@Data
public class ProductoDTO {
    
    private Long id_Producto = 0L;
    private String codigo = "ABC123";
    private String descripcion = "Cinta adhesiva doble faz 3M";
    private double cantidad = 10;
    private double cantMinima = 2;    
    private double ventaMinima = 0;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String medida = "unidad";
    private double precioCosto = 100;
    private double ganancia_porcentaje = 50;
    private double ganancia_neto = 50;
    private double precioVentaPublico = 150;
    private double iva_porcentaje = 21;
    private double iva_neto = 31.5;
    private double impuestoInterno_porcentaje = 0;
    private double impuestoInterno_neto = 0;
    private double precioLista = 181.5;    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String rubro = "Ferreteria";
    private boolean ilimitado = false;
    private Date fechaUltimaModificacion = new Date(1463540400000L); // 18-05-2016
    private String estanteria = "A";
    private String estante = "1";
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String proveedor = "Abrasol";
    private String nota = "Cumple con las normas ISO";
    private Date fechaAlta = new Date(1458010800000L); // 15-03-2016;
    private Date fechaVencimiento = new Date(1597892400000L); // 20-08-2020
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String empresa = "Globo De Oro";
    private boolean eliminado = false;

    @Override
    public String toString() {
        return descripcion;
    }
    
}
