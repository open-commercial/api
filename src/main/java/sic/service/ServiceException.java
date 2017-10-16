package sic.service;

public class ServiceException extends RuntimeException {

    public ServiceException() {
        super();
    }

    public ServiceException(String mensaje) {
        super(mensaje);
    }

    public ServiceException(Throwable causa) {
        super(causa);
    }

    public ServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
