package ru.spbstu.sukhanov.crypto;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Component
public class BouncyCastleCrypto {

    private final X509Certificate certificate;

    private final PrivateKey privateKey;

    static {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    public BouncyCastleCrypto() throws Exception {
        final var certFactory = CertificateFactory.getInstance("X.509", "BC");
        this.certificate = (X509Certificate) certFactory.generateCertificate(new FileInputStream("src/main/resources/public.cer"));
        final var keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("src/main/resources/private.p12"), "password".toCharArray());
        this.privateKey = (PrivateKey) keyStore.getKey("baeldung", "password".toCharArray());
    }


    public byte[] encryptData(final byte[] data) throws Exception {
        byte[] encryptedData = null;
        if (null != data && null != certificate) {
            CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
            JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(certificate);
            cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
            CMSTypedData msg = new CMSProcessableByteArray(data);
            OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();
            CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(msg, encryptor);
            encryptedData = cmsEnvelopedData.getEncoded();
        }
        return encryptedData;
    }

    public byte[] decryptData(final byte[] encryptedData) throws Exception {
        byte[] decryptedData = null;
        if (null != encryptedData && null != privateKey) {
            CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);
            Collection<RecipientInformation> recip = envelopedData.getRecipientInfos().getRecipients();
            KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recip.iterator().next();
            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(privateKey);
            decryptedData = recipientInfo.getContent(recipient);
        }
        return decryptedData;
    }

    public byte[] signData(final byte[] data) throws Exception {
        byte[] signedMessage;
        List<X509Certificate> certList = new ArrayList<>();
        CMSTypedData cmsData = new CMSProcessableByteArray(data);
        certList.add(certificate);
        Store certs = new JcaCertStore(certList);
        CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
        cmsGenerator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(contentSigner, certificate));
        cmsGenerator.addCertificates(certs);
        CMSSignedData cms = cmsGenerator.generate(cmsData, true);
        signedMessage = cms.getEncoded();
        return signedMessage;
    }

    public boolean verifySignData(final byte[] signedData) throws Exception {
        ByteArrayInputStream bIn = new ByteArrayInputStream(signedData);
        ASN1InputStream aIn = new ASN1InputStream(bIn);
        CMSSignedData s = new CMSSignedData(ContentInfo.getInstance(aIn.readObject()));
        aIn.close();
        bIn.close();
        Store certs = s.getCertificates();
        SignerInformationStore signers = s.getSignerInfos();
        Collection<SignerInformation> c = signers.getSigners();
        SignerInformation signer = c.iterator().next();
        Collection<X509CertificateHolder> certCollection = certs.getMatches(signer.getSID());
        Iterator<X509CertificateHolder> certIt = certCollection.iterator();
        X509CertificateHolder certHolder = certIt.next();
        return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certHolder));
    }
}
