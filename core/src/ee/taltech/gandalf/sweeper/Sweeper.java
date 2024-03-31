package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import ee.taltech.gandalf.entities.PlayerCharacter;

public class Sweeper {

    static final int WIDTH = 4;
    static final int HEIGHT = 4;
    Body body;
    int xPosition = 0;
    int yPosition = 4;

    public Sweeper(World world) {
        createBody(world);
    }

    /**
     * Create player's hit box.
     *
     * @param world world, where hit boxes are in
     */
    public void createBody(World world) {
        // Create a dynamic or static body for the player
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(4, 4);
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

        hitBoxBody.setUserData(this);
        this.body = hitBoxBody;
    }

    public void setPosition(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        body.setTransform(xPosition, yPosition, body.getAngle());
    }
}