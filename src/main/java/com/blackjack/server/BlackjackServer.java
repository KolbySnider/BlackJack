package com.blackjack.server;

import com.blackjack.client.BlackjackGUI;
import com.blackjack.game.GameState;
import com.blackjack.game.Player;
import com.blackjack.network.Message;
import com.blackjack.network.MessageType;
import javafx.application.Platform;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BlackjackServer {
    private static final int PORT = 8888;
    private static final int REQUIRED_PLAYERS = 2;// set for more than 1 if you want multiplayer
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private GameState gameState;
    private int connectedPlayers;

    public BlackjackServer() {
        clients = new ArrayList<>();
        gameState = new GameState();
        connectedPlayers = 0;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Blackjack server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                connectedPlayers++;
                System.out.println("Player connected. Connected players: " + connectedPlayers);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
        System.out.println("Starting the game...");
        gameState.startNewGame();

        // Add players to the GameState before dealing initial cards
        for (ClientHandler client : clients) {
            Player player = client.getPlayer();
            if (player != null) {
                gameState.addPlayer(player);
            }
        }

        gameState.dealInitialCards();

        if (gameState.isInitialCardsDealt()) {
            broadcast(new Message(MessageType.GAME_STATE, gameState));
        }
    }
    private void startNewRound() {
        gameState.startNewGame();
        gameState.dealInitialCards();
        broadcast(new Message(MessageType.NEW_ROUND, gameState));
    }

    public synchronized void broadcast(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized void handleMessage(ClientHandler sender, Message message) {
        switch (message.getType()) {
            case PLAYER_JOINED:
                String playerName = (String) message.getPayload();
                Player player = new Player(playerName);
                gameState.addPlayer(player);
                sender.setPlayer(player);
                broadcast(new Message(MessageType.PLAYER_JOINED, playerName));
                System.out.println("Player " + playerName + " joined the game.");

                if (gameState.getAllPlayers().size() == REQUIRED_PLAYERS) {
                    startGame();
                }
                break;
            case PLAYER_ACTION:
                String action = (String) message.getPayload();
                Player currentPlayer = gameState.getCurrentPlayer();
                if (currentPlayer == sender.getPlayer()) {
                    if (action.equals("HIT")) {
                        gameState.playerHit(currentPlayer);
                        if (currentPlayer.getHand().getValue() > 21) {
                            gameState.playerBust(currentPlayer);
                            broadcast(new Message(MessageType.GAME_STATE, gameState));

                        }
                    } else if (action.equals("STAND")) {
                        gameState.playerStand(currentPlayer);
                    }
                    broadcast(new Message(MessageType.GAME_STATE, gameState));
                }
                break;
            case CHAT_MESSAGE:
                String chatMessage = (String) message.getPayload();
                broadcast(new Message(MessageType.CHAT_MESSAGE, sender.getPlayer().getName() + ": " + chatMessage));
                break;
            case NEW_ROUND:
                startNewRound();
                break;
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        Player player = client.getPlayer();
        if (player != null) {
            gameState.removePlayer(player);
            broadcast(new Message(MessageType.PLAYER_LEFT, player.getName()));
        }
    }

}