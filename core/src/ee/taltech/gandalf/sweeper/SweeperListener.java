package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.physics.box2d.*;

public class SweeperListener implements ContactListener {

    public SweeperListener() {
    }
    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getBody().getType() == BodyDef.BodyType.KinematicBody
                || contact.getFixtureB().getBody().getType() == BodyDef.BodyType.KinematicBody) {
            // Do nothing, it is your shadow
        } else {
            System.out.println("1");
        }
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {
    }
}
