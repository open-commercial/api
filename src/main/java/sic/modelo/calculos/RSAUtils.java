package sic.modelo.calculos;

import sic.exception.BusinessServiceException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class RSAUtils {

  public static String encrypt(String plainText) {
    try {
      String publicKey =
          "dmsxxhetzejiibz6z99xnjenpxzk8e9uu8kpj42b93gahr239tugclb0i9vob6cnvoe1qhgcba39c389tk90j0n6n5mpno5hw147pkfdzp7i3"
              + "pu54okmm5t3uvs1ktz9qjzio6gock2zj7h1tx5bi7zzg6o5j53721qu7dj3rdxovu6h7km5dazy3q7xd37tpptd33fewc8l3wibqg"
              + "1gxhkdtstcux8hcff69t4k91901cxuj25i56ht0ch";
      KeyFactory kf = KeyFactory.getInstance("RSA");
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(
          Cipher.ENCRYPT_MODE, kf.generatePublic(new X509EncodedKeySpec(stringToBytes(publicKey))));
      return bytesToString(cipher.doFinal(plainText.getBytes()));
    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeyException
        | BadPaddingException
        | IllegalBlockSizeException
        | InvalidKeySpecException ex) {
      throw new BusinessServiceException(ex.getMessage());
    }
  }

  public static String decrypt(String cipherTextArray) {
    try {
      String privateKey =
          "aftmhklqmrn01drhnjosgqbmj21zel7u5frf9mi8ziksx8fiszj3adbf5kz8o5ml0oyv3mzel93x9ybh7uhhx8irb9u9dvzzpqmhj7diqt0go"
              + "2rwgbvpawv7qhdz3mva9lcou3gkm76jk1o2vnuckk9yz7qyps4u8v9den9d2lnsx1w2zubu79ts7gw1y417rahlvvbbccwfgymjkv"
              + "76iyw5phpn5mnm33te5ghsm4rr5401t5g6d38ewkik3j7xj1x2svaeulyr0krp1o20x2oviadg0znqlj2s39mqlh33p10rbhgimn1"
              + "jme7e7rlhki2419dabzrg5zcdoxr8qxfppu2qzk8r45eijoopq223efsvr3saqz3wrgcrqyk1lwj7a6zioz5wyrixwnjp4jq7bxz1"
              + "6n78ebjb6kvtctiq02pffbpppqyqq1jpx5iwimby78qlbtmx53d3dvukvros4cwgor7snumbrff2ywicgbc87mw2ocljug95kjj78"
              + "4dhms7uwgpnqtonprj0ti0uwj06s953gp700485bos2ff8kulf69k59d0o4zjj1k67bx1k6iueuoh4g539hrloac4e7ffdroek1dn"
              + "cglbvsttzvoqeww208tdniafq0eeiw900k8k4mkwv8qc8uc9eejvisujuept3xs77cbvk9dlsco915w2704n51jsl1k3c17fzen33"
              + "j4tu722hrdvuqd0mvps21hu6b6jcmx86g6lu43f4yve864k1t6jjrea68zqv0zkm933bm1uxles0ij4opmxslk1c7du2vok3y2csr"
              + "g47aiea4iflel62e85zgw5bewskfazzn7wwt2bvtfnxu2ub2fyudqa60rgoatqfb3y943x12c4f9ur3h0pmxyxfi1utr5unww8yy0"
              + "suaskcxovfyzanzbzbz333y5d5wvw8mgwsbiwip933ljtfkvpggprfwoyk9ndfkywe";
      KeyFactory kf = KeyFactory.getInstance("RSA");
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(
          Cipher.DECRYPT_MODE,
          kf.generatePrivate(new PKCS8EncodedKeySpec(stringToBytes(privateKey))));
      return new String(cipher.doFinal(stringToBytes(cipherTextArray)));
    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeyException
        | BadPaddingException
        | IllegalBlockSizeException
        | InvalidKeySpecException ex) {
      return ex.getMessage();
    }
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
}
