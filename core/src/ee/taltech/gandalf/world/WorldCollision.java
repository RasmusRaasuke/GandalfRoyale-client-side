package ee.taltech.gandalf.world;

import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.esotericsoftware.kryo.io.Output;
import ee.taltech.gandalf.components.Constants;
import ee.taltech.gandalf.network.NetworkClient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.List;

public class WorldCollision {

    private final NetworkClient nc;
    private boolean collisionsCreated;
    private final Map map;
    private World world;
    private final int tileHeight;
    private final int tileWidth;
    public List<MapObjectData> savedData = new ArrayList<>();

    /**
     * @param world Box2D world created in GameScreen
     * @param map   Map containing assets created in GameScreen
     */
    public WorldCollision(World world, Map map, NetworkClient nc) {
        this.world = world;
        this.map = map;

        // Change if using different tilesets (measurement: px)
        this.tileHeight = 32;
        this.tileWidth = 32;

        // Collision created flag for loading purposes.
        this.collisionsCreated = false;
        this.nc = nc;
        createCollisionsForEveryTile();

    }

    /**
     * Iterates through every layer and tile to create according collision in the Box2D format.
     */
    public void createCollisionsForEveryTile() {
        MapLayers layers = map.getLayers();
        for (MapLayer layer : layers) {
            // Check if the layer is an object layer containing collisions.
            if (layer.getName().endsWith("Collisions")) {
                lookForObjectTypeAndCreate(layer.getObjects(), 0, 0, 0);
            } else if (layer instanceof TiledMapTileLayer tileLayer) {
                loopThroughTiles(tileLayer); // Check if a tile in the layer has a collision object.
            } else {
                System.out.println("Warning: Wrong layer imported - >" + layer);
            }
        }
        collisionsCreated = true; // Flag for collision end
    }

    /**
     * Loop through tiles.
     *
     * @param tileLayer - to loop through.
     */
    public void loopThroughTiles(TiledMapTileLayer tileLayer) {
        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                // Check if the cell exists in this layer.
                if (cell != null) {
                    float textureRegionWidth = 0;
                    MapObjects mapObjects = cell.getTile().getObjects();
                    if (cell.getFlipHorizontally()) {
                        textureRegionWidth = cell.getTile().getTextureRegion().getRegionWidth();
                    }
                    lookForObjectTypeAndCreate(mapObjects, x, y, textureRegionWidth);
                }
            }

        }
    }

    /**
     * Create shapes accordingly from map objects.
     *
     * @param mapObjects         Collision objects read from TMX files.
     * @param tileX              Number x Tile in the map's x-Axis
     * @param tileY              Number y Tile in the map's y-Axis
     * @param textureRegionWidth Size of the sprite image (If it is 0, that means the cell is flipped).
     */
    public void lookForObjectTypeAndCreate(MapObjects mapObjects, int tileX, int tileY, float textureRegionWidth) {
        for (MapObject object : mapObjects) {
            switch (object) {
                case EllipseMapObject ellipseMapObject:
                    createCircle(ellipseMapObject, tileX, tileY, textureRegionWidth);
                    break;
                case RectangleMapObject rectangleMapObject:
                    createRectangle(rectangleMapObject, tileX, tileY, textureRegionWidth);
                    break;
                case PolygonMapObject polygonMapObject:
                    createPolygon(polygonMapObject, tileX, tileY, textureRegionWidth);
                    break;
                default:
                    throw new IllegalStateException("Unexpected object: " + object);
            }
        }
    }

    /**
     * Creates an Ellipse (Circle) shaped Box2D fixture.
     *
     * @param ellipseMapObject   Ellipse collision object.
     * @param tileX              Number x Tile in the map's x-Axis
     * @param tileY              Number y Tile in the map's y-Axis
     * @param textureRegionWidth Size of the sprite image (If it is 0, that means the cell is flipped).
     */
    private void createCircle(EllipseMapObject ellipseMapObject, int tileX, int tileY, float textureRegionWidth) {
        Ellipse ellipse = ellipseMapObject.getEllipse();
        savedData.add(new MapObjectData("ellipse", ellipse.x, ellipse.y, ellipse.width, ellipse.height, tileX, tileY, textureRegionWidth));
        BodyDef bodyDef = getBodyDef(ellipse.x, ellipse.y, ellipse.height,
                (textureRegionWidth != 0 ? -ellipse.width : ellipse.width),
                tileX, tileY, textureRegionWidth);
        Body body = world.createBody(bodyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(ellipse.width / 2f / Constants.PPM);

        body.createFixture(circleShape, 0.0f);
    }

    /**
     * Creates a Rectangle shaped Box2D object.
     *
     * @param rectangleMapObject Rectangle collision object
     * @param tileX              Number x Tile in the map's x-Axis
     * @param tileY              Number y Tile in the map's y-Axis
     * @param textureRegionWidth Size of the sprite image (If it is 0, that means the cell is flipped).
     */
    private void createRectangle(RectangleMapObject rectangleMapObject, int tileX, int tileY, float textureRegionWidth) {
        Rectangle rectangle = rectangleMapObject.getRectangle();
        savedData.add(new MapObjectData("rectangle", rectangle.x, rectangle.y, rectangle.width, rectangle.height, tileX, tileY, textureRegionWidth));
        BodyDef bodyDef = getBodyDef(rectangle.x, rectangle.y,
                rectangle.height, (textureRegionWidth != 0 ? -rectangle.width : rectangle.width),
                tileX, tileY, textureRegionWidth);
        Body body = world.createBody(bodyDef);
        PolygonShape rectangleShape = new PolygonShape();
        rectangleShape.setAsBox(rectangle.width / 2f / Constants.PPM, rectangle.height / 2f / Constants.PPM);
        body.createFixture(rectangleShape, 0.0f);
    }

    /**
     * Creates a Polygon shaped Box2D fixture.
     *
     * @param polygonMapObject   Polygon collision object
     * @param tileX              Number x Tile in the map's x-Axis
     * @param tileY              Number y Tile in the map's y-Axis
     * @param textureRegionWidth Size of the sprite image (If it is 0, that means the cell is flipped).
     */
    private void createPolygon(PolygonMapObject polygonMapObject, int tileX, int tileY, float textureRegionWidth) {
        // Get all position of the polygon 'corners' or vertices
        Polygon polygon = polygonMapObject.getPolygon();
        savedData.add(new MapObjectData("polygon", 0, 0, 0, 0, tileX, tileY, textureRegionWidth, polygon.getTransformedVertices()));
        float[] vertices = polygon.getTransformedVertices();
        if (vertices.length > 300) {
            System.out.println("Warning: Vertex count really high: " + vertices.length);
        }

        // TextureRegionWidth is not 0, when the cell is flipped.
        if (textureRegionWidth != 0) {
            // Loop through every second vertices, that contain x values.
            for (int i = 0; i < vertices.length; i += 2) {
                // Inverting x-values, because cell is flipped.
                vertices[i] = -vertices[i];
            }
        }

        // Scale vertices using PPM
        float[] scaledVertices = new float[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            scaledVertices[i] = vertices[i] / Constants.PPM;
        }

        // Define the body's location
        BodyDef bodyDef = getBodyDef(0, 0, 0, 0, tileX, tileY, textureRegionWidth);
        Body body = world.createBody(bodyDef);

        // Handle multiple scenarios based on vertex count
        if (vertices.length <= 8) {
            // PolygonShape can have maximum of 8 vertices.
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.set(scaledVertices);
            body.createFixture(polygonShape, 0.0f);
        } else {
            // Use ChainShape for complex shapes (more than 8 vertices)
            ChainShape chainShape = new ChainShape();
            chainShape.createLoop(scaledVertices);
            body.createFixture(chainShape, 0.0f);
            chainShape.dispose(); // Dispose of the shape after use
        }
    }

    /**
     * Creates BodyDef according to the location of the collision shape.
     *
     * @param x                  Position x of the collision inside the tile
     * @param y                  Position y of the collision inside the tile
     * @param height             Height of the collision
     * @param width              Width of the collision object
     * @param tileX              Number x Tile in the map's x-Axis
     * @param tileY              Number y Tile in the map's y-Axis
     * @param textureRegionWidth Size of the sprite image (If it is 0, that means the cell is flipped).
     * @return BodyDef, meaning body's location
     */
    private BodyDef getBodyDef(float x, float y, float height, float width,
                               int tileX, int tileY, float textureRegionWidth) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        // Calculate offset based on tile coordinates
        float bodyX = (float) (tileX * tileWidth) / Constants.PPM +
                (textureRegionWidth != 0 ? -x / Constants.PPM : x / Constants.PPM)
                + (width / 2f / Constants.PPM) + textureRegionWidth / Constants.PPM;
        float bodyY = (float) (tileY * tileHeight) / Constants.PPM + y / Constants.PPM + (height / 2f / Constants.PPM);

        bodyDef.position.set(bodyX, bodyY);
        bodyDef.fixedRotation = true;

        // The bodies don't act in any physics simulation. They wake up on collision tho.
        bodyDef.allowSleep = true;
        return bodyDef;
    }

    /**
     * @return If the collision creation process has ended.
     */
    public boolean areCollisionsCreated() {
        return collisionsCreated;
    }

    /**
     * Method for saving the world to kryonet .bin file.
     */
    public void saveWorld() {
        try {
            Output output = new Output(new FileOutputStream("mapdata.bin"));
            nc.client.getKryo().writeObject(output, savedData);
            output.close();
            System.out.println("finished");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
