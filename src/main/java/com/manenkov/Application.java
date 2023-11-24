package com.manenkov;

public class Application {
    void execute() throws Exception {
        Thread thread = Thread.ofVirtual().start(() -> System.out.println("It's works!"));
        thread.join();
    }

    public static void main(String[] args) throws Exception {
        new Application().execute();
    }
}
