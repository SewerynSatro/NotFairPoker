package com.NotFairPoker.logic;

import java.util.*;
import java.util.stream.Collectors;

public class Hand implements Comparable<Hand> {
    private final List<Card> cards;
    private HandRank rank;
    private List<Integer> rankValues;
    private List<Card> bestFiveCards;

    public Hand(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        evaluate();
    }

    public void swapCardForAce(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= cards.size()) {
            throw new IllegalArgumentException("Nieprawidłowy indeks karty.");
        }
        Card.Suit[] suits = Card.Suit.values();
        Random rand = new Random();
        Card.Suit randomSuit = suits[rand.nextInt(suits.length)];
        Card ace = new Card(randomSuit, Card.Rank.ACE);
        cards.set(cardIndex, ace);
        evaluate();
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public int size() {
        return cards.size();
    }

    private void evaluate() {
        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(Comparator.comparingInt((Card c) -> c.rank.getValue()).reversed());

        Map<Card.Suit, List<Card>> suits = sortedCards.stream()
            .collect(Collectors.groupingBy(c -> c.suit));

        List<Card> flushCards = suits.values().stream()
            .filter(list -> list.size() >= 5)
            .findFirst()
            .orElse(null);

        List<Card> straightCards = getStraight(sortedCards);
        List<Card> straightFlushCards = flushCards != null ? getStraight(flushCards) : null;

        Map<Integer, Long> groups = sortedCards.stream()
            .collect(Collectors.groupingBy(c -> c.rank.getValue(), Collectors.counting()));

        List<Map.Entry<Integer, Long>> grouped = new ArrayList<>(groups.entrySet());
        grouped.sort((a, b) -> {
            int cmp = Long.compare(b.getValue(), a.getValue());
            return cmp != 0 ? cmp : Integer.compare(b.getKey(), a.getKey());
        });

        if (straightFlushCards != null) {
            if (straightFlushCards.get(0).rank.getValue() == 14) {
                rank = HandRank.ROYAL_FLUSH;
            } else {
                rank = HandRank.STRAIGHT_FLUSH;
            }
            bestFiveCards = straightFlushCards.stream().limit(5).collect(Collectors.toList());
        } else if (grouped.get(0).getValue() == 4) {
            rank = HandRank.FOUR_OF_A_KIND;
            bestFiveCards = buildNOfAKindCards(sortedCards, grouped, 4);
        } else if (grouped.get(0).getValue() == 3 && grouped.size() > 1 && grouped.get(1).getValue() >= 2) {
            rank = HandRank.FULL_HOUSE;
            bestFiveCards = buildFullHouseCards(sortedCards, grouped);
        } else if (flushCards != null) {
            rank = HandRank.FLUSH;
            bestFiveCards = flushCards.stream().limit(5).collect(Collectors.toList());
        } else if (straightCards != null) {
            rank = HandRank.STRAIGHT;
            bestFiveCards = straightCards.stream().limit(5).collect(Collectors.toList());
        } else if (grouped.get(0).getValue() == 3) {
            rank = HandRank.THREE_OF_A_KIND;
            bestFiveCards = buildNOfAKindCards(sortedCards, grouped, 3);
        } else if (countPairs(grouped) >= 2) {
            rank = HandRank.TWO_PAIR;
            bestFiveCards = buildTwoPairCards(sortedCards, grouped);
        } else if (grouped.get(0).getValue() == 2) {
            rank = HandRank.ONE_PAIR;
            bestFiveCards = buildNOfAKindCards(sortedCards, grouped, 2);
        } else {
            rank = HandRank.HIGH_CARD;
            bestFiveCards = sortedCards.stream().limit(5).collect(Collectors.toList());
        }

        rankValues = bestFiveCards.stream().map(c -> c.rank.getValue()).collect(Collectors.toList());
        System.out.println("Evaluated hand rank: " + rank);
        System.out.println("Best hand: " + bestFiveCards);
    }

    private List<Card> getStraight(List<Card> cardList) {
        List<Integer> unique = cardList.stream().map(c -> c.rank.getValue()).distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        if (unique.contains(14)) unique.add(1);
        for (int i = 0; i <= unique.size() - 5; i++) {
            if (unique.get(i) - unique.get(i + 4) == 4) {
                int high = unique.get(i);
                return cardList.stream().filter(c -> {
                    int v = c.rank.getValue();
                    return v == high || v == high - 1 || v == high - 2 || v == high - 3 || v == high - 4;
                }).sorted(Comparator.comparingInt((Card c) -> c.rank.getValue()).reversed()).collect(Collectors.toList());
            }
        }
        return null;
    }

    private int countPairs(List<Map.Entry<Integer, Long>> grouped) {
        int count = 0;
        for (var e : grouped) {
            if (e.getValue() == 2) count++;
        }
        return count;
    }

    private List<Card> buildNOfAKindCards(List<Card> sortedCards, List<Map.Entry<Integer, Long>> grouped, int groupSize) {
        List<Integer> ranks = new ArrayList<>();
        for (var e : grouped) {
            if (e.getValue() == groupSize) ranks.add(e.getKey());
        }
        for (var e : grouped) {
            if (e.getValue() != groupSize) ranks.add(e.getKey());
        }

        List<Card> result = new ArrayList<>();
        for (int val : ranks) {
            for (Card c : sortedCards) {
                if (c.rank.getValue() == val && !result.contains(c)) result.add(c);
                if (result.size() == 5) return result;
            }
        }
        return result;
    }

    private List<Card> buildFullHouseCards(List<Card> sortedCards, List<Map.Entry<Integer, Long>> grouped) {
        int three = -1, pair = -1;
        for (var e : grouped) {
            if (e.getValue() == 3 && three == -1) three = e.getKey();
            else if (e.getValue() >= 2 && e.getKey() != three && pair == -1) pair = e.getKey();
        }
        List<Card> result = new ArrayList<>();
        for (Card c : sortedCards) {
            if (c.rank.getValue() == three && result.size() < 3) result.add(c);
        }
        for (Card c : sortedCards) {
            if (c.rank.getValue() == pair && result.size() < 5) result.add(c);
        }
        return result;
    }

    private List<Card> buildTwoPairCards(List<Card> sortedCards, List<Map.Entry<Integer, Long>> grouped) {
        List<Integer> pairs = new ArrayList<>();
        int kicker = -1;
        for (var e : grouped) {
            if (e.getValue() == 2 && pairs.size() < 2) pairs.add(e.getKey());
            else if (e.getValue() != 2 && kicker == -1) kicker = e.getKey();
        }
        List<Card> result = new ArrayList<>();
        for (int val : pairs) {
            for (Card c : sortedCards) {
                if (c.rank.getValue() == val && result.stream().filter(card -> card.rank.getValue() == val).count() < 2) result.add(c);
            }
        }
        for (Card c : sortedCards) {
            if (c.rank.getValue() == kicker && result.size() < 5) result.add(c);
        }
        return result;
    }

    public HandRank getRank() {
        return rank;
    }

    public List<Integer> getRankValues() {
        return rankValues;
    }

    public List<Card> getBestFiveCards() {
        return bestFiveCards;
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    @Override
    public int compareTo(Hand other) {
        int cmp = Integer.compare(this.rank.ordinal(), other.rank.ordinal());
        if (cmp != 0) return cmp;
        for (int i = 0; i < Math.min(this.rankValues.size(), other.rankValues.size()); i++) {
            cmp = Integer.compare(this.rankValues.get(i), other.rankValues.get(i));
            if (cmp != 0) return cmp;
        }
        return 0;
    }
}
