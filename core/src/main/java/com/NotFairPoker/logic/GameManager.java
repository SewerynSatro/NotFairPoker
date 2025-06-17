package com.NotFairPoker.logic;

import com.NotFairPoker.GameState;
import com.NotFairPoker.ai.BotLogic;
import com.NotFairPoker.screens.TableScreen;

import java.util.*;

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
    private TableScreen tableScreen;

    private boolean cheatCaughtThisHand = false;
    private boolean peekCheatCaught = false;

    private GameState gameState;

    // Showdown info for display
    private String showdownResultText = "";
    private String showdownPlayerHandText = "";
    private String showdownBotHandText = "";

    // --- CHEAT logic ---
    private boolean playerUsedAceCheat = false;
    private Card cheatedAce = null;

    // ---- STATISTICS for bot adaptation ----
    private int playerRaiseCount = 0;
    private int playerFoldCount = 0;
    private int playerCheatCount = 0;
    private int totalHandsPlayed = 0;
    private int playerShowdownWins = 0;
    private int botShowdownWins = 0;

    public GameManager(TableScreen tableScreen) {
        this.player = new Player(false);
        this.bot = new Player(true);
        this.communityCards = new ArrayList<>();
        this.pot = 0;
        this.playerIsSmallBlind = true;
        this.gameState = GameState.PRE_FLOP;
        this.tableScreen = tableScreen;
    }

    public void startNewHand() {
        deck = new Deck();
        deck.shuffle();
        player.reset();
        bot.reset();
        communityCards.clear();
        gameState = GameState.PRE_FLOP;
        pot = 0;
        resetBets();
        if (tableScreen != null) {
            tableScreen.setBotDecisionTexture(null);
        }

        cheatCaughtThisHand = false;
        peekCheatCaught = false;
        playerUsedAceCheat = false;
        cheatedAce = null;

        showdownResultText = "";
        showdownPlayerHandText = "";
        showdownBotHandText = "";

        List<Card> playerCards = List.of(deck.draw(), deck.draw());
        List<Card> botCards = List.of(deck.draw(), deck.draw());
        player.setHand(playerCards);
        bot.setHand(botCards);

        if (playerIsSmallBlind) {
            if (player.chips < SMALL_BLIND || bot.chips < BIG_BLIND) {
                gameOver = true;
                if (tableScreen != null) tableScreen.setBotDecisionTexture(null);
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
                if (tableScreen != null) tableScreen.setBotDecisionTexture(null);
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
        totalHandsPlayed++;
    }

    private void resetBets() {
        currentBet = 0;
        lastPlayerBet = 0;
        lastBotBet = 0;
        waitingForResponse = false;
        lastAggressorIsPlayer = false;
        isShowdownAfterFold = false;
    }

    public void playerAction(PlayerAction action, int raiseAmount) {
        if (!isPlayerTurn) return;

        switch (action) {
            case CHECK -> {
                if (currentBet == lastPlayerBet) {
                    endTurn();
                }
            }
            case CALL -> {
                int callAmount = currentBet - lastPlayerBet;
                player.chips -= callAmount;
                pot += callAmount;
                lastPlayerBet += callAmount;
                waitingForResponse = false;
                endTurn();
            }
            case RAISE -> {
                int totalBet = currentBet + raiseAmount;
                int callAmount = totalBet - lastPlayerBet;
                player.chips -= callAmount;
                pot += callAmount;

                currentBet = totalBet;
                lastPlayerBet += callAmount;
                waitingForResponse = true;
                lastAggressorIsPlayer = true;
                playerRaiseCount++;
                endTurn();
            }
            case FOLD -> {
                bot.chips += pot;
                gameState = GameState.SHOWDOWN;
                isShowdownAfterFold = true;
                playerFoldCount++;
                showdownResultText = "Player folded. Bot wins the pot of " + pot + " chips!";
                showdownPlayerHandText = "";
                showdownBotHandText = "";
            }
        }
    }

    public void botTurn() {
        int callAmount = currentBet - lastBotBet;
        BotLogic.BotDecision decision = BotLogic.decide(
            callAmount, pot, bot.chips,
            bot.hand.getCards(), communityCards, gameState,
            playerRaiseCount, playerCheatCount, totalHandsPlayed
        );

        switch (decision) {
            case CHECK -> {
                if (tableScreen != null) tableScreen.setBotDecisionTexture("CHECK");
                endTurn();
            }
            case CALL -> {
                bot.chips -= callAmount;
                pot += callAmount;
                lastBotBet += callAmount;
                if (tableScreen != null) tableScreen.setBotDecisionTexture("CALL");
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
                if (tableScreen != null) tableScreen.setBotDecisionTexture("RAISE");
                waitingForResponse = true;
                lastAggressorIsPlayer = false;
                endTurn();
            }
            case FOLD -> {
                if (tableScreen != null) tableScreen.setBotDecisionTexture("FOLD");
                player.chips += pot;
                isShowdownAfterFold = true;
                gameState = GameState.SHOWDOWN;
                showdownResultText = "Bot folded. Player wins the pot of " + pot + " chips!";
                showdownPlayerHandText = "";
                showdownBotHandText = "";
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

    public void setTableScreen(TableScreen tableScreen) {
        this.tableScreen = tableScreen;
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
                for (int i = 0; i < 3; i++) {
                    communityCards.add(deck.draw());
                }
                gameState = GameState.FLOP;
            }
            case FLOP -> {
                communityCards.add(deck.draw());
                gameState = GameState.TURN;
            }
            case TURN -> {
                communityCards.add(deck.draw());
                gameState = GameState.RIVER;
            }
            default -> throw new IllegalStateException("Unexpected game state: " + gameState);
        }

        isPlayerTurn = playerIsSmallBlind;
    }

    // === NAJWAŻNIEJSZA POPRAWKA: wykrywaj duplikaty dowolnych kart w showdownie ===
    private boolean detectDuplicateCardsOnShowdown() {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(player.hand.getCards());
        allCards.addAll(bot.hand.getCards());
        allCards.addAll(communityCards);

        Set<Card> unique = new HashSet<>(allCards);
        return unique.size() != allCards.size();
    }

    public void evaluateHands() {
        // --- CHEAT DETECTION: duplicate cards (anywhere: player, bot, table) ---
        if (detectDuplicateCardsOnShowdown()) {
            bot.chips += pot;
            showdownResultText = "Cheat detected! Duplicate cards found. Bot wins the pot of " + pot + " chips!";
            showdownPlayerHandText = "";
            showdownBotHandText = "";
            gameState = GameState.SHOWDOWN;
            gameOver = (player.chips <= 0 || bot.chips <= 0);
            playerCheatCount++;
            return;
        }

        // --- normalna ocena rąk ---
        Hand playerHand = new Hand(player.getHandAndCommunity(communityCards));
        Hand botHand = new Hand(bot.getHandAndCommunity(communityCards));
        int comparison = playerHand.compareTo(botHand);

        if (comparison > 0) {
            player.chips += pot;
            showdownResultText = "Player wins the pot of " + pot + " chips!";
            showdownPlayerHandText = "Player: " + playerHand.getRank() + " (" + formatBestCards(playerHand) + ")";
            showdownBotHandText = "Bot: " + botHand.getRank() + " (" + formatBestCards(botHand) + ")";
            playerShowdownWins++;
        } else if (comparison < 0) {
            bot.chips += pot;
            showdownResultText = "Bot wins the pot of " + pot + " chips!";
            showdownPlayerHandText = "Player: " + playerHand.getRank() + " (" + formatBestCards(playerHand) + ")";
            showdownBotHandText = "Bot: " + botHand.getRank() + " (" + formatBestCards(botHand) + ")";
            botShowdownWins++;
        } else {
            player.chips += pot / 2;
            bot.chips += pot / 2;
            showdownResultText = "It's a tie! Pot of " + pot + " chips is split.";
            showdownPlayerHandText = "Player: " + playerHand.getRank() + " (" + formatBestCards(playerHand) + ")";
            showdownBotHandText = "Bot: " + botHand.getRank() + " (" + formatBestCards(botHand) + ")";
        }

        gameState = GameState.SHOWDOWN;
        if (tableScreen != null) tableScreen.setBotDecisionTexture(null);

        if (player.chips <= 0 || bot.chips <= 0) {
            gameOver = true;
        }
    }

    private String formatBestCards(Hand hand) {
        return hand.getBestFiveCards().stream()
            .map(card -> card.getRank().symbol + card.getSuit().name())
            .toList().toString();
    }

    // --- CHEAT: BOT DETECTION (random, not used for ace-detect logic anymore) ---
    public boolean tryBotDetectCheat() {
        Random rand = new Random();
        boolean caught = rand.nextInt(100) < 50;
        cheatCaughtThisHand = caught;
        if (caught) {
            bot.chips += pot;
            showdownResultText = "Your sleight of hand failed. The opponent caught you and claims the pot of " + pot + " chips!";
            showdownPlayerHandText = "";
            showdownBotHandText = "";
            gameState = GameState.SHOWDOWN;
            gameOver = (player.chips <= 0 || bot.chips <= 0);
            playerCheatCount++;
        }
        return caught;
    }
    public boolean isCheatCaughtThisHand() {
        return cheatCaughtThisHand;
    }

    // --- CHEAT: PEEKING ---
    public boolean tryBotDetectPeekCheat() {
        Random rand = new Random();
        boolean caught = rand.nextInt(100) < 30;
        peekCheatCaught = caught;
        if (caught) {
            bot.chips += pot;
            showdownResultText = "You were caught sneaking a peek. The opponent claims the pot of " + pot + " chips!";
            showdownPlayerHandText = "";
            showdownBotHandText = "";
            gameState = GameState.SHOWDOWN;
            gameOver = (player.chips <= 0 || bot.chips <= 0);
            playerCheatCount++;
        }
        return caught;
    }

    // --- STANDARD GETTERS ---
    public boolean isShowdownAfterFold() { return isShowdownAfterFold; }
    public boolean isPlayerSmallBlind() { return playerIsSmallBlind; }
    public int getLastPlayerBet() { return lastPlayerBet; }
    public int getLastBotBet() { return lastBotBet; }
    public boolean isGameOver() { return gameOver; }
    public boolean isPlayerTurn() { return isPlayerTurn; }
    public int getCurrentBet() { return currentBet; }
    public int getPlayerChips() { return player.chips; }
    public int getBotChips() { return bot.chips; }
    public int getPot() { return pot; }
    public GameState getGameState() { return gameState; }
    public List<Card> getCommunityCards() { return communityCards; }
    public Player getPlayer() { return player; }
    public Player getBot() { return bot; }
    public Deck getDeck() { return deck; }

    // --- Showdown Texts ---
    public String getShowdownResultText() { return showdownResultText; }
    public String getShowdownPlayerHandText() { return showdownPlayerHandText; }
    public String getShowdownBotHandText() { return showdownBotHandText; }
}
