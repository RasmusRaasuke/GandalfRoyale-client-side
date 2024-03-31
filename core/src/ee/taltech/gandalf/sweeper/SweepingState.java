package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import ee.taltech.gandalf.GandalfRoyale;
import ee.taltech.gandalf.world.WorldCollision;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;

public class SweepingState extends ScreenAdapter {

    final GandalfRoyale game;
    final World world;
    final ContactListener sweeperListener;
    final OrthogonalTiledMapRenderer renderer;
    final ExtendViewport viewport;
    final OrthographicCamera camera;
    final TmxMapLoader mapLoader;
    final TiledMap map;
    Box2DDebugRenderer debugRenderer; // For debugging
    Sweeper sweeper;
    int xPos;
    int yPos;
    int slotValue = 0;
    StringBuilder line = new StringBuilder("{");

    public SweepingState(GandalfRoyale game) {
        world = new World(new Vector2(0, 0), true); // Create a new Box2D world
        world.setContinuousPhysics(false);
        world.setAutoClearForces(false);
        sweeperListener = new SweeperListener(this);
        world.setContactListener(sweeperListener);

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        viewport = new ExtendViewport(500, 500, camera);
        viewport.apply();

        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("Gandalf_Royale.tmx");
        this.renderer = new OrthogonalTiledMapRenderer(map);

        new WorldCollision(world, map);

        sweeper = new Sweeper(this);

        debugRenderer = new Box2DDebugRenderer();
    }

    private void constructLine() {
        if (xPos == 4 && yPos != 4) {
            line.replace(line.length - 2, line.length, "},");
            writeToFile(line.toString());
            line.clear();
            line.append("{");
            line.append(slotValue);
        } else {
            line.append(slotValue);
            line.append(", ");
        }
    }

    private void writeToFile(String input) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("GridOutPut.txt", true))) {
            writer.write(input);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        slotValue = 0;
        world.step(delta, 6, 2);

        ScreenUtils.clear(0, 0, 0, 0);
        xPos = sweeper.xPosition % 9604 == 0 ? 4 : sweeper.xPosition + 8;
        yPos = xPos == 4 ? sweeper.yPosition + 8 : sweeper.yPosition;
        if (sweeper.xPosition == 0) yPos = 4;

        sweeper.setPosition(xPos, yPos);

        // Update camera position to follow player
        camera.position.x = sweeper.xPosition;
        camera.position.y = sweeper.yPosition;

        // Update camera matrices
        camera.update();

        // Set camera projection matrix
        game.batch.setProjectionMatrix(camera.combined);

        renderer.setView(camera);
        renderer.render();

        constructLine();

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
    }
}
