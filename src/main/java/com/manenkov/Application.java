package com.manenkov;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Application {
    public static void main(final String[] args) throws Exception {
        try {
            new Application().execute();
        } catch (final Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    void execute() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

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

        try (
            final InputStream inputStream = getClass().getResourceAsStream("/input.xml");
            final InputStream stylesheetStream = getClass().getResourceAsStream("/stylesheet.xsl");
        ) {
            final StreamSource xmlSource = new StreamSource(inputStream);
            final StreamSource xsltSource = new StreamSource(stylesheetStream);

            final StringWriter stringWriter = new StringWriter();
            final StreamResult result = new StreamResult(stringWriter);

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer(xsltSource);
            transformer.transform(xmlSource, result);

            final String output = stringWriter.toString();
            
            // System.out.println(output);
            final Person p = parseXml(output, Person.class);
            System.out.println(p.getSubp().getNested());
        }

        System.exit(0);
    }

    public static <T> T parseXml(String xml, Class<T> clazz) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new InputSource(new StringReader(xml)));

        final Element rootElement = document.getDocumentElement();
        final NodeList childNodes = rootElement.getChildNodes();

        final T instance = clazz.getDeclaredConstructor().newInstance();

        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                final Element childElement = (Element) childNode;
                final String fieldName = childElement.getTagName();
                final String fieldValue = childElement.getTextContent();

                final Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                // TODO: Implement other types
                if (field.getType() == Integer.class) {
                    field.set(instance, Integer.parseInt(fieldValue));
                } else if (field.getType() == Float.class) {
                    field.set(instance, Float.parseFloat(fieldValue));
                } else if (field.getType() == Double.class) {
                    field.set(instance, Double.parseDouble(fieldValue));
                } else if (field.getType() == String.class) {
                    field.set(instance, fieldValue);
                } else {
                    // Handle nested objects

                    final StringWriter stringWriter = new StringWriter();
                    final StreamResult result = new StreamResult(stringWriter);

                    final DOMSource source = new DOMSource(childNode);
                    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    final Transformer transformer = transformerFactory.newTransformer();
                    
                    transformer.transform(source, result);
                    
                    final String output = stringWriter.toString();

                    final Class<?> nestedClass = (Class<?>) field.getType();
                    final Object nestedInstance = parseXml(output, nestedClass);
                    field.set(instance, nestedInstance);
                }
            }
        }

        return instance;
    }
}
