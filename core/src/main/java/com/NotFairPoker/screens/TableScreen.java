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
    private Texture sliderBgTex, sliderKnobTex, confirmRaiseTex;
    private List<Texture> communityTextures = new ArrayList<>();

    private Stage stage;
    private Slider raiseSlider;
    private ImageButton confirmRaiseButton;
    private ImageButton newHandButton, restartButton;
    private ImageButton checkButton, callButton, raiseButton, foldButton, pauseButton;
    private boolean initialized = false;
    private int currentRaiseAmount;
    private GameManager gameManager;

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

            gameManager = new GameManager();
            gameManager.startNewHand();

            tableBg = new Texture(Gdx.files.internal("poker_table_bg.png"));
            avatarPlayer = new Texture(Gdx.files.internal("avatars/" + selectedAvatar + ".png"));
            avatarBot = new Texture(Gdx.files.internal("avatars/3.png"));
            cardBack = new Texture(Gdx.files.internal("cards/card_back.png"));

            sliderBgTex = new Texture(Gdx.files.internal("slider_bg.png"));
            sliderKnobTex = new Texture(Gdx.files.internal("slider_knob.png"));
            confirmRaiseTex = new Texture(Gdx.files.internal("buttons/confirm.png"));
            dealer = new Texture(Gdx.files.internal("dealer.png"));

            stage = new Stage(new ScreenViewport());
            Gdx.input.setInputProcessor(stage);

            createButtons();
            createNewHandButton();
            createRestartButton();
            updateCardTextures();

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

        checkButton.setPosition(xButton, yButton + 300);
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

    private void createNewHandButton() {
        Texture newHandTex = new Texture(Gdx.files.internal("buttons/newhand.png"));
        newHandButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(newHandTex)));
        newHandButton.setPosition(900, 50);
        newHandButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager.startNewHand();
                updateCardTextures();
            }
        });
        stage.addActor(newHandButton);
        newHandButton.setVisible(false);
    }

    private void createRestartButton() {
        Texture restartTex = new Texture(Gdx.files.internal("buttons/confirm.png"));
        restartButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(restartTex)));
        restartButton.setPosition(900, 150);
        restartButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                playClickSound();
                gameManager = new GameManager();
                gameManager.startNewHand();
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
            new TextureRegionDrawable(new TextureRegion(sliderBgTex)),
            new TextureRegionDrawable(new TextureRegion(sliderKnobTex))
        ));
        raiseSlider.setValue(20);
        raiseSlider.setPosition(210, 400);
        raiseSlider.setSize(300, 50);
        stage.addActor(raiseSlider);

        raiseSlider.addListener(event -> {
            currentRaiseAmount = (int) raiseSlider.getValue();
            return false;
        });

        confirmRaiseButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(confirmRaiseTex)));
        confirmRaiseButton.setPosition(530, 400);
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
        playerCard1 = new Texture(Gdx.files.internal(player.hand.get(0).getTexturePath()));
        playerCard2 = new Texture(Gdx.files.internal(player.hand.get(1).getTexturePath()));
        botCard1 = new Texture(Gdx.files.internal(bot.hand.get(0).getTexturePath()));
        botCard2 = new Texture(Gdx.files.internal(bot.hand.get(1).getTexturePath()));
        updateCommunityCardTextures();
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
        batch.draw(avatarBot, 1710, 830, 160, 200);
        batch.draw(playerCard1, 250, 50, 160, 200);
        batch.draw(playerCard2, 440, 50, 160, 200);

        if (gameManager.getGameState() == GameState.SHOWDOWN) {
            newHandButton.setVisible(true);
            checkButton.setVisible(false);
            callButton.setVisible(false);
            raiseButton.setVisible(false);
            foldButton.setVisible(false);
            if (!gameManager.isShowdownAfterFold()) {
                batch.draw(botCard1, 1520, 830, 160, 200);
                batch.draw(botCard2, 1320, 830, 160, 200);
            } else {
                batch.draw(cardBack, 1520, 830, 160, 200);
                batch.draw(cardBack, 1320, 830, 160, 200);
            }
        } else {
            newHandButton.setVisible(false);
            int currentBet = gameManager.getCurrentBet();
            int lastPlayerBet = gameManager.getLastPlayerBet();
            checkButton.setVisible(currentBet == lastPlayerBet);
            callButton.setVisible(currentBet != lastPlayerBet);
            raiseButton.setVisible(true);
            foldButton.setVisible(true);
            batch.draw(cardBack, 1520, 830, 160, 200);
            batch.draw(cardBack, 1320, 830, 160, 200);
        }

        for (int i = 0; i < communityTextures.size(); i++) {
            batch.draw(communityTextures.get(i), 465 + i * 200, 430, 192, 241);
        }

        font.draw(batch, playerName + " chips: " + gameManager.getPlayerChips(), 50, 40);
        font.draw(batch, "Bot chips: " + gameManager.getBotChips(), 1710, 820);
        font.draw(batch, playerName + " bet: " + gameManager.getLastPlayerBet(), 300, 40);
        font.draw(batch, "Bot bet: " + gameManager.getLastBotBet(), 1710, 740);
        font.draw(batch, "Pula: " + gameManager.getPot(), 900, 600);

        if (gameManager.isPlayerSmallBlind()) {
            batch.draw(dealer, 50, 230);
        } else {
            batch.draw(dealer, 1710, 1010);
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

        batch.end();
        stage.act(delta);
        stage.draw();

        if (!gameManager.isPlayerTurn() && gameManager.getGameState() != GameState.SHOWDOWN && !gameManager.isGameOver()) {
            gameManager.botTurn();
            updateCommunityCardTextures();
        }
    }

    public void startNewGame() {
        gameManager = new GameManager();
        gameManager.startNewHand();
        updateCardTextures();
    }

    public void regainInputFocus() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
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
