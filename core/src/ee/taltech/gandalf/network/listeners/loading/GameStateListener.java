package ee.taltech.gandalf.network.listeners.loading;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.taltech.gandalf.components.Lobby;
import ee.taltech.gandalf.network.messages.game.GameLoaded;
import ee.taltech.gandalf.screens.GameScreen;
import ee.taltech.gandalf.screens.LoadingScreen;
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
        LoadingScreen loadingScreen = screenController.getLoadingScreen();
        Lobby lobby = loadingScreen.getLobby();
        switch (incomingData) {
            case GameLoaded message: // UpdateHealth message
                if (message.isLoaded()) {
                    screenController.setGameScreen(lobby);
                }
                System.out.println("switching screens");
                break;
            default:
                break;
        }
    }
}
