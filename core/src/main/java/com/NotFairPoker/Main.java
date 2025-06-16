package com.NotFairPoker;

import com.NotFairPoker.screens.MenuScreen;
import com.badlogic.gdx.Game;
import com.NotFairPoker.screens.TableScreen;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
