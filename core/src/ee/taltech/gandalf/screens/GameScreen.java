package ee.taltech.gandalf.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import ee.taltech.gandalf.GandalfRoyale;
import ee.taltech.gandalf.entities.Item;
import ee.taltech.gandalf.entities.PlayerCharacterAnimator;
import ee.taltech.gandalf.entities.collision.CollisionHandler;
import ee.taltech.gandalf.components.StartedGame;
import ee.taltech.gandalf.network.NetworkClient;
import ee.taltech.gandalf.network.messages.game.MouseClicks;
import ee.taltech.gandalf.entities.PlayerCharacter;
import ee.taltech.gandalf.input.PlayerInput;
import ee.taltech.gandalf.entities.Spell;
import ee.taltech.gandalf.components.SpellTypes;
import ee.taltech.gandalf.components.Lobby;
import ee.taltech.gandalf.scenes.Hud;
import ee.taltech.gandalf.world.WorldCollision;

import java.util.Map;

public class GameScreen extends ScreenAdapter {

    private final World world;
    private final CollisionHandler collisionHandler;
    GandalfRoyale game;
    NetworkClient nc;
    private final OrthogonalTiledMapRenderer renderer;
    private final ExtendViewport viewport;
    public final OrthographicCamera camera;
    private final Hud hud;
    public final StartedGame startedGame;
    private final Map<Integer, PlayerCharacter> alivePlayers;
    private final PlayerCharacter clientCharacter;
    private final Map<Integer, Spell> spells;
    private final Map<Integer, Item> items;
    private final TmxMapLoader mapLoader;
    private final TiledMap map;
    private Texture img;
    private static Texture fireballTexture;
    private static Texture fireballBook;
    private static Texture wizard;
    private float elapsedTime;
    MouseClicks mouseClicks;
    private int deathAnimationCalls = 0;

    private final ShapeRenderer shapeRenderer;

    private Box2DDebugRenderer debugRenderer; // For debugging
    private TextureRegion currentFrame;

    /**
     * Construct GameScreen.
     *
     * @param game GandalfRoyale game instance
     */
    public GameScreen(GandalfRoyale game, Lobby lobby) {
        world = new World(new Vector2(0, 0), true); // Create a new Box2D world
        collisionHandler = new CollisionHandler();
        world.setContactListener(new CollisionHandler());

        this.game = game;
        this.nc = game.nc;

        setTextures();

        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        viewport = new ExtendViewport(150, 150, camera);
        viewport.apply();

        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("Gandalf_Royale.tmx");
        this.renderer = new OrthogonalTiledMapRenderer(map);

        new WorldCollision(world, map);

        startedGame = new StartedGame(game, lobby, world);
        alivePlayers = startedGame.getAlivePlayers();
        clientCharacter = startedGame.getClientCharacter();
        spells = startedGame.getSpells();
        items = startedGame.getItems();

        hud = new Hud(camera, clientCharacter);

        shapeRenderer = new ShapeRenderer();
        debugRenderer = new Box2DDebugRenderer();
    }

    /**
     * Set all textures.
     */
    private static void setTextures() {
        fireballBook = new Texture("fireball_book.png");
        fireballTexture = new Texture("spell1_Fireball.png");
    }

    /**
     * Set the specific wizards texture.
     * Useful because different wizards have different textures.
     */
    public static Texture getWizardTexture(int userID) {
        // Choose one of the spritesheets for the character
        String filename = "wizardTexture" + userID % 10 + ".png";
        return new Texture(filename);
    }

    /**
     * Draw all player character to screen.
     */
    private void drawPlayers() {
        for (PlayerCharacter player : alivePlayers.values()) {
            PlayerCharacterAnimator playerAnimator = player.getPlayerAnimator();
            elapsedTime += Gdx.graphics.getDeltaTime();
            if (player.health() == 0) {
                if (deathAnimationCalls >= 5) {
                    currentFrame = playerAnimator.deathAnimation().getKeyFrames()[playerAnimator.deathAnimation().getKeyFrames().length - 1];
                } else {
                    // Play the death animation
                    currentFrame = playerAnimator.deathAnimation().getKeyFrame(elapsedTime, true);
                    deathAnimationCalls++;
                }
            } else if (playerAnimator.action()) {
                currentFrame = playerAnimator.actionAnimation().getKeyFrame(elapsedTime, true);
            } else if (playerAnimator.isMoving()) {
                currentFrame = playerAnimator.movementAnimation().getKeyFrame(elapsedTime, true);
            } else {
                currentFrame = playerAnimator.idleAnimation().getKeyFrame(elapsedTime, true);
            }


            game.batch.begin();
            // Set the image to 3 times smaller picture and flip it, if player is moving left.
            if (playerAnimator.lookRight()) {
                game.batch.draw(currentFrame, player.xPosition - 30, player.yPosition - 15, 80, 80);
            } else {
                game.batch.draw(currentFrame, player.xPosition - 40, player.yPosition - 15, 80, 80);
            }
            game.batch.end();
            // Draw health and mana bar
            drawBars(player);
        }
    }

    /**
     * Draw health and mana bar for player.
     *
     * @param player any PlayerCharacter
     */
    private void drawBars(PlayerCharacter player) {
        // Render shapes
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw missing health bar
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(player.xPosition - 45, player.yPosition + 60,
                (float) 100, 2);

        // Draw health bar
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(player.xPosition - 45, player.yPosition + 60,
                (float) player.health(), 2);

        // Draw missing mana bar
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(player.xPosition - 45, player.yPosition + 55,
                (float) 100, 2);

        // Draw mana bar
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(player.xPosition - 45, player.yPosition + 55,
                (float) player.mana(), 2);

        // Stop rendering shapes
        shapeRenderer.end();
    }

    /**
     * Draw spells.
     */
    private void drawSpells() {
        for (Spell spell : spells.values()) {
            if ((spell.getType() == SpellTypes.FIREBALL) && (spell.rotation().isPresent())) {
                game.batch.begin();
                TextureRegion currentFrame = spell.getFireballAnimation().getKeyFrame(elapsedTime, true);
                game.batch.draw(currentFrame,
                        (float) spell.getXPosition(),
                        (float) spell.getYPosition(),
                        0,
                        0,
                        32,
                        17,
                        1,
                        1,
                        spell.rotation().get());
                game.batch.end();
            }
        }
    }

    /**
     * Draw items that are on the ground.
     */
    private void drawItems() {
        for (Item item : items.values()) {
            if (item.getType() == SpellTypes.FIREBALL) {
                game.batch.begin();
                game.batch.draw(fireballBook, item.getXPosition() - (float) fireballBook.getWidth() / 3,
                        item.getYPosition() - (float) fireballBook.getHeight() / 3,
                        fireballBook.getWidth(), fireballBook.getHeight());
                game.batch.end();
            }
        }
    }

    /**
     * Disable players movement aka don't listen to input.
     */
    public void disableClientPlayerCharacter() {
        Gdx.input.setInputProcessor(null);
    }

    /**
     * Texture types
     */
    public enum TextureType {
        FIREBALL_BOOK, FIREBALL, WIZARD
    }

    /**
     * Get any texture.
     * Give the appropriate type upon calling this method to get the right texture.
     * @return fireballBook
     */
    public static Texture getTexture(TextureType textureType) {
        switch (textureType) {
            case FIREBALL_BOOK:
                return fireballBook;
            case FIREBALL:
                return fireballTexture;
            case WIZARD:
                return wizard;
            default:
                return new Texture("wizard.png");
        }
    }

    /**
     * Show screen on initialisation.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(new PlayerInput(game, clientCharacter));
    }

    /**
     * Render the screen aka update every frame.
     *
     * @param delta time
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 0);
        world.step(delta, 6, 2);
        startedGame.update(delta);

        // Update camera position to follow player
        camera.position.x = clientCharacter.xPosition;
        camera.position.y = clientCharacter.yPosition;

        // Update camera matrices
        camera.update();

        // Set camera projection matrix
        game.batch.setProjectionMatrix(camera.combined);

        // Render game objects
        game.batch.begin();
        camera.zoom = 0.7f; // To render 2X bigger area than seen.
        renderer.setView(camera);
        renderer.render();
        camera.zoom = 0.35f; // Reset the camera back to its original state.
        game.batch.end();

        drawPlayers(); // Draw client character.
        if (!spells.isEmpty()) {
            drawSpells(); // Draw spells.
        }
        drawItems();

        hud.draw();

        debugRenderer.render(world, camera.combined);
    }

    /**
     * Correct camera position when resizing window.
     *
     * @param width window width
     * @param height window height
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hud.resize(width, height);
    }

    /**
     * Hide screen.
     */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        img.dispose();
        world.dispose();
        debugRenderer.dispose();
        hud.dispose();
    }
}
