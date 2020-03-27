package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

import static junit.framework.TestCase.assertFalse;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppTest.class)
public class MessageSourceImplTest {

  @Autowired private MessageSource messageSourceTest;

  @Test
  void shouldTestLongSinComaEnElRecuperoDeContrasenia() {
    assertFalse(
        messageSourceTest
            .getMessage(
                "mensaje_correo_recuperacion",
                new Object[] {"host", "passwordRecoveryKey", 12956L},
                Locale.ENGLISH)
            .contains("1,000"));
  }
}
