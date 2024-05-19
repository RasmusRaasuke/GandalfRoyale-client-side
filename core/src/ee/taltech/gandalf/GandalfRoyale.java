package ee.taltech.gandalf;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.taltech.gandalf.network.NetworkClient;
import ee.taltech.gandalf.screens.ScreenController;

public class GandalfRoyale extends Game {
    public static BitmapFont font;
    public SpriteBatch batch;
    public Viewport viewport;
    public NetworkClient nc;
    public AssetManager assetManager;

    public ScreenController screenController() {
        return screenController;
    }

    public ScreenController screenController;

    /**
     * Create game instance.
     */
    @Override
    public void create() {
        assetManager = new AssetManager();
        batch = new SpriteBatch();
        viewport = new ScreenViewport();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Font/WizardWorldSimplified.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        font = generator.generateFont(parameter);
        generator.dispose();

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
