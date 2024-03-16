package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
    final Drop game;
    Texture dropImage;
    Texture bucketImage;
    Texture background;
    Sound dropSound;
    Music rainMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Rectangle> raindrops;
    long lastDropTime;
    int dropsGathered;

    public GameScreen(final Drop game) {
        this.game = game;

        // Carreguem les imatges de la gota i la galleda (64x64 píxels cadascuna)
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // Carreguem la imatge de fons
        background = new Texture(Gdx.files.internal("backgroundGame.jpg"));

        // Carreguem l'efecte de so de la gota i la "música" de fons de la pluja
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);

        // Creem la càmara i l'SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Creem un Rectangle per representar lògicament la galleda
        bucket = new Rectangle();
        bucket.x = (float) 800 / 2 - (float) 64 / 2; // Centrem la galleda horitzontalment
        bucket.y = 20; // La cantonada inferior esquerra de la galleda es troba 20 píxels per sobre de la vora inferior de la pantalla
        bucket.width = 64;
        bucket.height = 64;

        // Creem l'Array de gotes i spawnegem la primera gota
        raindrops = new Array<>();
        spawnRaindrop();
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        /*
         * Netegem la pantalla amb un color blau fosc. Els arguments per netejar són el component vermell,
         * verd, blau i alfa de l'interval [0,1] del color que s'utilitzarà per esborrar la pantalla.
         */
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // Li diem a la càmara que actualitzi les seves matrius
        camera.update();

        // Li diem al SpriteBatch que es renderitzi en el sistema de coordenades especificat per la càmara
        game.batch.setProjectionMatrix(camera.combined);

        // Comencem un nou lot i dibuixem la galleda i totes les gotes
        game.batch.begin();
        game.batch.draw(background, 0, 0);
        game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 480);
        game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 725, 480);
        game.batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        // Processem l'input de l'usuari
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - (float) 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT))
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.RIGHT))
            bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // Ens assegurem que la galleda es manté dins dels límits de la pantalla
        if (bucket.x < 0)
            bucket.x = 0;
        if (bucket.x > 800 - 64)
            bucket.x = 800 - 64;

        // Comprovem si necessitem crear una nova gota
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
            spawnRaindrop();

        /*
         * Movem les gotes, traiem les que hi hagi sota la vora inferior de la pantalla o
         * que toquin la galleda. En aquest últim cas també reproduïm un efecte de so.
         */
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + 64 < 0) {
                iter.remove();
                rainMusic.stop();
                game.setScreen(new GameOverScreen(game, dropsGathered));
            }
            if (raindrop.overlaps(bucket) && raindrop.y >= bucket.height - 10) {
                dropsGathered++;
                dropSound.play();
                iter.remove();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // Iniciem la reproducció de la música de fons quan es mostra la pantalla
        rainMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        background.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
