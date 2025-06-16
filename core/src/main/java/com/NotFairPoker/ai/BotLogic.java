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

    // Adaptacyjny bot!
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
        double playerAggression = totalHandsPlayed > 0 ? (double)playerRaiseCount / totalHandsPlayed : 0;
        double cheatSuspicion = Math.min(1.0, playerCheatCount * 0.25);

        Hand botHand = new Hand(merge(botCards, communityCards));
        HandRank botRank = botHand.getRank();
        double handStrength = botRank.ordinal() / (double) HandRank.values().length;

        double baseRaise = 0.20 + handStrength * 0.5 + playerAggression * 0.20 - cheatSuspicion * 0.25;
        double baseCall =  0.30 + handStrength * 0.5 - cheatSuspicion * 0.30;

        if (callAmount == 0) {
            if (random.nextDouble() < baseRaise) return BotDecision.RAISE;
            return BotDecision.CHECK;
        }

        if (handStrength > 0.4 && random.nextDouble() < baseCall) {
            return BotDecision.CALL;
        }

        if (callAmount < 30 && random.nextDouble() < 0.20) {
            return BotDecision.RAISE;
        }

        if (cheatSuspicion > 0.7 && random.nextDouble() < cheatSuspicion) {
            return BotDecision.FOLD;
        }

        return BotDecision.FOLD;
    }

    private static List<Card> merge(List<Card> hand, List<Card> table) {
        List<Card> all = new java.util.ArrayList<>(hand);
        all.addAll(table);
        return all;
    }
}
