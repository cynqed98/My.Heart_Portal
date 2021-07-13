package com.example.myheartportal;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecureEncryption
{
    //***********************************************************
    //AES CBC MODE --------------****
    //Enable or disable by Ctrl + Shift + / on the whole code below until ENDcode

    /*private final String algorithm = "AES/CBC/PKCS5Padding";
    private final int iterationCount = 1000;
    private final int keyLength = 256;
    int saltLength = keyLength / 8;

    public String encryptData(String Data, String password) throws Exception
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(algorithm);
        byte[] iv = new byte[cipher.getBlockSize()];
        random.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        byte[] ciphertext = cipher.doFinal(Data.getBytes("UTF-8"));
        String saltStr = Base64.encodeToString(salt, Base64.DEFAULT);
        String ivStr = Base64.encodeToString(iv, Base64.DEFAULT);
        String cipherStr = Base64.encodeToString(ciphertext, Base64.DEFAULT);
        String concatStr = saltStr + "]" + ivStr + "]" + cipherStr;
        return concatStr;
    }

    public String decryptData(String encData, String password) throws Exception
    {
        String[] fields = encData.split("]");
        byte[] salt = Base64.decode(fields[0], Base64.DEFAULT);
        byte[] iv = Base64.decode(fields[1], Base64.DEFAULT);
        byte[] cipherBytes = Base64.decode(fields[2], Base64.DEFAULT);

        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(algorithm);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        byte[] plaintext = cipher.doFinal(cipherBytes);
        String plainStr = new String(plaintext, "UTF-8");

        return plainStr;
    }*/
    //---------------CBC-mode ENDcode****

    //***********************************************************

    //AES EBC MODE (high performance than CBC mode------**
    //Enable or disable by Ctrl + Shift + / on the whole code below until ENDcode

    private final String algorithm = "AES";
    public String encryptData(String Data, String password) throws Exception
    {
        SecretKeySpec key = generateKey(password);
        @SuppressLint("GetInstance") Cipher c = Cipher.getInstance(algorithm);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }
    public SecretKeySpec generateKey(String password) throws Exception
    {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }
    public String decryptData(String encData, String password) throws Exception
    {
        SecretKeySpec key = generateKey(password);
        @SuppressLint("GetInstance") Cipher c = Cipher.getInstance(algorithm);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(encData, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);
    }
    //-------------EBC-mode ENDcode****
}
