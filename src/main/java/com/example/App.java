package com.example;

import static spark.Spark.*;

public class App {
    public static void main(String[] args) {
        port(getPort());
        get("/health", (req, res) -> "OK");
        get("/", (req, res) -> "Hello, Spark + Gradle + Docker!");
    }

    private static int getPort() {
        String p = System.getenv("PORT");
        return (p != null) ? Integer.parseInt(p) : 8080;
    }
}
