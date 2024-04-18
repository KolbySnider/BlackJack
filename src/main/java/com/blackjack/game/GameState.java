package com.blackjack.game;

import com.blackjack.network.Message;
import com.blackjack.network.MessageType;
import com.blackjack.server.BlackjackServer;

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
        }
        dealer.getHand().getCards().clear();
        deck = new Deck();
        deck.shuffle();
        gameOver = false;
        currentPlayerIndex = 0;
    }

    public synchronized void dealInitialCards() {
        for (Player player : players.get().values()) {
            Card card1 = deck.drawCard();
            Card card2 = deck.drawCard();
            player.getHand().addCard(card1);
            player.getHand().addCard(card2);
        }
        Card dealerCard1 = deck.drawCard();
        Card dealerCard2 = deck.drawCard();
        dealer.getHand().addCard(dealerCard1);
        dealer.getHand().addCard(dealerCard2);
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
            if (allPlayersFinished()) {
                startNewGame();
            }
        }
    }

    public void playerBust(Player player) {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.get().size()) {
            dealerTurn();
            endGame();
            if (allPlayersFinished()) {
                startNewGame();
            }
        }
    }

    public void dealerTurn() {
        while (dealer.getHand().getValue() < 17) {
            dealer.getHand().addCard(deck.drawCard());
        }
    }

    public void endGame() {
        gameOver = true;
    }
    public boolean isGameOver() {
        return gameOver;
    }
    public boolean isInitialCardsDealt() {
        return initialCardsDealt;
    }
    private boolean allPlayersFinished() {
        for (Player player : players.get().values()) {
            if (player.getHand().getValue() <= 21) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "players=" + players +
                ", dealer=" + dealer +
                ", deck=" + deck +
                ", gameOver=" + gameOver +
                ", currentPlayerIndex=" + currentPlayerIndex +
                ", initialCardsDealt=" + initialCardsDealt +
                '}';
    }
}