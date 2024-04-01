package ee.taltech.gandalf.sweeper;

import com.badlogic.gdx.physics.box2d.*;

public class SweeperListener implements ContactListener {

    SweepingState state;

    /**
     * Construct SweeperListener.
     *
     * @param state the state where rendering takes place
     */
    public SweeperListener(SweepingState state) {
        this.state = state;
    }

    /**
     * Do nothing.
     * @param contact ignore
     */
    @Override
    public void beginContact(Contact contact) {
        // Empty
    }

    /**
     * Do nothing.
     * @param contact ignore
     */
    @Override
    public void endContact(Contact contact) {
        // Empty
    }

    /**
     * Make the slot value 1.
     * @param contact ignore
     * @param manifold ignore
     */
    @Override
    public void preSolve(Contact contact, Manifold manifold) {
        state.slotValue = 1;
    }

    /**
     * Do nothing.
     * @param contact ignore
     * @param contactImpulse ignore
     */
    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {
        // Empty
    }
}
