package sic.util;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Validator {

    public static boolean esVacio(String campo) {
        if (campo == null) return true;
        return campo.equals("");
    }
    
    /**
     * Compara dos fechas teniendo en cuenta solo los dias sin hora
     * 
     * @param fechaAnterior
     * @param fechaSiguiente
     * @return -1 si la fechaAnterior esta antes de la fechaSiguiente, 0 si son iguales,
     * o 1 si la fechaAnterior esta despues de la fechaSiguiente
     */
    public static int compararDias(LocalDateTime fechaAnterior, LocalDateTime fechaSiguiente) {
        return fechaAnterior.compareTo(fechaSiguiente);
    }
    
    public static boolean tieneDuplicados(long[] array) {
        Set<Long> set = new HashSet<>();
        for (long i : array) {
            if (set.contains(i)) {
                return true;
            }
            set.add(i);
        }
        return false;
    }
}
