package sic.modelo.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO {
    
    private Long id_Producto = 0L;
    private String codigo = "ABC123";
    private String descripcion = "Cinta adhesiva doble faz 3M";
    private double cantidad = 10;
    private double cantMinima = 2;    
    private double ventaMinima = 0;
    private String nombreMedida = "Unidad";
    private double precioCosto = 100;
    private double ganancia_porcentaje = 50;
    private double ganancia_neto = 50;
    private double precioVentaPublico = 150;
    private double iva_porcentaje = 21;
    private double iva_neto = 31.5;
    private double impuestoInterno_porcentaje = 0;
    private double impuestoInterno_neto = 0;
    private double precioLista = 181.5;    
    private String nombreRubro = "Ferreteria";
    private boolean ilimitado = false;
    private Date fechaUltimaModificacion = new Date(1463540400000L); // 18-05-2016
    private String estanteria = "A";
    private String estante = "1";
    private String razonSocialProveedor = "Abrasol";
    private String nota = "Cumple con las normas ISO9001";
    private Date fechaAlta = new Date(1458010800000L); // 15-03-2016;
    private Date fechaVencimiento = new Date(1597892400000L); // 20-08-2020
    private String nombreEmpresa = "Globo De Oro";
    private boolean eliminado = false;
    
}
