package com.blackjack;


import com.blackjack.server.BlackjackServer;

public class Main {
    public static void main(String[] args) {
        // Start the server
        BlackjackServer server = new BlackjackServer();
        server.start();

    }
}