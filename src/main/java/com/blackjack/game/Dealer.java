package com.blackjack.game;

import java.io.Serializable;

public class Dealer implements Serializable {
    private Hand hand;

    public Dealer() {
        this.hand = new Hand();
    }

    public Hand getHand() {
        return hand;
    }
}