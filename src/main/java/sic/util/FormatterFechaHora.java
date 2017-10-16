package sic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FormatterFechaHora extends SimpleDateFormat {

    public static final String FORMATO_FECHAHORA_INTERNACIONAL = "yyyy/MM/dd HH:mm:ss";
    public static final String FORMATO_FECHA_INTERNACIONAL = "yyyy/MM/dd";
    public static final String FORMATO_HORA_INTERNACIONAL = "HH:mm:ss";
    public static final String FORMATO_FECHA_HISPANO = "dd/MM/yyyy";
    public static final String FORMATO_FECHAHORA_HISPANO = "dd/MM/yyyy HH:mm:ss";
    public static final String FORMATO_FECHAHORA_LETRAS = "EEE, d MMM yyyy HH:mm";

    public FormatterFechaHora(String formato) {
        this.applyPattern(formato);
    }

    public boolean esFechaHoraValida(String cadenaFecha) {
        try {
            this.setLenient(false);
            this.parse(cadenaFecha);

        } catch (ParseException ex) {
            return false;
        }
        return true;
    }

}
