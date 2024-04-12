package com.blackjack.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {
    private Map<String, Player> players;
    private Dealer dealer;
    private Deck deck;
    private boolean gameOver;
    private int currentPlayerIndex;

    public GameState() {
        players = new HashMap<>();
        dealer = new Dealer();
        deck = new Deck();
        gameOver = false;
        currentPlayerIndex = 0;
    }

    public void addPlayer(Player player) {
        System.out.println("Player set");
        players.put(player.getName(), player);
    }

    public void removePlayer(Player player) {
        players.remove(player.getName());
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    public Player getPlayer(String playerName) {
        return players.get(playerName);
    }

    public Dealer getDealer() {
        return dealer;
    }

    public void startNewGame() {
        for (Player player : players.values()) {
            player.getHand().getCards().clear();
            player.resetBet();
        }
        dealer.getHand().getCards().clear();
        deck = new Deck();
        deck.shuffle();
        gameOver = false;
        currentPlayerIndex = 0;
    }

    public void dealInitialCards() {
        for (Player player : players.values()) {
            player.getHand().addCard(deck.drawCard());
            player.getHand().addCard(deck.drawCard());
        }
        dealer.getHand().addCard(deck.drawCard());
        dealer.getHand().addCard(deck.drawCard());
    }

    public void playerHit(Player player) {
        player.getHand().addCard(deck.drawCard());
    }

    public void playerStand(Player player) {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.size()) {
            dealerTurn();
            endGame();
        }
    }

    public void playerBust(Player player) {
        player.resetBet();
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.size()) {
            endGame();
        }
    }

    public void dealerTurn() {
        while (dealer.getHand().getValue() < 17) {
            dealer.getHand().addCard(deck.drawCard());
        }
    }

    public void determineWinners() {
        int dealerValue = dealer.getHand().getValue();

        for (Player player : players.values()) {
            int playerValue = player.getHand().getValue();

            if (playerValue > 21) {
                player.resetBet();
            } else if (dealerValue > 21 || playerValue > dealerValue) {
                player.winBet();
            } else if (playerValue == dealerValue) {
                player.resetBet();
            } else {
                player.resetBet();
            }
        }
    }

    public void endGame() {
        gameOver = true;
        determineWinners();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getCurrentPlayer() {
        if (currentPlayerIndex < getAllPlayers().size()) {
            return getAllPlayers().get(currentPlayerIndex);
        }
        return null;
    }
}