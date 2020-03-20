package sic.utils;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Movimiento;
import sic.modelo.TipoDeEnvio;
import sic.util.EncryptUtils;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations="classpath:application.properties")
public class EncryptUtilsTest {

    @Value("${SIC_PRIVATE_KEY}")
    private String privateKey;

    @Value("${SIC_INIT_VECTOR}")
    private String initVector;

  @Test
  void shouldEncriptarAndDesencriptarString() throws GeneralSecurityException {
    String stringParaEncriptar =
        "{ \""
            + 1L
            + "\": "
            + 1L
            + " , \"idSucursal\": "
            + 2L
            + " , \"tipoDeEnvio\": "
            + TipoDeEnvio.RETIRO_EN_SUCURSAL
            + " , \"movimiento\": "
            + Movimiento.PEDIDO
            + "}";
    String jsonParaEncriptar =
        new JsonParser().parse(stringParaEncriptar).getAsJsonObject().toString();
    String stringEncriptado =
        EncryptUtils.encryptWhitAES(jsonParaEncriptar, initVector, privateKey);
    assertEquals(
        jsonParaEncriptar, EncryptUtils.decryptWhitAES(stringEncriptado, initVector, privateKey));
  }
}
