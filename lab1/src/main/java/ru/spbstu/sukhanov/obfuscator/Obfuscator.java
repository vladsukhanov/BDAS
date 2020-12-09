package ru.spbstu.sukhanov.obfuscator;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

@Component
public class Obfuscator {

    private static final String SOURCE = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-=_+[]{}\\|;':\",.?`~";
    private static final String TARGET = "Q~5b@cg$A8^Zd*ef)WS.0st9?GD\"C6:RF|VT[lm{no+BY-4HN=U3}xy]zJ\\ 2;MI'1p_qrKO,7L(Pa&hi%jk#uv!XE`w";

    public String obfuscate(final String source) {
        return processXML(source, this::obfuscateString);
    }

    public String unobfuscate(final String obfuscatedSource) {
        return processXML(obfuscatedSource, this::unobfuscateString);
    }

    public String obfuscateString(final String s) {
        return process(s, SOURCE, TARGET);
    }

    public String unobfuscateString(final String s) {
        return process(s, TARGET, SOURCE);
    }

    public static String process(final String s, final String source, final String target) {
        StringBuilder result= new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int index=source.indexOf(c);
            result.append(target.charAt(index));
        }
        return result.toString();
    }

    private String processXML(final String xml,
                              final UnaryOperator<String> elementProcessor) {
        try {
            final var doc = getDocument(xml);
            final var root = doc.getFirstChild();
            final var queue = new ArrayDeque<Node>();
            queue.addLast(root);
            while (!queue.isEmpty()) {
                queue.addAll(processNode(queue.pollFirst(), elementProcessor));
            }
            return convertToXMLString(doc);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Document getDocument(final String source)
            throws ParserConfigurationException, IOException, SAXException {
        final var docFactory = DocumentBuilderFactory.newInstance();
        final var docBuilder = docFactory.newDocumentBuilder();
        final var doc = docBuilder.parse(new InputSource(new StringReader(source)));
        doc.normalizeDocument();
        return doc;
    }

    private String convertToXMLString(final Document doc) throws TransformerException {
        final var transformerFactory = TransformerFactory.newInstance();
        final var transformer = transformerFactory.newTransformer();
        final var writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private List<Node> processNode(final Node node,
                                   final UnaryOperator<String> elementProcessor) {
        processAttrs(node, elementProcessor);
        final var childNodes = node.getChildNodes();
        final var elementNodes = new ArrayList<Node>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final var childNode = childNodes.item(i);
            final var nodeType = childNode.getNodeType();
            if (nodeType == Node.TEXT_NODE) {
                final var text = childNode.getTextContent();
                if (!text.isBlank()) {
                    childNode.setTextContent(elementProcessor.apply(text));
                }
            } else if (nodeType == Node.ELEMENT_NODE) {
                processAttrs(childNode, elementProcessor);
                elementNodes.add(childNode);
            }
        }
        return elementNodes;
    }

    private void processAttrs(final Node node,
                              final UnaryOperator<String> attrProcessor) {
        final var attrs = node.getAttributes();
        if (attrs != null) {
            for (int j = 0; j < attrs.getLength(); j++) {
                final var attr = attrs.item(j);
                attr.setTextContent(attrProcessor.apply(attr.getTextContent()));
            }
        }
    }
}
