package com.NotFairPoker.logic;

import java.util.*;

public class Player {
    public List<Card> hand = new ArrayList<>();
    public int chips;
    public boolean isBot;

    public Player(boolean isBot) {
        this.isBot = isBot;
        this.chips = 200;  // ustawiamy startowe żetony
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void reset() {
        hand.clear();
    }

    public List<Card> getHandAndCommunity(List<Card> community) {
        List<Card> all = new ArrayList<>(hand);
        all.addAll(community);
        return all;
    }
}
