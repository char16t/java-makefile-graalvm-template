package com.manenkov.module1;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class Module1 {

    private final OutputStream output;

    public Module1(final OutputStream output) {
        this.output = output;
    }

    void execute() throws Exception {
        try (final InputStream propsInputStream = Module1.class.getResourceAsStream("module1.properties")) {
            final Properties prop = new Properties();
            prop.load(propsInputStream);
            final String message = prop.getProperty("hello");
            new PrintStream(this.output).printf("%s\n", message);
        }
    }

    public static void main(final String[] args) throws Exception {
        new Module1(System.out).execute();
    }
}
