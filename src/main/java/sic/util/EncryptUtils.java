package sic.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class EncryptUtils {

  private EncryptUtils() {}

  public static String encryptWhitAES(String valor, String initVectorValue, String key)
      throws GeneralSecurityException {

    IvParameterSpec iv = new IvParameterSpec(initVectorValue.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(1, skeySpec, iv);

    byte[] encrypted = cipher.doFinal(valor.getBytes());

    return DatatypeConverter.printBase64Binary(encrypted);
  }

  public static String decryptWhitAES(String valorEncriptado, String initVectorValue, String key)
      throws GeneralSecurityException {
    IvParameterSpec iv = new IvParameterSpec(initVectorValue.getBytes(StandardCharsets.UTF_8));
    SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(2, skeySpec, iv);

    byte[] original = cipher.doFinal(DatatypeConverter.parseBase64Binary(valorEncriptado));
    return new String(original);
  }

  public static String encryptWhitSHA(String passwordToHash) throws NoSuchAlgorithmException {
    String generatedPassword = null;
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
    byte[] salt = new byte[16];
    sr.nextBytes(salt);
    md.update(salt);
    byte[] bytes = md.digest(passwordToHash.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
    }
    generatedPassword = sb.toString();
    return generatedPassword;
  }
}
