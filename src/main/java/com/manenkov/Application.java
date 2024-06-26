package com.manenkov;

import com.manenkov.parser.CommandLineParser;

public class Application {
    void execute(String[] args) throws Exception {
        Thread thread = Thread.ofVirtual().start(() -> {
            CommandLineParser.parse(args);
        });
        thread.join();
    }

    public static void version() {
        System.out.println("Version 0.0.2");
    }

    public static void help() {
        System.out.println("Short help");
    } 

    public static void welcome(String name) {
        System.out.println("Hello, " + name + "!");
    }

    public static String parseError() {
        return "Short help after parse error.";
    }

    public static String parseWelcomeError() {
        return "Wrong welcome format.";
    }

    public static void main(String[] args) throws Exception {
        new Application().execute(args);
    }
}
