package com.manenkov;

import com.manenkov.parser.CommandLineParser;

public class Application {
    void execute(String[] args) throws Exception {
        Thread thread = Thread.ofVirtual().start(() -> {
            System.out.println("It's works!");
            CommandLineParser.parse(args);
        });
        thread.join();
    }

    public static void version() {
        System.out.println("Version 0.0.2");
    } 

    public static void main(String[] args) throws Exception {
        new Application().execute(args);
    }
}
