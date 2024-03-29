package sic.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.util.EncryptUtils;
import java.security.GeneralSecurityException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EncryptUtils.class})
@TestPropertySource(locations = "classpath:application.properties")
class EncryptUtilsTest {

  @Autowired EncryptUtils encryptUtils;

  @Test
  void shouldEncriptarAndDesencriptarString() throws GeneralSecurityException {
    String valorParaEncriptar = "TestingAES!.@ABC";
    String valorEncriptado = encryptUtils.encryptWithAES(valorParaEncriptar);
    assertNotEquals(valorParaEncriptar, valorEncriptado);
    String valorDesencriptado = encryptUtils.decryptWithAES(valorEncriptado);
    assertEquals(valorParaEncriptar, valorDesencriptado);
  }
}
