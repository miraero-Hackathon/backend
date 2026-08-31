package org.jejuro.miraero.global.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class AccountNumberCrypto {

  private static final String ENCRYPTION_KEY_ENV = "ACCOUNT_NUMBER_ENCRYPTION_KEY";
  private static final int GCM_TAG_BIT_LENGTH = 128;
  private static final int IV_LENGTH = 12;
  private static final int ACCOUNT_HASH_LENGTH = 30;

  private final SecureRandom secureRandom = new SecureRandom();
  private final SecretKeySpec secretKeySpec;

  public AccountNumberCrypto() {
    this.secretKeySpec = new SecretKeySpec(resolveKey(), "AES");
  }

  //암호화
  public byte[] encrypt(String accountNumber) {
    try {
      byte[] iv = new byte[IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv));
      byte[] cipherText = cipher.doFinal(accountNumber.getBytes(StandardCharsets.UTF_8));

      return ByteBuffer.allocate(iv.length + cipherText.length)
          .put(iv)
          .put(cipherText)
          .array();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to encrypt account number.", e);
    }
  }


  //복호화
  public String decrypt(byte[] encryptedAccountNumber) {
    if (encryptedAccountNumber == null || encryptedAccountNumber.length <= IV_LENGTH) {
      throw new IllegalArgumentException("Invalid encrypted account number.");
    }

    try {
      ByteBuffer buffer = ByteBuffer.wrap(encryptedAccountNumber);
      byte[] iv = new byte[IV_LENGTH];
      buffer.get(iv);
      byte[] cipherText = new byte[buffer.remaining()];
      buffer.get(cipherText);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv));
      byte[] plainText = cipher.doFinal(cipherText);
      return new String(plainText, StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to decrypt account number.", e);
    }
  }

  public String hashForAccount(String accountNumber) {
    return hash(accountNumber).substring(0, ACCOUNT_HASH_LENGTH);
  }

  public String hashForMoneyBox(String accountNumber) {
    return hash(accountNumber);
  }

  public String mask(String accountNumber) {
    return accountNumber.substring(0, 4) + "*****" + accountNumber.substring(9);
  }

  private String hash(String accountNumber) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(accountNumber.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(hashed.length * 2);
      for (byte value : hashed) {
        builder.append(String.format("%02x", value));
      }
      return builder.toString();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to hash account number.", e);
    }
  }

  private byte[] resolveKey() {
    String configuredKey = System.getenv(ENCRYPTION_KEY_ENV);
    if (configuredKey == null || configuredKey.isBlank()) {
      return MessageDigestHolder.sha256(
          "miraero-local-account-number-key".getBytes(StandardCharsets.UTF_8));
    }

    byte[] decodedKey = decodeKey(configuredKey);
    if (decodedKey.length == 16 || decodedKey.length == 24 || decodedKey.length == 32) {
      return decodedKey;
    }
    return Arrays.copyOf(MessageDigestHolder.sha256(decodedKey), 32);
  }

  private byte[] decodeKey(String configuredKey) {
    try {
      return Base64.getDecoder().decode(configuredKey);
    } catch (IllegalArgumentException ignored) {
      return configuredKey.getBytes(StandardCharsets.UTF_8);
    }
  }

  private static class MessageDigestHolder {

    private static byte[] sha256(byte[] value) {
      try {
        return MessageDigest.getInstance("SHA-256").digest(value);
      } catch (GeneralSecurityException e) {
        throw new IllegalStateException("Failed to create AES key.", e);
      }
    }
  }
}
