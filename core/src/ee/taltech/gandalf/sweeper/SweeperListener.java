package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.physics.box2d.*;

public class SweeperListener implements ContactListener {

    World world;

    public SweeperListener(World world) {
        this.world = world;
    }
    @Override
    public void beginContact(Contact contact) {
        System.out.println("1");
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
