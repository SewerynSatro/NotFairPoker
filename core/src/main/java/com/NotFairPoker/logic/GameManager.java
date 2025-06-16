package com.NotFairPoker.logic;

import com.NotFairPoker.GameState;
import com.NotFairPoker.ai.BotLogic;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    public enum PlayerAction { CHECK, CALL, RAISE, FOLD }

    private final Player player;
    private final Player bot;
    private Deck deck;
    private final List<Card> communityCards;

    private int pot;
    private boolean playerIsSmallBlind;

    private static final int SMALL_BLIND = 10;
    private static final int BIG_BLIND = 20;

    private int currentBet = 0;
    private boolean isPlayerTurn = true;
    private int lastPlayerBet = 0;
    private int lastBotBet = 0;
    private boolean waitingForResponse = false;
    private boolean lastAggressorIsPlayer = false;
    private boolean isShowdownAfterFold = false;
    private boolean gameOver = false;

    private GameState gameState;

    public GameManager() {
        this.player = new Player(false);
        this.bot = new Player(true);
        this.communityCards = new ArrayList<>();
        this.pot = 0;
        this.playerIsSmallBlind = true;
        this.gameState = GameState.PRE_FLOP;
    }

    public void startNewHand() {
        System.out.println("=== NOWE ROZDANIE ===");

        deck = new Deck();
        deck.shuffle();
        player.reset();
        bot.reset();
        communityCards.clear();
        gameState = GameState.PRE_FLOP;
        pot = 0;
        resetBets();

        player.addCard(deck.draw());
        player.addCard(deck.draw());
        bot.addCard(deck.draw());
        bot.addCard(deck.draw());

        if (playerIsSmallBlind) {
            if (player.chips < SMALL_BLIND || bot.chips < BIG_BLIND) {
                gameOver = true;
                System.out.println("=== GAME OVER ===");
                return;
            }
            player.chips -= SMALL_BLIND;
            bot.chips -= BIG_BLIND;
            pot += SMALL_BLIND + BIG_BLIND;
            currentBet = BIG_BLIND;
            lastPlayerBet = SMALL_BLIND;
            lastBotBet = BIG_BLIND;
            isPlayerTurn = true;
        } else {
            if (player.chips < BIG_BLIND || bot.chips < SMALL_BLIND) {
                gameOver = true;
                System.out.println("=== GAME OVER ===");
                return;
            }
            player.chips -= BIG_BLIND;
            bot.chips -= SMALL_BLIND;
            pot += SMALL_BLIND + BIG_BLIND;
            currentBet = BIG_BLIND;
            lastPlayerBet = BIG_BLIND;
            lastBotBet = SMALL_BLIND;
            isPlayerTurn = false;
            botTurn();
        }

        playerIsSmallBlind = !playerIsSmallBlind;
    }


    private void resetBets() {
        currentBet = 0;
        lastPlayerBet = 0;
        lastBotBet = 0;
        waitingForResponse = false;
        lastAggressorIsPlayer = false;
    }

    public void playerAction(PlayerAction action, int raiseAmount) {
        if (!isPlayerTurn) return;

        switch (action) {
            case CHECK -> {
                if (currentBet == lastPlayerBet) {
                    System.out.println("PLAYER checks.");
                    endTurn();
                } else {
                    System.out.println("Cannot check, must call or fold.");
                }
            }
            case CALL -> {
                int callAmount = currentBet - lastPlayerBet;
                System.out.println("PLAYER calls " + callAmount);
                player.chips -= callAmount;
                pot += callAmount;
                lastPlayerBet += callAmount;
                waitingForResponse = false;
                endTurn();
            }
            case RAISE -> {
                int totalBet = currentBet + raiseAmount;
                int callAmount = totalBet - lastPlayerBet;

                System.out.println("PLAYER raises to " + totalBet);
                player.chips -= callAmount;
                pot += callAmount;

                currentBet = totalBet;
                lastPlayerBet += callAmount;
                waitingForResponse = true;
                lastAggressorIsPlayer = true;
                endTurn();
            }
            case FOLD -> {
                System.out.println("PLAYER folds.");
                System.out.println("BOT wins the pot of " + pot + " chips.");
                bot.chips += pot;
                gameState = GameState.SHOWDOWN;
                isShowdownAfterFold = true;
            }
        }
    }

    public void botTurn() {
        System.out.println("BOT's turn.");

        int callAmount = currentBet - lastBotBet;
        BotLogic.BotDecision decision = BotLogic.decide(callAmount, pot, bot.chips);

        switch (decision) {
            case CHECK -> {
                System.out.println("BOT checks.");
                endTurn();
            }
            case CALL -> {
                bot.chips -= callAmount;
                pot += callAmount;
                lastBotBet += callAmount;
                System.out.println("BOT calls " + callAmount);
                waitingForResponse = false;
                endTurn();
            }
            case RAISE -> {
                int raiseAmount = 20;
                int totalBet = currentBet + raiseAmount;
                int toPay = totalBet - lastBotBet;

                bot.chips -= toPay;
                pot += toPay;
                currentBet = totalBet;
                lastBotBet += toPay;
                System.out.println("BOT raises to " + totalBet);
                waitingForResponse = true;
                lastAggressorIsPlayer = false;
                endTurn();
            }
            case FOLD -> {
                System.out.println("BOT folds.");
                System.out.println("PLAYER wins the pot of " + pot + " chips.");
                player.chips += pot;
                isShowdownAfterFold = true;
                gameState = GameState.SHOWDOWN;
            }
        }
    }

    private void endTurn() {
        if (lastPlayerBet == lastBotBet && !waitingForResponse) {
            if (gameState == GameState.RIVER) {
                evaluateHands();
                return;
            }
            if (isBettingRoundClosed()) {
                nextGameState();
            } else {
                switchTurn();
            }
        } else {
            switchTurn();
        }
    }

    private boolean isBettingRoundClosed() {
        return (isPlayerTurn && !lastAggressorIsPlayer) || (!isPlayerTurn && lastAggressorIsPlayer);
    }

    private void switchTurn() {
        isPlayerTurn = !isPlayerTurn;
        if (!isPlayerTurn) {
            botTurn();
        }
    }

    private void nextGameState() {
        resetBets();

        switch (gameState) {
            case PRE_FLOP -> {
                System.out.println("--- FLOP ---");
                for (int i = 0; i < 3; i++) {
                    communityCards.add(deck.draw());
                }
                gameState = GameState.FLOP;
            }
            case FLOP -> {
                System.out.println("--- TURN ---");
                communityCards.add(deck.draw());
                gameState = GameState.TURN;
            }
            case TURN -> {
                System.out.println("--- RIVER ---");
                communityCards.add(deck.draw());
                gameState = GameState.RIVER;
            }
            default -> throw new IllegalStateException("Unexpected game state: " + gameState);
        }

        isPlayerTurn = playerIsSmallBlind;
        System.out.println(isPlayerTurn ? "PLAYER's turn." : "BOT's turn.");
    }

    public void evaluateHands() {
        Hand playerHand = new Hand(player.getHandAndCommunity(communityCards));
        Hand botHand = new Hand(bot.getHandAndCommunity(communityCards));

        System.out.println("-------------------------------------------------");
        System.out.println("SHOWDOWN!");
        System.out.println("Player hand: " + playerHand.getRank() + " -> " + formatBestCards(playerHand));
        System.out.println("Bot hand:    " + botHand.getRank() + " -> " + formatBestCards(botHand));

        int comparison = playerHand.compareTo(botHand);
        if (comparison > 0) {
            System.out.println("PLAYER wins the pot of " + pot + " chips.");
            player.chips += pot;
        } else if (comparison < 0) {
            System.out.println("BOT wins the pot of " + pot + " chips.");
            bot.chips += pot;
        } else {
            System.out.println("It's a TIE! Each player takes " + (pot / 2) + " chips.");
            player.chips += pot / 2;
            bot.chips += pot / 2;
        }
        System.out.println("Player chips: " + player.chips);
        System.out.println("Bot chips: " + bot.chips);
        System.out.println("-------------------------------------------------");

        gameState = GameState.SHOWDOWN;
        isShowdownAfterFold = false;

        if (player.chips <= 0 || bot.chips <= 0) {
            gameOver = true;
        }
    }

    private String formatBestCards(Hand hand) {
        return hand.getBestFiveCards().stream()
            .map(card -> card.rank.symbol + card.suit.name())
            .toList().toString();
    }

    public boolean isShowdownAfterFold() {
        return isShowdownAfterFold;
    }

    public boolean isPlayerSmallBlind() {
        return playerIsSmallBlind;
    }
    public int getLastPlayerBet() {
        return lastPlayerBet;
    }

    public int getLastBotBet() {
        return lastBotBet;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public int getCurrentBet() { return currentBet; }
    public int getPlayerChips() { return player.chips; }
    public int getBotChips() { return bot.chips; }
    public int getPot() { return pot; }
    public GameState getGameState() { return gameState; }
    public List<Card> getCommunityCards() { return communityCards; }
    public Player getPlayer() { return player; }
    public Player getBot() { return bot; }
}
