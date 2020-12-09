package ru.spbstu.sukhanov.crypto;

public final class TestUtil {

    public static final String SECRETE_MSG = "My password is qwerty";

    private TestUtil() {

    }

    public static byte[] getEncryptedData(BouncyCastleCrypto bouncyCastleCrypto) throws Exception {
        return bouncyCastleCrypto.encryptData(SECRETE_MSG.getBytes());
    }

    public static byte[] getDecryptedData(BouncyCastleCrypto bouncyCastleCrypto) throws Exception {
        return bouncyCastleCrypto.decryptData(getEncryptedData(bouncyCastleCrypto));
    }

    public static byte[] getSignedData(BouncyCastleCrypto bouncyCastleCrypto) throws Exception {
        return bouncyCastleCrypto.signData(SECRETE_MSG.getBytes());
    }
}
