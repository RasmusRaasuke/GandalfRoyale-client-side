package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import ee.taltech.gandalf.GandalfRoyale;
import ee.taltech.gandalf.entities.collision.CollisionHandler;
import ee.taltech.gandalf.world.WorldCollision;

public class SweepingState extends ScreenAdapter {

    final GandalfRoyale game;
    final World world;
    final CollisionHandler collisionHandler;
    final OrthogonalTiledMapRenderer renderer;
    final ExtendViewport viewport;
    final OrthographicCamera camera;
    final TmxMapLoader mapLoader;
    final TiledMap map;
    final ShapeRenderer shapeRenderer;

    public SweepingState(GandalfRoyale game) {
        world = new World(new Vector2(0, 0), true); // Create a new Box2D world
        collisionHandler = new CollisionHandler();
        world.setContactListener(new CollisionHandler());

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        viewport = new ExtendViewport(500, 500, camera);
        viewport.apply();

        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("Gandalf_Royale.tmx");
        this.renderer = new OrthogonalTiledMapRenderer(map);

        new WorldCollision(world, map);

        shapeRenderer = new ShapeRenderer();

//        SweeperClock tickRateLoop = new SweeperClock(this); // Create a running TPS loop.
//        Thread tickRateThread = new Thread(tickRateLoop); // Run TPS parallel to other processes.
//        tickRateThread.start();
    }

    @Override
    public void render(float delta) {
        System.out.println("Works?");
        ScreenUtils.clear(0, 0, 0, 0);
        world.step(delta, 6, 2);

        // Update camera position to follow player
        camera.position.x = 0;
        camera.position.y = 0;

        // Update camera matrices
        camera.update();

        // Set camera projection matrix
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        camera.zoom = 2f; // To render 2X bigger area than seen.
        renderer.setView(camera);
        renderer.render();
        camera.zoom = 1f; // Reset the camera back to its original state.
        game.batch.end();

        // Render shapes
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Draw missing health bar
        shapeRenderer.setColor(Color.FIREBRICK);
        shapeRenderer.rect(0, 0, (float) 100 * 2, 5);
        // Stop rendering shapes
        shapeRenderer.end();
    }


}
