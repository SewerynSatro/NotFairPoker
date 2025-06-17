package com.NotFairPoker.screens;

import com.NotFairPoker.Main;
import com.NotFairPoker.GameState;
import com.NotFairPoker.logic.*;
import com.NotFairPoker.audio.MusicManager;
import com.NotFairPoker.audio.SoundManager;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.List;

public class TableScreen implements Screen {
    private final Main game;
    private final String playerName;
    private final int selectedAvatar;

    private SpriteBatch batch;
    private BitmapFont font;
    private Texture tableBg, avatarPlayer, avatarBot, cardBack, dealer;
    private Texture playerCard1, playerCard2, botCard1, botCard2;
    private Texture sliderBg, sliderKnob, confirmRaise;
    private Texture label, botLabel;
    private Texture statsPlayer, statsBot;
    private Texture actionBotCall, actionBotRaise, actionBotFold, actionBotCheck;
    private Texture currentAction;
    private List<Texture> communityTextures = new ArrayList<>();

    private Stage stage;
    private Slider raiseSlider;
    private ImageButton confirmRaiseButton;
    private ImageButton newHandButton, restartButton;
    private ImageButton checkButton, callButton, raiseButton, foldButton, pauseButton;
    private boolean initialized = false;
    private int currentRaiseAmount;
    private GameManager gameManager;

    // ------ CHEAT: Zamiana na asa ------
    private ImageButton cheatButton;
    private boolean cheatUsed = false; // raz na rozdanie
    private List<ImageButton> playerCardButtons = new ArrayList<>();
    private boolean cheatCaught = false; // czy bot wykrył cheata (zamiana na asa)

    // ------ CHEAT: Podglądanie karty ------
    private ImageButton peekButton;
    private boolean peekCheatCaught = false;
    private Image peekedBotCardImage = null;

    public TableScreen(Main game, String playerName, int selectedAvatar) {
        this.game = game;
        this.playerName = playerName;
        this.selectedAvatar = selectedAvatar;
    }

    @Override
    public void show() {
        if (!initialized) {
            batch = new SpriteBatch();
            font = new BitmapFont();
            font.getData().setScale(2f);

            MusicManager.getInstance().playGameMusic();

            gameManager = new GameManager(this);
            gameManager.startNewHand();

            tableBg = new Texture(Gdx.files.internal("poker_table_bg.png"));
            avatarPlayer = new Texture(Gdx.files.internal("avatars/" + selectedAvatar + ".png"));
            avatarBot = new Texture(Gdx.files.internal("avatars/3.png"));
            cardBack = new Texture(Gdx.files.internal("cards/card_back.png"));
            label = new Texture(Gdx.files.internal("avatars/"+selectedAvatar+"label.png"));
            botLabel = new Texture(Gdx.files.internal("avatars/botLabel.png"));
            statsPlayer = new Texture(Gdx.files.internal("stats1.png"));
            statsBot = new Texture(Gdx.files.internal("stats2.png"));

            actionBotCall = new Texture(Gdx.files.internal("botAction/call.png"));
            actionBotRaise = new Texture(Gdx.files.internal("botAction/raise.png"));
            actionBotCheck = new Texture(Gdx.files.internal("botAction/check.png"));
            actionBotFold = new Texture(Gdx.files.internal("botAction/fold.png"));
            currentAction = null;

            sliderBg = new Texture(Gdx.files.internal("slider_bg.png"));
            sliderKnob = new Texture(Gdx.files.internal("slider_knob.png"));
            confirmRaise = new Texture(Gdx.files.internal("buttons/confirm.png"));
            dealer = new Texture(Gdx.files.internal("dealer.png"));

            stage = new Stage(new ScreenViewport());
            Gdx.input.setInputProcessor(stage);

            createButtons();
            createCheatButton();
            createPeekButton();
            createNewHandButton();
            createRestartButton();
            updateCardTextures();

            // Widoczność cheatów dla konkretnych postaci
            if (selectedAvatar == 1) {
                peekButton.setVisible(true);
                cheatButton.setVisible(false);
            } else if (selectedAvatar == 2) {
                peekButton.setVisible(false);
                cheatButton.setVisible(true);
            } else {
                peekButton.setVisible(false);
                cheatButton.setVisible(false);
            }

            initialized = true;
        }
    }

    private void playClickSound() {
        SoundManager.getInstance().playClick();
    }

    private void createButtons() {
        Texture checkTex = new Texture(Gdx.files.internal("buttons/check.png"));
        Texture callTex = new Texture(Gdx.files.internal("buttons/call.png"));
        Texture raiseTex = new Texture(Gdx.files.internal("buttons/raise.png"));
        Texture foldTex = new Texture(Gdx.files.internal("buttons/fold.png"));
        Texture pauseTex = new Texture(Gdx.files.internal("buttons/pause.png"));

        checkButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(checkTex)));
        callButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(callTex)));
        raiseButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(raiseTex)));
        foldButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(foldTex)));
        pauseButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(pauseTex)));

        int xButton = 50;
        int yButton = 300;

        checkButton.setPosition(xButton, yButton + 200);
        callButton.setPosition(xButton, yButton + 200);
        raiseButton.setPosition(xButton, yButton + 100);
        foldButton.setPosition(xButton, yButton);
        pauseButton.setPosition(1700, 100);

        checkButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager.playerAction(GameManager.PlayerAction.CHECK, 0);
                updateCommunityCardTextures();
            }
        });
        callButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager.playerAction(GameManager.PlayerAction.CALL, 0);
                updateCommunityCardTextures();
            }
        });
        raiseButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                showRaiseSlider();
            }
        });
        foldButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager.playerAction(GameManager.PlayerAction.FOLD, 0);
                updateCardTextures();
            }
        });
        pauseButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                game.setScreen(new PauseScreen(game, TableScreen.this));
            }
        });

        stage.addActor(checkButton);
        stage.addActor(callButton);
        stage.addActor(raiseButton);
        stage.addActor(foldButton);
        stage.addActor(pauseButton);
    }

    // ------- CHEAT BUTTON: ZAMIANA NA ASA --------
    private void createCheatButton() {
        Texture cheatTex = new Texture(Gdx.files.internal("buttons/cheat.png"));
        cheatButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(cheatTex)));
        cheatButton.setPosition(850, 50);
        cheatButton.setVisible(false);
        cheatButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                if (!cheatUsed) {
                    enableCheatSelectMode();
                }
            }
        });
        stage.addActor(cheatButton);
    }

    private void enableCheatSelectMode() {
        Player player = gameManager.getPlayer();
        removePlayerCardButtons();
        playerCardButtons.clear();
        for (int i = 0; i < player.hand.size(); i++) {
            final int index = i;
            String cardTexturePath = player.hand.getCard(i).getTexturePath();
            Texture cardTexture = new Texture(Gdx.files.internal(cardTexturePath));
            ImageButton cardButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(cardTexture)));
            int xPosition = 450 + i * 200;
            int yPosition = 50;

            cardButton.setPosition(xPosition, yPosition);
            cardButton.setSize(162, 200);

            cardButton.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    playClickSound();
                    useCheat(index);
                }
            });
            stage.addActor(cardButton);
            playerCardButtons.add(cardButton);
        }
    }

    private void useCheat(int cardIndex) {
        if (cheatUsed) return;
        cheatUsed = true;
        removePlayerCardButtons();

        Player player = gameManager.getPlayer();
        player.hand.swapCardForAce(cardIndex);
        cheatButton.setVisible(false);
        updateCardTextures();

        // Wykrycie przez bota:
        cheatCaught = gameManager.tryBotDetectCheat();
        if (cheatCaught) {
            updateCardTextures();
        }
    }

    private void removePlayerCardButtons() {
        for (ImageButton b : playerCardButtons) {
            b.remove();
        }
        playerCardButtons.clear();
    }
    // -----------------------------------

    // ------- CHEAT BUTTON: PODGLĄDANIE --------
    private void createPeekButton() {
        Texture peekTex = new Texture(Gdx.files.internal("buttons/peek.png"));
        peekButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(peekTex)));
        peekButton.setPosition(850, 50);
        peekButton.setVisible(true);
        peekButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                usePeekCheat();
            }
        });
        stage.addActor(peekButton);
    }

    private void usePeekCheat() {
        Player bot = gameManager.getBot();
        Card cardToPeek = bot.hand.getCard(0); // Możesz dać wybór, tu zawsze pierwsza karta
        showPeekedBotCard(cardToPeek);

        peekCheatCaught = gameManager.tryBotDetectPeekCheat();
    }

    private void showPeekedBotCard(Card card) {
        if (peekedBotCardImage != null) {
            peekedBotCardImage.remove();
        }
        peekedBotCardImage = new Image(new Texture(Gdx.files.internal(card.getTexturePath())));
        peekedBotCardImage.setPosition(1320, 830);
        peekedBotCardImage.setSize(160, 200);
        stage.addActor(peekedBotCardImage);

        // Usunięcie po 2 sekundach
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (peekedBotCardImage != null) {
                    peekedBotCardImage.remove();
                    peekedBotCardImage = null;
                }
            }
        }, 2f);
    }
    // -----------------------------------

    private void createNewHandButton() {
        Texture newHandTex = new Texture(Gdx.files.internal("buttons/newhand.png"));
        newHandButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(newHandTex)));
        newHandButton.setPosition(900, 50);
        newHandButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager.startNewHand();
                cheatUsed = false;
                cheatCaught = false;
                peekCheatCaught = false;
                updateCardTextures();
            }
        });
        stage.addActor(newHandButton);
        newHandButton.setVisible(false);
    }

    private void createRestartButton() {
        Texture restartTex = new Texture(Gdx.files.internal("buttons/restart.png"));
        restartButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(restartTex)));
        restartButton.setPosition(900, 150);
        restartButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager = new GameManager(TableScreen.this);
                gameManager.startNewHand();
                cheatUsed = false;
                cheatCaught = false;
                peekCheatCaught = false;
                updateCardTextures();
                restartButton.setVisible(false);
            }
        });
        stage.addActor(restartButton);
        restartButton.setVisible(false);
    }

    private void showRaiseSlider() {
        if (raiseSlider != null) raiseSlider.remove();
        if (confirmRaiseButton != null) confirmRaiseButton.remove();

        raiseSlider = new Slider(20, gameManager.getPlayerChips(), 10, false, new Slider.SliderStyle(
            new TextureRegionDrawable(new TextureRegion(sliderBg)),
            new TextureRegionDrawable(new TextureRegion(sliderKnob))
        ));
        raiseSlider.setValue(20);
        raiseSlider.setPosition(180, 400);
        raiseSlider.setSize(350, 50);
        stage.addActor(raiseSlider);

        raiseSlider.addListener(event -> {
            currentRaiseAmount = (int) raiseSlider.getValue();
            return false;
        });

        confirmRaiseButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(confirmRaise)));
        confirmRaiseButton.setPosition(50, 400);
        confirmRaiseButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager.playerAction(GameManager.PlayerAction.RAISE, currentRaiseAmount);
                updateCardTextures();
                raiseSlider.remove();
                confirmRaiseButton.remove();
            }
        });
        stage.addActor(confirmRaiseButton);
    }

    private void updateCardTextures() {
        disposeCardTextures();
        Player player = gameManager.getPlayer();
        Player bot = gameManager.getBot();
        playerCard1 = new Texture(Gdx.files.internal(player.hand.getCard(0).getTexturePath()));
        playerCard2 = new Texture(Gdx.files.internal(player.hand.getCard(1).getTexturePath()));
        botCard1 = new Texture(Gdx.files.internal(bot.hand.getCard(0).getTexturePath()));
        botCard2 = new Texture(Gdx.files.internal(bot.hand.getCard(1).getTexturePath()));
        updateCommunityCardTextures();

        // CheatButton pojawia się na początku rozdania, jeśli nie był użyty i nie jest showdown
        if (!cheatUsed && gameManager.getGameState() != GameState.SHOWDOWN && !gameManager.isGameOver() && selectedAvatar == 2) {
            cheatButton.setVisible(true);
        } else {
            cheatButton.setVisible(false);
        }
    }

    private void updateCommunityCardTextures() {
        for (Texture t : communityTextures) t.dispose();
        communityTextures.clear();
        for (Card c : gameManager.getCommunityCards()) {
            communityTextures.add(new Texture(Gdx.files.internal(c.getTexturePath())));
        }
    }

    private void disposeCardTextures() {
        if (playerCard1 != null) playerCard1.dispose();
        if (playerCard2 != null) playerCard2.dispose();
        if (botCard1 != null) botCard1.dispose();
        if (botCard2 != null) botCard2.dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0.4f, 0, 1);

        batch.begin();
        batch.draw(tableBg, 0, 0);
        batch.draw(avatarPlayer, 50, 50, 160, 200);
        batch.draw(label, 50, 20, 160, 50);
        batch.draw(statsPlayer, 250, 50, 160, 200);
        batch.draw(avatarBot, 1710, 830, 160, 200);
        batch.draw(botLabel, 1710, 800, 160, 50);
        batch.draw(statsBot, 1520, 830, 160, 200);
        batch.draw(playerCard1, 450, 50, 160, 200);
        batch.draw(playerCard2, 650, 50, 160, 200);

        if (gameManager.getGameState() == GameState.SHOWDOWN) {
            newHandButton.setVisible(true);
            checkButton.setVisible(false);
            callButton.setVisible(false);
            raiseButton.setVisible(false);
            foldButton.setVisible(false);
            if (!gameManager.isShowdownAfterFold()) {
                batch.draw(botCard1, 1320, 830, 160, 200);
                batch.draw(botCard2, 1120, 830, 160, 200);
            } else {
                batch.draw(cardBack, 1320, 830, 160, 200);
                batch.draw(cardBack, 1120, 830, 160, 200);
            }
        } else {
            newHandButton.setVisible(false);
            int currentBet = gameManager.getCurrentBet();
            int lastPlayerBet = gameManager.getLastPlayerBet();
            checkButton.setVisible(currentBet == lastPlayerBet);
            callButton.setVisible(currentBet != lastPlayerBet);
            raiseButton.setVisible(true);
            foldButton.setVisible(true);
            batch.draw(cardBack, 1320, 830, 160, 200);
            batch.draw(cardBack, 1120, 830, 160, 200);
        }

        for (int i = 0; i < communityTextures.size(); i++) {
            batch.draw(communityTextures.get(i), 465 + i * 200, 430, 192, 241);
        }

        font.draw(batch, "" + gameManager.getPlayerChips(), 310, 200);
        font.draw(batch, "" + gameManager.getBotChips(), 1580, 980);
        font.draw(batch, "" + gameManager.getLastPlayerBet(), 310, 115);
        font.draw(batch, "" + gameManager.getLastBotBet(), 1580, 895);
        font.draw(batch, "CURRENT POT: " + gameManager.getPot(), 900, 860);

        if (gameManager.isPlayerSmallBlind()) {
            batch.draw(dealer, 50, 230);
        } else {
            batch.draw(dealer, 1710, 1010);
        }

        if (currentAction != null) {
            batch.draw(currentAction, 1710, 750, 160, 50);
        }

        if (raiseSlider != null && raiseSlider.hasParent()) {
            font.draw(batch, "" + currentRaiseAmount, 210, 480);
        }

        if (gameManager.isGameOver()) {
            font.draw(batch, "GAME OVER!", 800, 700);
            newHandButton.setVisible(false);
            checkButton.setVisible(false);
            callButton.setVisible(false);
            raiseButton.setVisible(false);
            foldButton.setVisible(false);
            restartButton.setVisible(true);
        }

        // WYŚWIETL komunikat o złapaniu cheatera - zamiana na asa
        if (cheatCaught && gameManager.getGameState() == GameState.SHOWDOWN) {
            font.getData().setScale(3f);
            font.setColor(Color.RED);
            font.draw(batch, "Zostałeś złapany na cheatowaniu!\nBot wygrywa całą pulę!", 350, 400);
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
        }
        // WYŚWIETL komunikat o złapaniu przy podglądaniu
        if (peekCheatCaught && gameManager.getGameState() == GameState.SHOWDOWN) {
            font.getData().setScale(3f);
            font.setColor(Color.ORANGE);
            font.draw(batch, "Bot złapał Cię na podglądaniu karty!\nBot wygrywa całą pulę!", 350, 330);
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
        }

        batch.end();
        stage.act(delta);
        stage.draw();

        if (!gameManager.isPlayerTurn() && gameManager.getGameState() != GameState.SHOWDOWN && !gameManager.isGameOver()) {
            gameManager.botTurn();
            updateCommunityCardTextures();
        }
    }

    public void startNewGame() {
        gameManager = new GameManager(TableScreen.this);
        gameManager.setTableScreen(this);
        gameManager.startNewHand();
        cheatUsed = false;
        cheatCaught = false;
        peekCheatCaught = false;
        updateCardTextures();
    }

    public void regainInputFocus() {
        Gdx.input.setInputProcessor(stage);
    }

    public void setBotDecisionTexture(String decision) {
        if (decision == null) {
            currentAction = null;
            return;
        }
        switch (decision) {
            case "CHECK" -> currentAction = actionBotCheck;
            case "CALL" -> currentAction = actionBotCall;
            case "RAISE" -> currentAction = actionBotRaise;
            case "FOLD" -> currentAction = actionBotFold;
            default -> currentAction = null;
        }
    }

    // --- WYMAGANE PRZEZ Screen ---
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        disposeCardTextures();
        cardBack.dispose();
        avatarPlayer.dispose();
        avatarBot.dispose();
        tableBg.dispose();
        stage.dispose();
        for (Texture t : communityTextures) t.dispose();
    }
}
