package com.blackjack.game;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private Hand hand;
    private int balance;
    private int bet;

    public Player(String name) {
        this.name = name;
        this.hand = new Hand();
        this.balance = 1000;
        this.bet = 0;
    }

    public String getName() {
        return name;
    }

    public Hand getHand() {
        return hand;
    }

    public int getBalance() {
        return balance;
    }

    public int getBet() {
        return bet;
    }

    public void placeBet(int amount) {
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        balance -= amount;
        bet = amount;
    }

    public void resetBet() {
        balance += bet;
        bet = 0;
    }

    public void winBet() {
        balance += bet * 2;
        bet = 0;
    }
}