package com.manenkov;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class Application {

    void execute(String[] args) throws Exception {
        int port = 8000;
        InetSocketAddress address = new InetSocketAddress("0.0.0.0", port);
        HttpServer server = HttpServer.create(address, 0);

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/", new MyHandler());
        server.start();
    }

    public static void main(String[] args) throws Exception {
        new Application().execute(args);
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
