package sic.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FormatterNumero extends DecimalFormat {

    public static String formatConRedondeo(Number n) {
        NumberFormat format = DecimalFormat.getInstance();
        format.setRoundingMode(RoundingMode.FLOOR);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format.format(n);
    }

}
