package org.opencommercial.util;

import jakarta.xml.bind.DatatypeConverter;
import org.opencommercial.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

@Component
public class EncryptUtils {

  @Value("${AES_PRIVATE_KEY}")
  private String privateKey;

  private final MessageSource messageSource;
  private static final String AES_CBC_PKCS5 = "AES/CBC/NoPadding";
  private static final String AES_ALGORITHM = "AES";
  private static final String MD5_ALGORITHM = "MD5";

  @Autowired
  public EncryptUtils(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String encryptWithAES(String valueToEncrypt) throws GeneralSecurityException {
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
    Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
    byte[] encrypted = cipher.doFinal(valueToEncrypt.getBytes(StandardCharsets.UTF_8));
    byte[] combined = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
    return DatatypeConverter.printBase64Binary(combined);
  }

  public String decryptWithAES(String encryptedValue) throws GeneralSecurityException {
    byte[] combined = DatatypeConverter.parseBase64Binary(encryptedValue);
    byte[] iv = new byte[16];
    byte[] encryptedBytes = new byte[combined.length - 16];
    System.arraycopy(combined, 0, iv, 0, 16);
    System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    SecretKeySpec skeySpec = new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
    Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
    byte[] original = cipher.doFinal(encryptedBytes);
    return new String(original, StandardCharsets.UTF_8);
  }

  public String encryptWithMD5(String valueToEncrypt) {
    try {
      MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
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
