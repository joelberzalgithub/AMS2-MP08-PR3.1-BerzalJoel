package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Drop extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private long lastDropTime;

	@Override
	public void create() {
		// Carreguem les imatges de la gota i la galleda (64x64 píxels cadascuna)
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// Carreguem l'efecte de so de la gota i la "música" de fons de la pluja
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// Iniciem la reproducció de la música de fons inmediatament
		rainMusic.setLooping(true);
		rainMusic.play();

		// Creem la càmara de l'SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		// Creem un Rectangle per representar lògicament la galleda
		bucket = new Rectangle();
		bucket.x = (float) 800 / 2 - (float) 64 / 2; // Centrem la galleda horitzontalment
		bucket.y = 20; // La cantonada inferior esquerra de la galleda es troba 20 píxels per sobre de la vora inferior de la pantalla
		bucket.width = 64;
		bucket.height = 64;

		// Creem l'Array de gotes i spawnegem la primera gota de pluja
		raindrops = new Array<>();
		spawnRaindrop();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render() {
		/*
		 * Netegem la pantalla amb un color blau fosc. Els arguments per netejar són el component vermell,
		 * verd, blau i alfa de l'interval [0,1] del color que s'utilitzarà per esborrar la pantalla.
		 */
		ScreenUtils.clear(0, 0, 0.2f, 1);

		// Li diem a la càmara que actualitzi les seves matrius
		camera.update();

		// Li diem al SpriteBatch que es renderitzi en el sistema de coordenades especificat per la càmara
		batch.setProjectionMatrix(camera.combined);

		// Comencem un nou lot i dibuixem la galleda i totes les gotes
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		// Processem l'input de l'usuari
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - (float) 64 / 2;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		// Ens assegurem que la galleda es manté dins dels límits de la pantalla
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		// Comprovem si necessitem crear una nova gota
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		/*
		 * Movem les gotes, traiem les que hi hagi sota la vora inferior de la pantalla o
		 * que toquin la galleda. En aquest últim cas també reproduïm un efecte de so.
		 */
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0) iter.remove();
			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}
	}

	@Override
	public void dispose() {
		// Disposem de tots els recursos natius
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
