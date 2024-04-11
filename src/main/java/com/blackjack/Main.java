package com.blackjack;

import com.blackjack.client.BlackjackGUI;
import com.blackjack.server.BlackjackServer;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        // Start the server
        BlackjackServer server = new BlackjackServer();
        server.start();

        // Start the client GUI
        Application.launch(BlackjackGUI.class, args);
    }
}