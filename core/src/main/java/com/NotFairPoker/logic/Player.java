package com.NotFairPoker.logic;

import java.util.*;

public class Player {
    public Hand hand;
    public int chips;
    public boolean isBot;

    public Player(boolean isBot) {
        this.isBot = isBot;
        this.chips = 200;
        this.hand = null;
    }

    public void setHand(List<Card> cards) {
        this.hand = new Hand(cards);
    }

    public List<Card> getHandCards() {
        return hand != null ? hand.getCards() : new ArrayList<>();
    }

    public void reset() {
        this.hand = null;
    }

    public List<Card> getHandAndCommunity(List<Card> community) {
        List<Card> all = new ArrayList<>(getHandCards());
        all.addAll(community);
        return all;
    }
}
