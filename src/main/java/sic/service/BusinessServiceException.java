package sic.service;

public class BusinessServiceException extends ServiceException {

    public BusinessServiceException() {
        super();
    }

    public BusinessServiceException(String mensaje) {
        super(mensaje);
    }

    public BusinessServiceException(Throwable causa) {
        super(causa);
    }

    public BusinessServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}