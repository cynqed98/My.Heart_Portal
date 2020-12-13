package com.example.myheartportal;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecureEncryption
{
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
}
