package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class SweeperShadow {

    static final int WIDTH = 4;
    static final int HEIGHT = 4;
    Body body;
    int xPosition = 0;
    int yPosition = 4;

    public SweeperShadow(SweepingState state) {
        createBody(state.world);
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

        this.body = hitBoxBody;
    }

    public void setPosition(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        body.setTransform(xPosition, yPosition, body.getAngle());
    }
}
