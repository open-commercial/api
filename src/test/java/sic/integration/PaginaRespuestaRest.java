package sic.integration;

import java.util.List;
import lombok.Data;

@Data
public class PaginaRespuestaRest<T> {
    
    private List<T> content;
    private boolean first;
    private boolean last;
    private int totalPages;
    private int totalElements;
    private int numberOfElements;
    private int size;
    private int number;
    private List<Ordenamiento> sort;
}

@Data
class Ordenamiento {

    private String direction;
    private String property;
    private boolean ignoreCase;
    private String nullHandling;
    private boolean ascending;
    private boolean descending;
}
