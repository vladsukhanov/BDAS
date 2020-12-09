package ru.spbstu.sukhanov.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static ru.spbstu.sukhanov.crypto.TestUtil.*;

@SpringBootTest(classes = BouncyCastleCryptoTestConfiguration.class)
class BouncyCastleCryptoTest {

    @Autowired
    private BouncyCastleCrypto bouncyCastleCrypto;

    @Test
    public void encryptData() throws Exception {
        assertNotEquals(SECRETE_MSG, new String(getEncryptedData(bouncyCastleCrypto), StandardCharsets.UTF_8));
    }

    @Test
    public void decryptData() throws Exception {
        assertEquals(SECRETE_MSG, new String(getDecryptedData(bouncyCastleCrypto), StandardCharsets.UTF_8));
    }

    @Test
    public void signData() throws Exception {
        assertNotEquals(SECRETE_MSG.getBytes(), getSignedData(bouncyCastleCrypto));
    }

    @Test
    public void verifyData() throws Exception {
        assertTrue(bouncyCastleCrypto.verifySignData(getSignedData(bouncyCastleCrypto)));
    }
}