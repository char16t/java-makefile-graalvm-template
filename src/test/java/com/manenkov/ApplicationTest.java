package com.manenkov;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class ApplicationTest {
    public static void main(final String[] args) throws Exception {
        new Application(); // TODO: Only for test. Remove
        new ApplicationTest().execute();
    }

    void execute() throws Exception {
        validate("/application.xml", "/application.xsd");
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
