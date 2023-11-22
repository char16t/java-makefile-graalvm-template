package com.manenkov;

import java.io.InputStream;
import java.util.Properties;

public class Application {
    public static void main(final String[] args) throws Exception {
        try {
            new Application().execute();
            System.exit(0);
        } catch (final Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    void execute() throws Exception {
        try (final InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            System.out.println(properties.getProperty("hello"));
        }
    }
}
