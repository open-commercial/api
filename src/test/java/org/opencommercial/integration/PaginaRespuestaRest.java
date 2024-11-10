package org.opencommercial.integration;

import lombok.Data;

import java.util.List;

@Data
class PaginaRespuestaRest<T> {
    
    private List<T> content;
    private boolean first;
    private boolean last;
    private int totalPages;
    private int totalElements;
    private int numberOfElements;
    private int size;
    private int number;
    private Ordenamiento sort;
}

@Data
class Ordenamiento {

    private boolean sorted;
    private boolean unsorted;
    private boolean empty;
}
