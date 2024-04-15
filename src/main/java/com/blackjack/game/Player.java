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

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", hand=" + hand +
                ", balance=" + balance +
                ", bet=" + bet +
                '}';
    }
}