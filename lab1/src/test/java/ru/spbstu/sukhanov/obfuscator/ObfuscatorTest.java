package ru.spbstu.sukhanov.obfuscator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ObfuscatorTestConfiguration.class)
public class ObfuscatorTest {

    @Autowired
    private Obfuscator obfuscator;

    private String xmlSource;

    private String obfuscatedXml;

    @BeforeEach
    public void init() throws IOException {
        final var classLoader = getClass().getClassLoader();
        final var file = new File(classLoader.getResource("test.xml").getFile()).toPath();
        xmlSource = Files.readString(file);
        obfuscatedXml = obfuscator.obfuscate(xmlSource);
    }

    @Test
    public void obfuscate() throws Exception {
        assertFalse(compareXMLStrings(xmlSource, obfuscatedXml));
        saveXMLStringAsFile(obfuscatedXml, "src/test/resources/obf.xml");
    }

    @Test
    public void unobfuscate() throws Exception {
        String unobfuscated = obfuscator.unobfuscate(obfuscatedXml);
        assertTrue(compareXMLStrings(xmlSource, unobfuscated));
        saveXMLStringAsFile(unobfuscated, "src/test/resources/unobf.xml");
    }

    private boolean compareXMLStrings(final String xml1, final String xml2) throws Exception {
        final var dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        final var db = dbf.newDocumentBuilder();

        final var doc1 = db.parse(new InputSource(new StringReader(xml1)));
        doc1.normalizeDocument();

        final var doc2 = db.parse(new InputSource(new StringReader(xml2)));
        doc2.normalizeDocument();

        return doc1.isEqualNode(doc2);
    }

    private void saveXMLStringAsFile(String xml, String fileName) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(xml);
        writer.close();
    }
}
