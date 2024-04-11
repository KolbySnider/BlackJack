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
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private BlackjackGUI gui;
    private String playerName;

    public BlackjackClient(String serverAddress, int serverPort, BlackjackGUI gui) {
        this.gui = gui;
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // Send player name to the server
        sendMessage(new Message(MessageType.PLAYER_JOINED, gui.getPlayerName()));

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
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}