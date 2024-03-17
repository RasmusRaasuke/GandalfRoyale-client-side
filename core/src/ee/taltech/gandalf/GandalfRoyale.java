package ee.taltech.gandalf;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ee.taltech.gandalf.network.NetworkClient;
import ee.taltech.gandalf.screens.ScreenController;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.taltech.network.NetworkClient;
import ee.taltech.screen.ScreenController;

public class GandalfRoyale extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public NetworkClient nc;
    public ScreenController screenController;

    /**
     * Create game instance.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        screenController = new ScreenController(this);

        try {
            nc = new NetworkClient(screenController);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        screenController.setMenuScreen(); // Set MenuScreen as the first screen.
    }

    /**
     * Dispose game.
     */
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
