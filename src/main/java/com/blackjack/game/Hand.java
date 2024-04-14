package com.blackjack.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hand implements Serializable {
    private List<Card> cards;

    public Hand() {
        cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getValue() {
        int value = 0;
        int numAces = 0;

        for (Card card : cards) {
            int cardValue = card.getValue();
            if (cardValue == 11) {
                numAces++;
            }
            value += cardValue;
        }

        while (value > 21 && numAces > 0) {
            value -= 10;
            numAces--;
        }

        return value;
    }

    @Override
    public String toString() {
        return "Hand{" +
                "cards=" + cards +
                '}';
    }
}