package com.badlogic.drop;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Drop extends Game {
	public SpriteBatch batch;
	public BitmapFont font;

	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont(); // Fem servir la font Arial per defecte de libGDX
		this.setScreen(new MainMenuScreen(this));
	}

	public void render() {
		super.render(); // Important!
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
	}
}
