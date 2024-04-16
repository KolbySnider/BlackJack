package com.blackjack.client;

import com.blackjack.game.GameState;
import com.blackjack.network.Message;
import com.blackjack.network.MessageType;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BlackjackClient {

    private GameState currentState; 
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private BlackjackGUI gui;
    private String playerName;

    public BlackjackClient(String serverAddress, int serverPort, BlackjackGUI gui) {
        this.gui = gui;
        this.currentState = new GameState(); // Initialize currentState with a new GameState object
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the connection failure, e.g., display an error message to the user
            Platform.runLater(() -> {
                gui.showAlert("Connection Error", "Failed to connect to the server. Please check the server address and port.");
            });
        }
    }

    public void start(String name) {
        setPlayerName(name);

        // Send player name to the server
        sendMessage(new Message(MessageType.PLAYER_JOINED, name));

        // Start listening for server messages
        new Thread(() -> {
            try {
                while (true) {
                    Message message = (Message) inputStream.readObject();
                    handleMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case PLAYER_JOINED:
                String joinedPlayerName = (String) message.getPayload();
                Platform.runLater(() -> gui.addMessage(joinedPlayerName + " joined the game."));
                break;
            case PLAYER_LEFT:
                String leftPlayerName = (String) message.getPayload();
                Platform.runLater(() -> gui.addMessage(leftPlayerName + " left the game."));
                break;
            case GAME_STATE:
                GameState gameState = (GameState) message.getPayload();
                currentState = gameState;
                Platform.runLater(() -> gui.updateGameState(gameState));
                break;
            case CHAT_MESSAGE:
                String chatMessage = (String) message.getPayload();
                Platform.runLater(() -> gui.addMessage(chatMessage));
                break;

        }
    }

    public void sendMessage(Message message) {
        try {
            if (outputStream != null) {
                outputStream.writeUnshared(message);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the message sending failure, e.g., display an error message to the user
            Platform.runLater(() -> {
                gui.showAlert("Communication Error", "Failed to send message to the server. Please check the connection.");
            });
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public GameState getCurrentState() {
        return currentState;
    }
}