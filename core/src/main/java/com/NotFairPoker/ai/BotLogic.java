package com.NotFairPoker.ai;

import java.util.Random;

public class BotLogic {

    public enum BotDecision { CHECK, CALL, RAISE, FOLD }

    private static final Random random = new Random();

    public static BotDecision decide(int callAmount, int potSize, int botChips) {

        // 100% check jeśli nic nie trzeba płacić
        if (callAmount == 0) {
            double roll = random.nextDouble();
            if (roll < 0.2) return BotDecision.RAISE;
            return BotDecision.CHECK;
        }

        // logika dla callAmount > 0
        double callRatio = (double) callAmount / (potSize + 1); // +1 dla uniknięcia dzielenia przez zero

        // im wyższy call ratio tym mniejsza skłonność do callowania
        double callThreshold = 0.5 - (callRatio * 0.8);

        if (random.nextDouble() < callThreshold) {
            return BotDecision.CALL;
        }

        // Rzadki raise gdy callAmount mały
        if (callAmount <= 30 && random.nextDouble() < 0.15) {
            return BotDecision.RAISE;
        }

        return BotDecision.FOLD;
    }
}
