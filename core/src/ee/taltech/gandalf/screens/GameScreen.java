package ee.taltech.gandalf.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import ee.taltech.gandalf.GandalfRoyale;
import ee.taltech.gandalf.components.Constants;
import ee.taltech.gandalf.components.GameObjectComparator;
import ee.taltech.gandalf.components.ItemTypes;
import ee.taltech.gandalf.components.Lobby;
import ee.taltech.gandalf.components.PlayZone;
import ee.taltech.gandalf.components.StartedGame;
import ee.taltech.gandalf.components.TextureType;
import ee.taltech.gandalf.entities.*;
import ee.taltech.gandalf.entities.collision.CollisionHandler;
import ee.taltech.gandalf.input.PlayerInput;
import ee.taltech.gandalf.network.NetworkClient;
import ee.taltech.gandalf.network.messages.game.GameLoaded;
import ee.taltech.gandalf.scenes.Hud;
import ee.taltech.gandalf.scenes.SettingsWindow;
import ee.taltech.gandalf.world.TileData;
import ee.taltech.gandalf.scenes.MenuWindow;
import ee.taltech.gandalf.world.WorldCollision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ee.taltech.gandalf.components.ItemTypes.*;

public class GameScreen extends ScreenAdapter {
    private final World world;
    private final AssetManager assetManager;
    GandalfRoyale game;
    NetworkClient nc;

    private ExtendViewport viewport;
    private OrthographicCamera camera;

    private final Hud hud;
    private final MenuWindow menuWindow;
    private final SettingsWindow settingsWindow;
    private boolean menuWindowShown;
    private boolean settingsWindowShown;
    public final StartedGame startedGame;

    private final Map<Integer, PlayerCharacter> gamePlayers;
    private final PlayerCharacter clientCharacter;

    private final OrthogonalTiledMapRenderer renderer;
    private final TmxMapLoader mapLoader;
    private final TiledMap map;

    private static Texture otherPlayZoneTexture;
    private static Texture otherExpectedZoneTexture;
    private static Texture fireballTexture;
    private static Texture plasmaTexture;
    private static Texture meteorTexture;
    private static Texture kunaiTexture;
    private static Texture fireballBook;
    private static Texture plasmaBook;
    private static Texture meteorBook;
    private static Texture kunaiBook;
    private static Texture iceShardBook;
    private static Texture poisonBallBook;
    private static Texture coinTexture;
    private static Texture pumpkinAttackingTexture;
    private static Texture pumpkinWalkingTexture;
    private static Texture healingPotionTexture;

    private static Texture firstPlayZoneTexture;
    private static Texture firstExpectedZoneTexture;
    private float elapsedTime;
    private float actionAnimationTime;
    private float mobAttackAnimationTime;
    private Integer attackAnimationCount = 0;
    private final ShapeRenderer shapeRenderer;

    private TextureRegion currentCharacterFrame;
    private TextureRegion currentMobFrame;
    private TextureRegion currentCoinFrame;
    private PlayZone playZone;
    private Integer currentTime;
    private List<TiledMapTileLayer> layersToBeOrdered;


    private final InputMultiplexer inputMultiplexer;

    /**
     * Construct GameScreen.
     *
     * @param game GandalfRoyale game instance
     */
    public GameScreen(GandalfRoyale game, Lobby lobby) {
        world = new World(new Vector2(0, 0), true); // Create a new Box2D world
        CollisionHandler collisionHandler = new CollisionHandler(); // This has to be on a separate line!!!
        world.setContactListener(new CollisionHandler());

        this.game = game;
        this.nc = game.nc;
        this.assetManager = this.game.assetManager;

        setTextures();

        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        mapLoader = new TmxMapLoader();

        viewport = new ExtendViewport(Constants.MIN_CHUNKS_SEEN, Constants.MIN_CHUNKS_SEEN,
                Constants.MAX_TILES_SEEN_WIDTH, Constants.MAX_TILES_SEEN_HEIGHT, camera);
        viewport.apply();

        map = game.assetManager.get("Gandalf_Royale.tmx", TiledMap.class);

        renderer = new OrthogonalTiledMapRenderer(map, 1 / Constants.PPM);
        new WorldCollision(world, map, nc);

        startedGame = new StartedGame(game, lobby, world);
        gamePlayers = startedGame.getGamePlayers();
        clientCharacter = startedGame.getClientCharacter();

        hud = new Hud(clientCharacter, startedGame);
        menuWindow = new MenuWindow(game);
        settingsWindow = game.screenController.getSettingsWindow();
        menuWindowShown = false;
        settingsWindowShown = false;

        shapeRenderer = new ShapeRenderer();

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new PlayerInput(game, clientCharacter, this));

        layersToBeOrdered = initializeLayersToBeOrdered();

        sendReadyState(lobby);
    }

    private void sendReadyState(Lobby lobby) {
        nc.sendTCP(new GameLoaded(lobby.getId(), true));
    }


    /**
     * Add specific layers to the list.
     *
     * @return List of layers, that are needed to be ordered.
     */
    private List<TiledMapTileLayer> initializeLayersToBeOrdered() {
        List<TiledMapTileLayer> layers = new ArrayList<>();
        for (String layerName : Constants.LAYERS_TO_BE_ORDERED) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
            layers.add(layer);
        }
        return layers;
    }


    /**
     * CurrentTime setter.
     *
     * @param currentTime - set current time.
     */
    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }


    /**
     * Get any texture.
     * Give the appropriate type upon calling this method to get the right texture.
     *
     * @return fireballBook
     */
    public static Texture getTexture(TextureType textureType) {
        return switch (textureType) {
            case FIREBALL_BOOK -> fireballBook;
            case FIREBALL -> fireballTexture;
            case PLASMA_BOOK -> plasmaBook;
            case PLASMA -> plasmaTexture;
            case METEOR_BOOK -> meteorBook;
            case METEOR -> meteorTexture;
            case KUNAI_BOOK -> kunaiBook;
            case KUNAI -> kunaiTexture;
            case ICE_SHARD_BOOK -> iceShardBook;
            case POISONBALL_BOOK -> poisonBallBook;
            case COIN -> coinTexture;
            case PUMPKINATTACK -> pumpkinAttackingTexture;
            case PUMPKINWALK -> pumpkinWalkingTexture;
            case HEALING_POTION -> healingPotionTexture;
            default -> new Texture("wizard.png");
        };
    }

    /**
     * Check if settings windows is shown.
     *
     * @return boolean value
     */
    public boolean isSettingsWindowShown() {
        return settingsWindowShown;
    }

    /**
     * Set all textures.
     */
    private void setTextures() {
        // *------ BOOK TEXTURES ------*
        fireballBook = assetManager.get("Spells/Fireball/fireball_book.png", Texture.class);
        plasmaBook = assetManager.get("Spells/Plasma/plasma_book.png", Texture.class);
        meteorBook = assetManager.get("Spells/Meteor/meteor_book.png", Texture.class);
        kunaiBook = assetManager.get("Spells/Kunai/kunai_book.png", Texture.class);
        iceShardBook = assetManager.get("Spells/IceShard/iceshard_book.png", Texture.class);
        poisonBallBook = assetManager.get("Spells/Poisonball/poisonball_book.png", Texture.class);

        // *------ SPELL TEXTURES ------*
        fireballTexture = assetManager.get("Spells/Fireball/packFireball.png", Texture.class);
        plasmaTexture = assetManager.get("Spells/Plasma/packPlasma.png", Texture.class);
        meteorTexture = assetManager.get("Spells/Meteor/packMeteor.png", Texture.class);
        kunaiTexture = assetManager.get("Spells/Kunai/packKunai.png", Texture.class);

        // *------ HEALING POTION TEXTURES ------*
        healingPotionTexture = assetManager.get("Potion/potion.png", Texture.class);

        // *------ COIN TEXTURE ------*
        coinTexture = assetManager.get("Coin/Coin_rotating.png", Texture.class);

        // *------ PLAY ZONE TEXTURES ------*
        firstExpectedZoneTexture = assetManager.get("Zone/expected_zone.png", Texture.class);
        otherExpectedZoneTexture = assetManager.get("Zone/huge_expected_zone.png", Texture.class);
        otherPlayZoneTexture = assetManager.get("Zone/hugeNewZone.png", Texture.class);
        firstPlayZoneTexture = assetManager.get("Zone/safezone.png", Texture.class);

        // *------ PUMPKIN TEXTURE ------*
        pumpkinAttackingTexture = assetManager.get("Pumpkin/Pumpkin_Attacking.png", Texture.class);
        pumpkinWalkingTexture = assetManager.get("Pumpkin/Pumpkin_Walks.png", Texture.class);
    }

    /**
     * Set the specific wizards texture.
     * Useful because different wizards have different textures.
     */
    public static Texture getWizardTexture(int userID) {
        // Choose one of the spritesheets for the character
        String filename = "wizardTexture" + userID % 10 + ".png";
        FileHandle fileHandle = Gdx.files.internal("Wizards/" + filename);
        return new Texture(fileHandle);
    }

    /**
     * Draw all player character to screen.
     */
    private void drawPlayer(PlayerCharacter player) {
        // elapsedTime is never reset and used for looping animations.
        // use animationTime and reset it to play an animation once
        PlayerCharacterAnimator playerAnimator = player.getPlayerAnimator();

        // Movement and idle setters.
        if (playerAnimator.getState() == PlayerCharacterAnimator.AnimationStates.IDLE ||
                playerAnimator.getState() == PlayerCharacterAnimator.AnimationStates.MOVEMENT) {
            playerAnimator.setState();
        }

        // All state handling. Choosing the frame to display this tick.
        if (playerAnimator.getState() == PlayerCharacterAnimator.AnimationStates.DEATH) {
            currentCharacterFrame = playerAnimator.deathAnimation().getKeyFrame(4 * 0.3f, false);
        } else if (playerAnimator.getState() == PlayerCharacterAnimator.AnimationStates.ACTION) {
            // Increment animationTime
            actionAnimationTime += Gdx.graphics.getDeltaTime();
            currentCharacterFrame = playerAnimator.actionAnimation().getKeyFrame(actionAnimationTime);
            if (playerAnimator.actionAnimation().isAnimationFinished(actionAnimationTime)) {
                playerAnimator.setState(PlayerCharacterAnimator.AnimationStates.IDLE);
                actionAnimationTime = 0;
            }
        } else if (playerAnimator.getState() == PlayerCharacterAnimator.AnimationStates.MOVEMENT) {
            currentCharacterFrame = playerAnimator.movementAnimation().getKeyFrame(elapsedTime, true); // Use elapsedTime
        } else if (playerAnimator.getState() == PlayerCharacterAnimator.AnimationStates.IDLE) {
            currentCharacterFrame = playerAnimator.idleAnimation().getKeyFrame(elapsedTime, true); // Use elapsedTime
        }
        game.batch.begin();
        // Set the image to 3 times smaller picture and flip it, if player is moving left.
        if (playerAnimator.lookRight()) {
            game.batch.draw(currentCharacterFrame, player.getXPosition() - 35f / Constants.PPM,
                    player.getYPosition() - 40f / Constants.PPM,
                    80 / Constants.PPM,
                    80 / Constants.PPM);
        } else {
            game.batch.draw(currentCharacterFrame, player.getXPosition() - 45f / Constants.PPM,
                    player.getYPosition() - 40f / Constants.PPM,
                    80 / Constants.PPM,
                    80 / Constants.PPM);
        }
        game.batch.end();
        // Draw health and mana bar
        drawBars(player);
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
        shapeRenderer.rect(player.getXPosition() - 50 / Constants.PPM, player.getYPosition() + 35 / Constants.PPM,
                100 / Constants.PPM, 2 / Constants.PPM);

        // Draw health bar
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(player.getXPosition() - 50 / Constants.PPM, player.getYPosition() + 35 / Constants.PPM,
                player.getHealth() / Constants.PPM, 2 / Constants.PPM);

        // Draw missing mana bar
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(player.getXPosition() - 50 / Constants.PPM, player.getYPosition() + 30 / Constants.PPM,
                100 / Constants.PPM, 2 / Constants.PPM);

        // Draw mana bar
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(player.getXPosition() - 50 / Constants.PPM, player.getYPosition() + 30 / Constants.PPM,
                (float) (player.getMana() / Constants.PPM), 2 / Constants.PPM);

        // Stop rendering shapes
        shapeRenderer.end();
    }

    /**
     * Draw PlayZone.
     */
    private void drawPlayZone() {
        playZone = startedGame.getPlayZone();
        int stage = playZone.getStage();
        game.batch.begin();
        if (stage == 1) {
            game.batch.draw(firstExpectedZoneTexture,
                    playZone.firstPlayZoneX() - Constants.FIRST_ZONE_TEXTURE_SIZE / 2f,
                    playZone.firstPlayZoneY() - Constants.FIRST_ZONE_TEXTURE_SIZE / 2f,
                    Constants.FIRST_ZONE_TEXTURE_SIZE, Constants.FIRST_ZONE_TEXTURE_SIZE);
        }
        if (stage == 2 || stage == 3) {
            game.batch.draw(firstPlayZoneTexture,
                    playZone.firstPlayZoneX() - Constants.FIRST_ZONE_TEXTURE_SIZE / 2f,
                    playZone.firstPlayZoneY() - Constants.FIRST_ZONE_TEXTURE_SIZE / 2f,
                    Constants.FIRST_ZONE_TEXTURE_SIZE, Constants.FIRST_ZONE_TEXTURE_SIZE);
        }
        if (stage == 3) {
            game.batch.draw(otherExpectedZoneTexture,
                    playZone.secondPlayZoneX() - Constants.SECOND_ZONE_TEXTURE_SIZE / 2f,
                    playZone.secondPlayZoneY() - Constants.SECOND_ZONE_TEXTURE_SIZE / 2f,
                    Constants.SECOND_ZONE_TEXTURE_SIZE, Constants.SECOND_ZONE_TEXTURE_SIZE);
        }
        if (stage == 4 || stage == 5) {
            // size is map times 4
            game.batch.draw(otherPlayZoneTexture,
                    playZone.secondPlayZoneX() - Constants.SECOND_ZONE_TEXTURE_SIZE / 2f,
                    playZone.secondPlayZoneY() - Constants.SECOND_ZONE_TEXTURE_SIZE / 2f,
                    Constants.SECOND_ZONE_TEXTURE_SIZE, Constants.SECOND_ZONE_TEXTURE_SIZE);
        }
        if (stage == 5) {
            game.batch.draw(otherExpectedZoneTexture,
                    playZone.thirdPlayZoneX() - Constants.THIRD_ZONE_TEXTURE_SIZE / 2f,
                    playZone.thirdPlayZoneY() - Constants.THIRD_ZONE_TEXTURE_SIZE / 2f,
                    Constants.THIRD_ZONE_TEXTURE_SIZE, Constants.THIRD_ZONE_TEXTURE_SIZE);
        }
        if (stage >= 6) {
            game.batch.draw(otherPlayZoneTexture,
                    playZone.thirdPlayZoneX() - Constants.THIRD_ZONE_TEXTURE_SIZE / 2f,
                    playZone.thirdPlayZoneY() - Constants.THIRD_ZONE_TEXTURE_SIZE / 2f,
                    Constants.THIRD_ZONE_TEXTURE_SIZE, Constants.THIRD_ZONE_TEXTURE_SIZE);
        }

        game.batch.end();
    }

    /**
     * Draw spells.
     */
    private void drawSpell(Spell spell) {
        // The spells need to be changed only outside rendering, otherwise will crash.
        if (spell.rotation().isPresent()) {
            if (spell.getType() == ItemTypes.FIREBALL) {
                game.batch.begin();
                TextureRegion spellCurrentFrame = spell.getAnimation().getKeyFrame(elapsedTime, true);
                float fireballXOffset = (float) (Constants.FIREBALL_WIDTH / 4 * (Math.cos(Math.toRadians(spell.rotation().get()))));
                float fireballYOffset = (float) (Constants.FIREBALL_HEIGHT / 4 * (Math.sin(Math.toRadians(spell.rotation().get()))));
                game.batch.draw(spellCurrentFrame,
                        (float) spell.getXPosition() - fireballXOffset - Constants.FIREBALL_WIDTH / 8 - 1.05f,
                        (float) spell.getYPosition() - fireballYOffset - Constants.FIREBALL_HEIGHT / 2.5f - 0.41f,
                        Constants.FIREBALL_WIDTH / 2,
                        Constants.FIREBALL_HEIGHT / 2,
                        Constants.FIREBALL_WIDTH,
                        Constants.FIREBALL_HEIGHT,
                        0.8f,
                        0.8f,
                        spell.rotation().get());
                game.batch.end();
            } else if (spell.getType() == ItemTypes.PLASMA) {
                game.batch.begin();
                TextureRegion spellCurrentFrame = spell.getAnimation().getKeyFrame(elapsedTime, true);
                float plasmaXOffset = (float) (Constants.PLASMA_WIDTH / 4 * (Math.cos(Math.toRadians(spell.rotation().get()))));
                float plasmaYOffset = (float) (Constants.PLASMA_HEIGHT / 4 * (Math.sin(Math.toRadians(spell.rotation().get()))));
                game.batch.draw(spellCurrentFrame,
                        spell.getXPosition() - plasmaXOffset - Constants.PLASMA_HEIGHT / 8 - 0.5f,
                        spell.getYPosition() - plasmaYOffset - Constants.PLASMA_HEIGHT / 1.5f - 0.03f,
                        Constants.PLASMA_WIDTH / 2,
                        Constants.PLASMA_HEIGHT / 2,
                        Constants.PLASMA_WIDTH,
                        Constants.PLASMA_HEIGHT,
                        0.5f,
                        0.5f,
                        spell.rotation().get());
                game.batch.end();
            } else if (spell.getType() == METEOR) {
                game.batch.begin();
                TextureRegion spellCurrentFrame = spell.getAnimation().getKeyFrame(elapsedTime, true);
                float meteorXOffset = (float) (Constants.METEOR_WIDTH / 4 * (Math.cos(Math.toRadians(spell.rotation().get()))));
                float meteorYOffset = (float) (Constants.METEOR_HEIGHT / 4 * (Math.sin(Math.toRadians(spell.rotation().get()))));
                game.batch.draw(spellCurrentFrame,
                        // in place
                        (float) spell.getXPosition() - meteorXOffset - Constants.METEOR_WIDTH / 2,
                        (float) spell.getYPosition() - meteorYOffset - Constants.METEOR_HEIGHT / 1.5f,
                        Constants.METEOR_WIDTH / 2,
                        Constants.METEOR_HEIGHT / 2,
                        Constants.METEOR_WIDTH,
                        Constants.METEOR_HEIGHT,
                        1,
                        1,
                        spell.rotation().get());
                game.batch.end();
            } else if (spell.getType() == KUNAI) {
                game.batch.begin();
                TextureRegion spellCurrentFrame = spell.getAnimation().getKeyFrame(elapsedTime, true);
                float meteorXOffset = (float) (Constants.KUNAI_WIDTH / 4 * (Math.cos(Math.toRadians(spell.rotation().get()))));
                float meteorYOffset = (float) (Constants.KUNAI_HEIGHT / 4 * (Math.sin(Math.toRadians(spell.rotation().get()))));
                game.batch.draw(spellCurrentFrame,
                        // in place
                        (float) spell.getXPosition() - meteorXOffset - Constants.KUNAI_WIDTH / 2,
                        (float) spell.getYPosition() - meteorYOffset - Constants.KUNAI_HEIGHT / 1.5f,
                        Constants.KUNAI_WIDTH / 2,
                        Constants.KUNAI_HEIGHT / 2,
                        Constants.KUNAI_WIDTH,
                        Constants.KUNAI_HEIGHT,
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
    private void drawItem(Item item) {
        if (item.getType() == ItemTypes.COIN) {
            currentCoinFrame = item.getCoinRotationAnimation().getKeyFrame(elapsedTime, true);
            game.batch.begin();
            game.batch.draw(currentCoinFrame,
                    item.getXPosition() - item.getTextureWidth() / 2,
                    item.getYPosition() - item.getTextureHeight() / 2,
                    10 / Constants.PPM, 10 / Constants.PPM);
            game.batch.end();
        } else {
            game.batch.begin();
            game.batch.draw(item.getTexture(),
                    item.getXPosition() - item.getTextureWidth() / 2,
                    item.getYPosition() - item.getTextureHeight() / 2,
                    item.getTextureWidth(), item.getTextureHeight());
            game.batch.end();
        }
    }

    /**
     * Draw mobs with their health bar.
     */
    private void drawMob(Mob mob) {
        MobAnimator mobAnimator = mob.getMobAnimator();
        int shortestDistance = getShortestDistance(mob);
        if (shortestDistance < 2) { // Check if mob is near the player
            // Increment attack animation time
            mobAttackAnimationTime += Gdx.graphics.getDeltaTime();

            // Check if attack animation is finished
            if (mobAnimator.attackAnimation().isAnimationFinished(mobAttackAnimationTime)) {
                // Reset animation time
                mobAttackAnimationTime = 0;
                attackAnimationCount++;
            } else if (attackAnimationCount < 2) {
                // Set current mob frame to attack animation frame
                currentMobFrame = mob.getMobAnimator().attackAnimation().getKeyFrame(mobAttackAnimationTime, true);
            } else {
                // Set current mob frame to movement animation frame
                currentMobFrame = mob.getMobAnimator().movementAnimation().getKeyFrame(elapsedTime, true);
            }
        } else {
            // Reset attack animation count when player moves away
            attackAnimationCount = 0;
            // Set current mob frame to movement animation frame
            currentMobFrame = mob.getMobAnimator().movementAnimation().getKeyFrame(elapsedTime, true);
        }

        // *-------------- MOB ASSET --------------*
        game.batch.begin();
        game.batch.draw(currentMobFrame,
                mob.getXPosition() - Constants.PUMPKIN_WIDTH / 2f,
                mob.getYPosition() - Constants.PUMPKIN_HEIGHT / 2f,
                Constants.PUMPKIN_WIDTH, Constants.PUMPKIN_HEIGHT);
        game.batch.end();

        // *-------------- MOB HEATH BAR --------------*
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // Start drawing

        // Missing health
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(
                mob.getXPosition() - 25f / 2f / Constants.PPM,
                mob.getYPosition() + Constants.PUMPKIN_HEIGHT / 2f,
                25 / Constants.PPM, 2 / Constants.PPM);

        // Present health
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(
                mob.getXPosition() - 25f / 2f / Constants.PPM,
                mob.getYPosition() + Constants.PUMPKIN_HEIGHT / 2f,
                mob.getHealth() / 2f / Constants.PPM, 2 / Constants.PPM);

        shapeRenderer.end(); // Stop drawing
    }

    /**
     * Get the shortest distance to a player.
     *
     * @param mob mob whose distance to players is checked
     * @return integer values of shortest distance to player from a mob
     */
    private int getShortestDistance(Mob mob) {
        int shortestDistance = Integer.MAX_VALUE; // Initialize with a large value
        int distanceFromPlayer = 0;
        for (PlayerCharacter playerCharacter : gamePlayers.values()) {
            distanceFromPlayer = (int) Math.sqrt(Math.pow(playerCharacter.getXPosition() - mob.getXPosition(), 2)
                    + Math.pow(playerCharacter.getYPosition() - mob.getYPosition(), 2));
            // Update the shortest distance if the current distance is shorter
            shortestDistance = Math.min(shortestDistance, distanceFromPlayer);
        }
        return shortestDistance;
    }

    /**
     * Display message on the screen.
     *
     * @param message message as a string
     */
    public void displayMessageOnScreen(String message) {
        hud.addMessage(message);
    }

    /**
     * Toggle menu window and input processor.
     */
    public void toggleMenuWindow() {
        menuWindowShown = !menuWindowShown;

        if (menuWindowShown) {
            inputMultiplexer.addProcessor(menuWindow.getStage()); // Listen to input from menu window
        } else {
            inputMultiplexer.removeProcessor(menuWindow.getStage()); // Stop listening to input from menu window
        }
    }

    /**
     * Toggle settings window and input processor.
     */
    public void toggleSettingsWindow() {
        settingsWindowShown = !settingsWindowShown;

        if (settingsWindowShown) {
            toggleMenuWindow(); // Toggle menu window off
            inputMultiplexer.addProcessor(settingsWindow.getInput()); // Listen to input from settings window
        } else {
            inputMultiplexer.removeProcessor(settingsWindow.getInput()); // Stop listening to input from settings window
        }
    }

    /**
     * Show screen on initialization.
     */
    @Override
    public void show() {
        // Set input for client
        Gdx.input.setInputProcessor(inputMultiplexer);
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
        camera.position.set(clientCharacter.getXPosition(), clientCharacter.getYPosition(), 0);
        // Update camera matrices
        camera.update();
        viewport.apply();
        // Set camera projection matrix
        game.batch.setProjectionMatrix(camera.combined);

        // Render game objects
        game.batch.begin();
        camera.zoom = 3f; // To render 3X bigger area than seen.
        renderer.setView(camera);
        renderer.render(Constants.BACKGROUND_LAYERS);
        game.batch.end();
        renderByLayering(); // Render entities and tiles
        camera.zoom = 1.5f; // Reset the camera back to its original state.

        if (startedGame.getPlayZone() != null) {
            drawPlayZone();
        } else {
            playZone = startedGame.getPlayZone();
        }

        hud.draw(currentTime);

        if (menuWindowShown) {
            menuWindow.draw();
        }
        if (settingsWindowShown) {
            settingsWindow.draw();
        }
    }

    /**
     * Sort player rendering and layer rendering to make the right visual appearance.
     */
    private void renderByLayering() {
        // Get all game objects, that should be rendered.
        List<Entity> entities = getGameObjects();
        elapsedTime += Gdx.graphics.getDeltaTime();
        for (Entity entity : entities) {
            switch (entity) {
                case PlayerCharacter player -> drawPlayer(player);
                case TileData tileData -> drawTile(tileData);
                case Mob mob -> drawMob(mob);
                case Spell spell -> drawSpell(spell);
                case Item item -> drawItem(item);
                default -> throw new IllegalStateException("Rendering for item not implemented yet: " + entity);
            }
        }
    }

    /**
     * Render tile once.
     *
     * @param tile One tile asset
     */
    private void drawTile(TileData tile) {
        game.batch.begin();
        game.batch.draw(tile.sprite, tile.x, tile.y,
                tile.sprite.getWidth() / Constants.PPM,
                tile.sprite.getHeight() / Constants.PPM);
        game.batch.end();
    }

    /**
     * Sorts the players and tiles based on y position.
     *
     * @return Players and Tiles in render order.
     */
    private List<Entity> getGameObjects() {
        List<Entity> entities = new ArrayList<>();
        entities.addAll(getTilesNearby());
        entities.addAll(startedGame.getGamePlayers().values());
        entities.addAll(startedGame.getSpells().values());
        entities.addAll(startedGame.getMobs().values());
        entities.addAll(startedGame.getItems().values());
        entities.sort(new GameObjectComparator());
        return entities.reversed(); // Higher Y should be rendered first.
    }

    /**
     * Get the tiles that need to be rendered on the screen.
     *
     * @return tiles near client's viewport.
     */
    private List<TileData> getTilesNearby() {
        List<TileData> tilesNearby = new ArrayList<>();
        // Loop through 3 layers "props", "vegetation", "rocks", that are manually set.
        for (TiledMapTileLayer layer : layersToBeOrdered) {
            // Loop x-cords around client's viewport.
            for (int x = (int) (clientCharacter.getXPosition() - Constants.MAX_TILES_SEEN_WIDTH);
                 x < clientCharacter.getXPosition() + Constants.MAX_TILES_SEEN_WIDTH; x++) {
                // Loop y-cords around client's viewport. Y - cords are read from top to bottom.
                for (int y = (int) (clientCharacter.getYPosition() + Constants.MAX_TILES_SEEN_HEIGHT);
                     y > clientCharacter.getYPosition() - Constants.MAX_TILES_SEEN_HEIGHT; y--) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        TiledMapTile tile = cell.getTile();
                        Sprite sprite = new Sprite(tile.getTextureRegion().getTexture());
                        if (cell.getFlipHorizontally()) {
                            sprite.flip(true, false);
                        }
                        // Create TileData object accordingly.
                        tilesNearby.add(new TileData(sprite, x, y, layer));
                    }
                }
            }
        }
        return tilesNearby;
    }

    /**
     * dd
     * Correct camera position when resizing a window.
     *
     * @param width  window width
     * @param height window height
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hud.resize(width, height);
        menuWindow.resize(width, height);
        settingsWindow.resize(width, height);
    }

    /**
     * Hide screen.
     */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        world.dispose();
        map.dispose();
        hud.dispose();
        menuWindow.dispose();
        game.nc.removeAllListeners();
    }
}
