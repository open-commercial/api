package sic.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utilidades {

    /**
     * Convierte un caracter de minusculas a mayusculas
     *
     * @param caracter Caracter para ser convertido
     * @return Devuelve el caracter ya convertido a mayusculas
     */
    public static char convertirAMayusculas(char caracter) {
        if ((caracter >= 'a' && caracter <= 'z') || caracter == 'ñ') {
            return (char) (((int) caracter) - 32);
        } else {
            return caracter;
        }
    }
    
    /**
     * Encripta el password con MD5
     *
     * @param password String a ser encriptado.
     * @return String encriptado con MD5.
     */
    public static String encriptarConMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }

    /**
     * Convierte el archivo en un array de bytes.
     *
     * @param archivo Archivo a ser convertido.
     * @return Array de byte representando al archivo.
     * @throws java.io.IOException
     */
    public static byte[] convertirFileIntoByteArray(File archivo) throws IOException {
        byte[] bArchivo = new byte[(int) archivo.length()];
        FileInputStream fileInputStream = new FileInputStream(archivo);
        fileInputStream.read(bArchivo);
        fileInputStream.close();
        return bArchivo;
    }
    
    public static ByteArrayInputStream convertirByteArrayToInputStream(byte[] byteArray) {
     return new ByteArrayInputStream(byteArray);
    }

    /**
     * Trunca los decimales de un double, segun la cantidad que uno requiera
     *
     * @param valor para ser truncado
     * @param cantidadDecimales cantidad de decimales que debe mantener
     * @return numero truncado
     */
    public static double truncarDecimal(double valor, int cantidadDecimales) {
        if (valor > 0) {
            return (new BigDecimal(String.valueOf(valor)).setScale(cantidadDecimales, BigDecimal.ROUND_FLOOR)).doubleValue();
        } else {
            return (new BigDecimal(String.valueOf(valor)).setScale(cantidadDecimales, BigDecimal.ROUND_CEILING)).doubleValue();
        }
    }

}
