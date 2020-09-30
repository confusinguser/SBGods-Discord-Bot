package com.confusinguser.sbgods.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class EncryptionUtil {
    private final Cipher cipher;
    private final Cipher aesCipher;
    private final Map<InetAddress, PrivateKey> storedKeys = new HashMap<>();

    public EncryptionUtil() {
        Cipher cipher1;
        Cipher aesCipher;
        try {
            cipher1 = Cipher.getInstance("RSA");
            aesCipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            cipher1 = null;
            aesCipher = null;
            e.printStackTrace();
        }
        this.cipher = cipher1;
        this.aesCipher = aesCipher;
    }

    public PrivateKey getPrivate(byte[] keyBytes) {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PublicKey getPublic(byte[] keyBytes) {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String encryptText(String msg, PublicKey key) {
        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeBase64String(cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decryptText(String msg, PrivateKey key) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        JsonObject data = JsonParser.parseString(msg).getAsJsonObject();
        SecretKey aesKey;
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedKey = cipher.doFinal(java.util.Base64.getDecoder().decode(data.get("AESKey").getAsString().getBytes(StandardCharsets.UTF_8)));
        aesKey = new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES");
        this.aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        return new String(this.aesCipher.doFinal(data.get("data").getAsString().getBytes(StandardCharsets.UTF_8)));
    }

    public KeyPair generateKeyPair(int length) {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        keyGen.initialize(length);
        return keyGen.generateKeyPair();
    }

    public KeyPair generateKeyPair() {
        return generateKeyPair(2048);
    }

    public PrivateKey getKeyForIP(InetAddress address) {
        return storedKeys.get(address);
    }

    public void assignKeyForIP(InetAddress address, PrivateKey key) {
        storedKeys.put(address, key);
    }
}
