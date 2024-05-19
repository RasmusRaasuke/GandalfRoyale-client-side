package ee.taltech.gandalf.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ee.taltech.gandalf.GandalfRoyale;
import ee.taltech.gandalf.network.listeners.lobby.LobbyRoomListener;
import ee.taltech.gandalf.network.messages.lobby.Leave;
import ee.taltech.gandalf.network.messages.lobby.StartGame;
import ee.taltech.gandalf.components.Lobby;

import java.util.HashMap;

public class LobbyRoomScreen extends ScreenAdapter {

    GandalfRoyale game;
    Lobby lobby;
    Stage stage;
    Table root;
    HashMap<Integer, Table> playerTables;
    Table headerTable;
    TextButton.TextButtonStyle textButtonStyle;
    TextButton buttonLeave;
    TextButton buttonStartGame;

    /**
     * Construct LobbyRoomScreen.
     *
     * @param game game instance
     * @param lobby given lobby
     */
    public LobbyRoomScreen(GandalfRoyale game, Lobby lobby) {
        this.game = game;
        this.lobby = lobby;

        stage = new Stage(game.viewport, game.batch); // Creating a stage (place, where things can be put on)

        root = new Table(); // Creating root table that covers the whole stage
        root.top(); // Starts from the top of the screen
        root.setFillParent(true); // Fill the stage
        stage.addActor(root); // Add root to stage (only actor on stage)

        playerTables = new HashMap<>();

        createHeader();
        setupListeners();
    }

    private void createHeader() {
        headerTable = new Table(); // Creating a headerTable

        // Styling labels
        Label.LabelStyle headingStyle = new Label.LabelStyle(game.font, Color.FIREBRICK);

        // Styling buttons
        textButtonStyle = new TextButton.TextButtonStyle();
//        textButtonStyle.up = skin.getDrawable("button.up")
//        textButtonStyle.down = skin.getDrawable("button.down")
        textButtonStyle.pressedOffsetX = 2;
        textButtonStyle.pressedOffsetY = -2;
        textButtonStyle.font = game.font;
        textButtonStyle.fontColor = Color.RED;

        // Create header
        buttonLeave = new TextButton("Leave", textButtonStyle);
        Label gameName = new Label(lobby.getName(), headingStyle);

        // Add header to headerTable
        headerTable.add(buttonLeave).pad(10).left();
        headerTable.add(gameName).pad(10).expandX();

        // Add start game button if client is lobby host
        addStartGameButton();

        // Add headerTable to root table
        root.add(headerTable).growX().top();

        showAllPlayersInLobby(); // Show players that are in the lobby before you
    }

    /**
     * Set up listeners for buttons to work.
     */
    private void setupListeners() {
        buttonLeave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.nc.sendTCP(new Leave(lobby.getId(), game.nc.clientId)); // Send sever Leave message
                dispose();
                game.screenController.setLobbyScreen();
            }
        });
    }

    /**
     * Add Start game button for host.
     */
    private void addStartGameButton() {
        if (game.nc.clientId == lobby.getHostId()) {
            buttonStartGame = new TextButton("Start game", textButtonStyle);
            headerTable.add(buttonStartGame).pad(10).right();

            buttonStartGame.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.nc.sendTCP(new StartGame(lobby.getId()));
                }
            });
        }
    }

    /**
     * Get lobby room's lobby.
     *
     * @return lobby
     */
    public Lobby getLobby() {
        return lobby;
    }

    /**
     * Show visually one player
     *
     * @param playerId player's ID who is shown
     */
    public void showPlayer(Integer playerId) {
        // Create playerTable
        Table playerTable = new Table();
        playerTables.put(playerId, playerTable); // Add playerTable to playerTables Map

        Label.LabelStyle gameStyle = new Label.LabelStyle(game.font, Color.WHITE);

        // Create player labels
        Label playerIdLabel = new Label(playerId.toString(), gameStyle);

        // Add lobby elements to lobbyTable
        playerTable.add(playerIdLabel).pad(10).expandX().left();

        // Add lobbyTable to root table
        root.row().padTop(20); // Empty row
        root.add(playerTable).growX();
    }

    /**
     * Show all player in lobby visually.
     */
    private void showAllPlayersInLobby() {
        for (Integer id : lobby.getPlayers()) {
            showPlayer(id);
        }
    }

    /**
     * Show new player who joined.
     *
     * @param playerId player who joined lobby
     */
    public void joinLobby(Integer playerId) {
        lobby.addPlayer(playerId);
        showPlayer(playerId);
    }

    /**
     * Remove player who left.
     *
     * @param playerId player who left lobby
     */
    public void leaveLobby(Integer playerId) {
        lobby.removePlayer(playerId);
        playerTables.get(playerId).remove();
    }

    /**
     * Start the game when host chose.
     */
    public void startGame() {
        game.screenController.setLoadingScreen(lobby);
    }

    /**
     * Show screen on initialisation.
     */
    @Override
    public void show() {
        // Reading input
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Render the screen aka update every frame.
     *
     * @param delta time
     */
    @Override
    public void render(float delta) {
        // Clear the screen and update the stage
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glFlush();

        // Show the stage
        stage.act(delta);
        stage.draw();
    }

    /**
     * Resize the viewport.
     *
     * @param width new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    /**
     * Hide screen.
     */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    /**
     * Dispose screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
        game.nc.removeAllListeners();
//        skin.dispose();
    }
}
