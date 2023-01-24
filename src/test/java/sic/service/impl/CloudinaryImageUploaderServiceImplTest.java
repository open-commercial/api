package sic.service.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.ServiceException;
import sic.service.IImageUploaderService;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CloudinaryImageUploaderServiceImpl.class, MessageSource.class})
@TestPropertySource(locations = "classpath:application.properties")
class CloudinaryImageUploaderServiceImplTest {

  @MockBean
  MessageSource messageSource;

  @Autowired
  IImageUploaderService imageUploaderService;

  @Test
  @Disabled
  void shouldNotValidarUrl() {
    assertThrows(ServiceException.class, () -> imageUploaderService.isUrlValida(""));
    assertThrows(
            ServiceException.class,
            () -> imageUploaderService.isUrlValida(
                    "http://res.cloudinary.com/hcpi6qoun/image/upload/v1608594469/Producto2.jpg"));
    verify(messageSource, times(2))
        .getMessage(eq("mensaje_url_imagen_no_valida"), any(), eq(Locale.getDefault()));
  }
}
