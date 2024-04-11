package com.blackjack.game;

import java.io.Serializable;

public class Card implements Serializable {
    private String suit;
    private String rank;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public int getValue() {
        switch (rank) {
            case "Ace":
                return 11;
            case "King":
            case "Queen":
            case "Jack":
            case "10":
                return 10;
            default:
                return Integer.parseInt(rank);
        }
    }
}