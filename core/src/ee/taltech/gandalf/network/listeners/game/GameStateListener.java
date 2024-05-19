package ee.taltech.gandalf.network.listeners.game;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.taltech.gandalf.network.messages.game.GameLoaded;
import ee.taltech.gandalf.screens.GameScreen;
import ee.taltech.gandalf.screens.ScreenController;

public class GameStateListener extends Listener {

    ScreenController screenController;

    /**
     * Construct HealthAndManaListener.
     *
     * @param screenController for accessing game screen
     */
    public GameStateListener(ScreenController screenController) {
        this.screenController = screenController;
    }

    /**
     * Received messages from server.
     *
     * @param connection server connection
     * @param incomingData message from server
     */
    @Override
    public void received(Connection connection, Object incomingData) {
        GameScreen gameScreen = screenController.getGameScreen();
        switch (incomingData) {
            case GameLoaded message: // UpdateHealth message
                break;
            default:
                break;
        }
    }
}
