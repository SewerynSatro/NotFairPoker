package com.NotFairPoker.ai;

import com.NotFairPoker.logic.Card;
import com.NotFairPoker.logic.Hand;
import com.NotFairPoker.logic.HandRank;
import com.NotFairPoker.GameState;

import java.util.List;
import java.util.Random;

public class BotLogic {

    public enum BotDecision { CHECK, CALL, RAISE, FOLD }

    private static final Random random = new Random();

    public static BotDecision decide(
        int callAmount,
        int potSize,
        int botChips,
        List<Card> botCards,
        List<Card> communityCards,
        GameState gameStage,
        int playerRaiseCount,
        int playerCheatCount,
        int totalHandsPlayed
    ) {
        double playerAggression = totalHandsPlayed > 0 ? (double) playerRaiseCount / totalHandsPlayed : 0;
        double cheatSuspicion = Math.min(1.0, playerCheatCount * 0.25);

        Hand botHand = new Hand(merge(botCards, communityCards));
        HandRank botRank = botHand.getRank();
        double handStrength = botRank.ordinal() / (double) HandRank.values().length;

        double callRatio = potSize > 0 ? ((double) callAmount / (double) potSize) : 0.0;

        // --- Bardziej ryzykowny bot: częściej CALL, rzadziej FOLD ---

        // Bez raise
        if (callAmount == 0) {
            // Więcej raise'ów nawet z przeciętną ręką
            if (handStrength > 0.35 && random.nextDouble() < (0.5 + handStrength * 0.4)) {
                return BotDecision.RAISE;
            }
            if (random.nextDouble() < 0.15) {
                return BotDecision.RAISE;
            }
            return BotDecision.CHECK;
        }

        // Niskie raise (do 30 coins)
        if (callAmount <= 30) {
            // Bardzo często CALL, prawie nigdy FOLD
            if (random.nextDouble() < 0.85) return BotDecision.CALL;
            if (handStrength > 0.5 && random.nextDouble() < 0.35) return BotDecision.RAISE;
            return BotDecision.CALL; // Rzadziej fold, domyślnie call
        }

        // Średnie raise (31-80 coins)
        if (callAmount <= 80) {
            if (handStrength > 0.2) {
                if (random.nextDouble() < 0.75) return BotDecision.CALL;
                if (handStrength > 0.7 && random.nextDouble() < 0.2) return BotDecision.RAISE;
            }
            // Zamiast fold od czasu do czasu zaryzykuj
            if (random.nextDouble() < 0.2) return BotDecision.CALL;
            return BotDecision.FOLD;
        }

        // Duże raise (>80 coins)
        if (handStrength > 0.5 && callAmount < botChips * 0.7) {
            if (random.nextDouble() < 0.65) return BotDecision.CALL;
            if (handStrength > 0.8 && random.nextDouble() < 0.2) return BotDecision.RAISE;
        }

        // SUSPICJA CHEATA
        if (cheatSuspicion > 0.85 && random.nextDouble() < cheatSuspicion) {
            return BotDecision.FOLD;
        }

        // Ostatnia szansa - minimalnie więcej ryzyka nawet przy bardzo wysokich stawkach
        if (random.nextDouble() < 0.15) return BotDecision.CALL;

        return BotDecision.FOLD;
    }

    private static List<Card> merge(List<Card> hand, List<Card> table) {
        List<Card> all = new java.util.ArrayList<>(hand);
        all.addAll(table);
        return all;
    }
}
