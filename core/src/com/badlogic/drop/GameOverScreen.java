package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen implements Screen {
    final Drop game;
    int dropsGathered;
    Texture background;
    OrthographicCamera camera;
    Music endMusic;

    public GameOverScreen(final Drop game, int dropsGathered) {
        this.game = game;
        this.dropsGathered = dropsGathered;
        background = new Texture(Gdx.files.internal("backgroundGameOver.jpg"));
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        endMusic = Gdx.audio.newMusic(Gdx.files.internal("end.wav"));
        endMusic.play();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0);
        game.font.draw(game.batch, "Game Over", 100, 200);

        switch (dropsGathered) {
            case 0:
                game.font.draw(game.batch, "You haven't collected any drop :(", 100, 150);
                break;
            case 1:
                game.font.draw(game.batch, "You have collected a single drop :)", 100, 150);
                break;
            default:
                game.font.draw(game.batch, "You have collected " + dropsGathered + " drops!!!", 100, 150);
                break;
        }

        game.font.draw(game.batch, "Tap anywhere to start again!", 100, 100);
        game.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        background.dispose();
        endMusic.dispose();
    }
}
