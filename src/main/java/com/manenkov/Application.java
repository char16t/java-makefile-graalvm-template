package com.manenkov;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Application {

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    public static void main(final String[] args) throws Exception {
        try {
            new Application().execute();
        } catch (final Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    void execute() throws Exception {
        validate("/application.xml", "/application.xsd");

        try (final InputStream dataInputStream = getClass().getResourceAsStream("/application.xml")) {
                final DocumentBuilder builder = factory.newDocumentBuilder();
                final Document document = builder.parse(dataInputStream);
                final Element root = document.getDocumentElement();
                final NodeList nestedNodes = root.getElementsByTagName("hello");
                final Node nestedNode = nestedNodes.item(0);
                System.out.println(nestedNode.getTextContent());
        }

        try (final InputStream dataInputStream = getClass().getResourceAsStream("/application.xml")) {
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.parse(dataInputStream);
            final XPathFactory xpathFactory = XPathFactory.newInstance();
            final XPath xpath = xpathFactory.newXPath();
            final XPathExpression expr = xpath.compile("//application/hello/text()");
            final String result = expr.evaluate(document);
            System.out.println(result);
        }

        System.exit(0);
    }

    void validate(final String xmlPath, final String xsdPath) throws Exception {
        try (final InputStream dataInputStream = getClass().getResourceAsStream(xmlPath);
            final InputStream schemaInputStream = getClass().getResourceAsStream(xsdPath)) {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(new StreamSource(schemaInputStream));
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(dataInputStream));
        }
    }
}
