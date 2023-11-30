package com.manenkov.module1;

import java.io.ByteArrayOutputStream;

public class Module1Test {

    void execute() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        new Module1(output).execute();

        String expected = "Module1 works!\n";
        System.out.println("Actual: " + output);
        System.out.println("Expected: " + expected);
        assert (expected.equals(output.toString()));
    }

    public static void main(final String[] args) throws Exception {
        new Module1Test().execute();
    }
}
