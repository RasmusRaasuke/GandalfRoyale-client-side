package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.physics.box2d.*;

public class SweeperListener implements ContactListener {

    SweepingState state;

    public SweeperListener(SweepingState state) {
        this.state = state;
    }
    @Override
    public void beginContact(Contact contact) {
        state.slotValue = 1;
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
