package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.Movimiento;
import sic.modelo.TipoDeOperacion;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PhotoVideoUploaderImpl.class, MessageSource.class})
class PhotoVideoUploaderImplTest {

  @MockBean MessageSource messageSource;

  @Autowired PhotoVideoUploaderImpl photoVideoUploader;

  @Test
  void shouldNotValidarUrl() {
    assertThrows(ServiceException.class, () -> photoVideoUploader.isUrlValida(""));
    assertThrows(
        ServiceException.class,
        () ->
            photoVideoUploader.isUrlValida(
                "http://res.cloudinary.com/hcpi6qoun/image/upload/v1608594469/Producto2.jpg"));
    verify(messageSource, times(2))
        .getMessage(eq("mensaje_url_imagen_no_valida"), any(), eq(Locale.getDefault()));
  }
}
