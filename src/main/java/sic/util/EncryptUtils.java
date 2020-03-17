package sic.util;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class EncryptUtils {

  private EncryptUtils() {}

  public static String encryptWhitRSA(String valor, String publicKey)
      throws GeneralSecurityException {
    KeyFactory kf = KeyFactory.getInstance("RSA");
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(
        Cipher.ENCRYPT_MODE, kf.generatePublic(new X509EncodedKeySpec(stringToBytes(publicKey))));
    return bytesToString(cipher.doFinal(valor.getBytes()));
  }

  public static String decryptWhitRSA(String valor, String privateKey)
      throws GeneralSecurityException {
    KeyFactory kf = KeyFactory.getInstance("RSA");
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(
        Cipher.DECRYPT_MODE,
        kf.generatePrivate(new PKCS8EncodedKeySpec(stringToBytes(privateKey))));
    return new String(cipher.doFinal(stringToBytes(valor)));
  }

  public static byte[] stringToBytes(String s) {
    byte[] b2 = new BigInteger(s, 36).toByteArray();
    return Arrays.copyOfRange(b2, 1, b2.length);
  }

  public static String bytesToString(byte[] b) {
    byte[] b2 = new byte[b.length + 1];
    b2[0] = 1;
    System.arraycopy(b, 0, b2, 1, b.length);
    return new BigInteger(b2).toString(36);
  }

  public static String encryptWhitSHA(String passwordToHash) {
    String generatedPassword = null;
    try {
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
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return generatedPassword;
  }
}
