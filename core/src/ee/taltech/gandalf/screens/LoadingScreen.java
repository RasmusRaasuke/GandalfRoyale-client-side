package ee.taltech.gandalf.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import ee.taltech.gandalf.GandalfRoyale;
import ee.taltech.gandalf.components.Lobby;
import ee.taltech.gandalf.network.messages.game.GameLoaded;

import java.util.Random;

public class LoadingScreen extends ScreenAdapter {
    private final GandalfRoyale game;
    private final AssetManager assetManager;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Stage stage;
    private final BitmapFont font;
    private final String[] loadingImages = {
            "Loading/loading1.jpg",
            "Loading/loading2.jpg",
            "Loading/loading3.jpg"
    };
    private final String[] loadingTips = {
            "Tip: Mastering movement is key to dodging spells!",
            "Tip: Different spells have unique strengths and weaknesses.",
            "Tip: Look for power-up items to gain an edge!",
            "Tip: Teamwork can make all the difference in a royale!",
            "Tip: Keep an eye on the shrinking play zone to avoid taking damage.",
            "Tip: Don't be afraid to run and heal if you're low on health.",
            "Tip: Use the terrain to your advantage – hide, ambush, or escape."
    };
    private final Lobby lobby;
    private final ExtendViewport viewport;
    private final Image image;
    private final ProgressBar loadingBar;
    private boolean loaded;

    public LoadingScreen(GandalfRoyale game, Lobby lobby) {
        this.lobby = lobby;
        this.game = game;
        assetManager = game.assetManager;
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage = new Stage(viewport);
        font = GandalfRoyale.font;
        // Loading Screen UI Setup
        Table table = new Table();
        table.setFillParent(true);

        loaded = false;

        // 1. Then create and add the loading image

        // Choose random loading screen
        Texture loadingImage = new Texture(loadingImages[new Random().nextInt(loadingImages.length)]);
        image = new Image(loadingImage);
        image.setScaling(Scaling.fillY);
        image.setFillParent(true);
        // 2. Add loading tip label
        Label loadingTipLabel = new Label(getRandomTip(), new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE));
        table.add(loadingTipLabel);
        table.row();

        // 3. Create and add the loading bar
        Skin skin = new Skin(Gdx.files.internal("UI/clean-crispy/skin/clean-crispy-ui.json"));
        loadingBar = new ProgressBar(0f, 1f, 0.01f, false, skin);
        table.add(loadingBar).expand().fillX().pad(40).bottom().width(Value.percentWidth(.6f, table));
        stage.addActor(image);
        // stage.addActor(loadingTipLabel);
        stage.addActor(table);
    }

    /**
     * Method to load in the assets needed in the game.
     */
    @Override
    public void show() {
        assetManager.setLoader(TiledMap.class, new TmxMapLoader());
        assetManager.load("Gandalf_Royale.tmx", TiledMap.class);

        assetManager.load("Spells/Fireball/fireball_book.png", Texture.class);
        assetManager.load("Spells/Plasma/plasma_book.png", Texture.class);
        assetManager.load("Spells/Meteor/meteor_book.png", Texture.class);
        assetManager.load("Spells/Kunai/kunai_book.png", Texture.class);
        assetManager.load("Spells/IceShard/iceshard_book.png", Texture.class);
        assetManager.load("Spells/Poisonball/poisonball_book.png", Texture.class);


        assetManager.load("Spells/Fireball/packFireball.png", Texture.class);
        assetManager.load("Spells/Plasma/packPlasma.png", Texture.class);
        assetManager.load("Spells/Meteor/packMeteor.png", Texture.class);
        assetManager.load("Spells/Kunai/packKunai.png", Texture.class);


        assetManager.load("Potion/potion.png", Texture.class);

        assetManager.load("Coin/Coin_rotating.png", Texture.class);

        assetManager.load("Zone/expected_zone.png", Texture.class);
        assetManager.load("Zone/huge_expected_zone.png", Texture.class);
        assetManager.load("Zone/hugeNewZone.png", Texture.class);
        assetManager.load("Zone/safezone.png", Texture.class);

        assetManager.load("Pumpkin/Pumpkin_Attacking.png", Texture.class);
        assetManager.load("Pumpkin/Pumpkin_Walks.png", Texture.class);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Iterate the loading queue
        assetManager.update();
        if (assetManager.isFinished() && !loaded) {
            System.out.println("finished loading");
            loaded = true;
            game.screenController.loadGameScreen(lobby);
        }
        // Update loading status
        float progress = assetManager.getProgress();
        loadingBar.setValue(progress); // Change the progress bar
        viewport.apply();
        stage.act(delta);
        stage.draw();
    }

    private String getRandomTip() {
        Random random = new Random();
        return loadingTips[random.nextInt(loadingTips.length)];
    }

    /**
     * Correct elements size when resizing a window.
     *
     * @param width  window width
     * @param height window height
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        image.invalidate();
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        font.dispose();
    }

    public Lobby getLobby() {
        return lobby;
    }
}
