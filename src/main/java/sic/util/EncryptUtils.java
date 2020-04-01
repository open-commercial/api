package sic.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.*;

@Component
public class EncryptUtils {

  @Value("${SIC_AES_PRIVATE_KEY}")
  private String privateKey;

  @Value("${SIC_AES_INIT_VECTOR}")
  private String initVector;

  public String encryptWhitAES(String valorParaEncriptar) throws GeneralSecurityException {
    IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(1, skeySpec, iv);
    byte[] encrypted = cipher.doFinal(valorParaEncriptar.getBytes());
    return DatatypeConverter.printBase64Binary(encrypted);
  }

  public String decryptWhitAES(String valorEncriptado) throws GeneralSecurityException {
    IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(2, skeySpec, iv);
    byte[] original = cipher.doFinal(DatatypeConverter.parseBase64Binary(valorEncriptado));
    return new String(original);
  }
}
