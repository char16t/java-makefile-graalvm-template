package com.manenkov;

public final class Application2 {

    private void execute() {
        final char[] string = new char[] { 'h', 'e', 'l', 'l', 'o' };
        System.out.println(string);
    }

    public static void main(final String[] args) {
        new Application2().execute();
    }
}
