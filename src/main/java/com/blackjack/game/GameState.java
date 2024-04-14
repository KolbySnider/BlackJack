package com.blackjack.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class GameState implements Serializable {
    private final AtomicReference<Map<String, Player>> players = new AtomicReference<>();
    private Dealer dealer;
    private Deck deck;
    private boolean gameOver;
    private int currentPlayerIndex;

    private boolean initialCardsDealt;

    public GameState() {
        players.set(new HashMap<>());
        dealer = new Dealer();
        deck = new Deck();
        gameOver = false;
        currentPlayerIndex = 0;
    }

    public synchronized void addPlayer(Player player) {
        System.out.println("Player set");
        players.updateAndGet(map -> {
            map.put(player.getName(), player);
            return map;
        });
    }

    public synchronized void removePlayer(Player player) {
        players.updateAndGet(map -> {
            map.remove(player.getName());
            return map;
        });
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.get().values());
    }
    public Player getCurrentPlayer() {
        if (currentPlayerIndex < getAllPlayers().size()) {
            return getAllPlayers().get(currentPlayerIndex);
        }
        return null;
    }

    public Player getPlayer(String playerName) {
        return players.get().get(playerName);
    }

    public Dealer getDealer() {
        return dealer;
    }

    public void startNewGame() {
        for (Player player : players.get().values()) {
            player.getHand().getCards().clear();
            player.resetBet();
        }
        dealer.getHand().getCards().clear();
        deck = new Deck();
        deck.shuffle();
        gameOver = false;
        currentPlayerIndex = 0;
    }

    public synchronized void dealInitialCards() {
        System.out.println("Players map: " + players);
        for (Player player : players.get().values()) {
            Card card1 = deck.drawCard();
            Card card2 = deck.drawCard();
            player.getHand().addCard(card1);
            player.getHand().addCard(card2);
            System.out.println("Dealt cards to player " + player.getName() + ": " + card1.getRank() + " of " + card1.getSuit() + ", " + card2.getRank() + " of " + card2.getSuit());
        }
        Card dealerCard1 = deck.drawCard();
        Card dealerCard2 = deck.drawCard();
        dealer.getHand().addCard(dealerCard1);
        dealer.getHand().addCard(dealerCard2);
        System.out.println("Dealt cards to dealer: " + dealerCard1.getRank() + " of " + dealerCard1.getSuit() + ", " + dealerCard2.getRank() + " of " + dealerCard2.getSuit());
        initialCardsDealt = true;
    }

    public void playerHit(Player player) {
        player.getHand().addCard(deck.drawCard());
    }

    public void playerStand(Player player) {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.get().size()) {
            dealerTurn();
            endGame();
        }
    }

    public void playerBust(Player player) {
        player.resetBet();
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.get().size()) {
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

        for (Player player : players.get().values()) {
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
    public boolean isInitialCardsDealt() {
        return initialCardsDealt;
    }



}