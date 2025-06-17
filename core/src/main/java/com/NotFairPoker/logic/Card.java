package com.NotFairPoker.logic;

import java.util.Objects;

public class Card {
    public enum Suit { C, D, H, S }
    public enum Rank {
        TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5), SIX("6", 6), SEVEN("7", 7),
        EIGHT("8", 8), NINE("9", 9), TEN("T", 10), JACK("J", 11), QUEEN("Q", 12),
        KING("K", 13), ACE("A", 14);

        public final String symbol;
        private final int value;

        Rank(String symbol, int value) {
            this.symbol = symbol;
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public final Suit suit;
    public final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public String getTexturePath() {
        return "cards/" + rank.symbol + suit.name() + ".png";
    }

    @Override
    public String toString() {
        return rank.symbol + suit.name();
    }

    // KLUCZOWE: Poprawna detekcja duplikatów!
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }
}
