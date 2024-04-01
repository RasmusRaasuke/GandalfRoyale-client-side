package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Sweeper {

    static final int WIDTH = 4;
    static final int HEIGHT = 4;
    Body body;
    int xPosition = 4;
    int yPosition = 4;

    /**
     * Construct Sweeper.
     *
     * @param state the state where rendering takes place
     */
    public Sweeper(SweepingState state) {
        createBody(state.world);
    }

    /**
     * Create sweeper's hit box.
     *
     * @param world world, where hit boxes are in
     */
    public void createBody(World world) {
        // Create a dynamic or static body for the player
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(xPosition, yPosition);
        bodyDef.fixedRotation = true;
        Body hitBoxBody = world.createBody(bodyDef);

        // Create a fixture defining the hit box shape
        PolygonShape hitBoxShape = new PolygonShape();
        hitBoxShape.setAsBox(WIDTH, HEIGHT);

        // Attach the fixture to the body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = hitBoxShape;
        hitBoxBody.createFixture(fixtureDef);

        hitBoxBody.setFixedRotation(true);

        // Clean up
        hitBoxShape.dispose();

        hitBoxBody.getFixtureList().get(0).setUserData(this);
        this.body = hitBoxBody;
    }

    /**
     * Set sweeper's position.
     *
     * @param xPosition new x coordinate
     * @param yPosition new y coordinate
     */
    public void setPosition(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        body.setTransform(xPosition, yPosition, body.getAngle());
    }
}
