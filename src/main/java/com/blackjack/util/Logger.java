package com.blackjack.util;

public class Logger {
    public static void log(String message) {
        System.out.println(message);
    }

    public static void error(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }
}