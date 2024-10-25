package sic.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import sic.exception.ServiceException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Locale;

@Component
public class EncryptUtils {

  @Value("${AES_PRIVATE_KEY}")
  private String privateKey;

  @Value("${AES_INIT_VECTOR}")
  private String initVector;

  private final MessageSource messageSource;

  @Autowired
  public EncryptUtils(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String encryptWithAES(String valueToEncrypt) throws GeneralSecurityException {
    IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(1, skeySpec, iv);
    byte[] encrypted = cipher.doFinal(valueToEncrypt.getBytes());
    return DatatypeConverter.printBase64Binary(encrypted);
  }

  public String decryptWithAES(String encryptedValue) throws GeneralSecurityException {
    IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(2, skeySpec, iv);
    byte[] original = cipher.doFinal(DatatypeConverter.parseBase64Binary(encryptedValue));
    return new String(original);
  }

  public String encryptWithMD5(String valueToEncrypt) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] array = md.digest(valueToEncrypt.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte anArray : array) {
        sb.append(Integer.toHexString((anArray & 0xFF) | 0x100), 1, 3);
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new ServiceException(
              messageSource.getMessage("mensaje_encriptacion_no_disponible", null, Locale.getDefault()),
              ex);
    }
  }
}
