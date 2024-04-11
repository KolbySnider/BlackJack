package com.blackjack.server;

import com.blackjack.game.GameState;
import com.blackjack.game.Player;
import com.blackjack.network.Message;
import com.blackjack.network.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BlackjackServer {
    private static final int PORT = 8888;

    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private GameState gameState;

    public BlackjackServer() {
        clients = new ArrayList<>();
        gameState = new GameState();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Blackjack server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                new Thread(String.valueOf(clientHandler)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                System.out.println("Player " + playerName + " joined the game."); // Log player joining
                break;
            case PLACE_BET:
                int betAmount = (int) message.getPayload();
                Player betPlayer = sender.getPlayer();
                betPlayer.placeBet(betAmount);
                broadcast(new Message(MessageType.GAME_STATE, gameState));
                break;
            case PLAYER_ACTION:
                String action = (String) message.getPayload();
                Player currentPlayer = gameState.getCurrentPlayer();
                if (currentPlayer == sender.getPlayer()) {
                    if (action.equals("HIT")) {
                        gameState.playerHit(currentPlayer);
                        if (currentPlayer.getHand().getValue() > 21) {
                            gameState.playerBust(currentPlayer);
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
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        gameState.removePlayer(client.getPlayer());
        broadcast(new Message(MessageType.PLAYER_LEFT, client.getPlayer().getName()));
    }

    public GameState getGameState() {
        return gameState;
    }
}