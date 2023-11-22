package com.manenkov;

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
        System.out.println("It's works!");
    }
}
