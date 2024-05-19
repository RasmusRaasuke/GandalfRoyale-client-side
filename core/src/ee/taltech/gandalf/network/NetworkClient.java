package ee.taltech.gandalf.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import ee.taltech.gandalf.network.listeners.game.*;
import ee.taltech.gandalf.network.listeners.loading.GameStateListener;
import ee.taltech.gandalf.screens.ScreenController;
import ee.taltech.gandalf.components.ItemTypes;
import ee.taltech.gandalf.network.listeners.lobby.LobbyListener;
import ee.taltech.gandalf.network.listeners.lobby.LobbyRoomListener;
import ee.taltech.gandalf.network.messages.game.*;
import ee.taltech.gandalf.network.messages.lobby.*;
import ee.taltech.gandalf.world.MapObjectData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkClient {

    ScreenController screenController;
    public Client client;
    public int clientId;
    List<Listener> listeners;
    LobbyListener lobbyListener;
    LobbyRoomListener lobbyRoomListener;
    PlayerListener playerListener;
    SpellListener fireballPositionListener;
    HealthAndManaListener healthAndManaListener;
    ItemListener itemListener;
    MobListener mobListener;
    PlayZoneListener playZoneListener;
    GameStateListener gameStateListener;


    /**
     * Construct NetworkClient.
     *
     * @throws RuntimeException if connecting ti server fails
     */
    public NetworkClient(ScreenController screenController) throws RuntimeException {
        this.screenController = screenController;

        client = new Client(); // Create new client
        client.start(); // Start client

        connect(); // Connect to server
        registerKryos(); // Register sendable data

        clientId = client.getID(); // Client id variable
        listeners = new ArrayList<>();
    }

    /**
     * Register acceptable classes for sending between server and client.
     */
    public void registerKryos(){
        // Add sendable data structures.
        Kryo kryo = client.getKryo();
        kryo.register(java.util.ArrayList.class);
        kryo.register(MapObjectData.class);
        kryo.register(float[].class);
        kryo.register(PlayZoneCoordinates.class);
        kryo.register(Position.class);
        kryo.register(ActionTaken.class);
        kryo.register(Join.class);
        kryo.register(Leave.class);
        kryo.register(LobbyCreation.class);
        kryo.register(LobbyDismantle.class);
        kryo.register(GetLobbies.class);
        kryo.register(StartGame.class);
        kryo.register(PlayZoneUpdate.class);
        kryo.register(KeyPress.class);
        kryo.register(ItemTypes.class);
        kryo.register(MouseClicks.class);
        kryo.register(KeyPress.Action.class);
        kryo.register(Position.class);
        kryo.register(SpellPosition.class);
        kryo.register(SpellDispel.class);
        kryo.register(UpdateHealth.class);
        kryo.register(UpdateMana.class);
        kryo.register(ItemPickedUp.class);
        kryo.register(CoinPickedUp.class);
        kryo.register(HealingPotionUsed.class);
        kryo.register(ItemDropped.class);
        kryo.register(MobPosition.class);
        kryo.register(UpdateMobHealth.class);
        kryo.register(GameLeave.class);
        kryo.register(GameOver.class);
        kryo.register(GameLoaded.class);
        kryo.addDefaultSerializer(KeyPress.Action.class, DefaultSerializers.EnumSerializer.class);
        kryo.addDefaultSerializer(ItemTypes.class, DefaultSerializers.EnumSerializer.class);
    }

    /**
     * Create connection to server.
     */
    public void connect() {
        // Connect client with the server.
        try {
            client.connect(5000, "localhost", 8080, 8081);
            // 193.40.255.34
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Disconnect client.
     */
    public void disconnect() {
        client.close();
    }

    /**
     * Sending things to server through TCP.
     *
     * @param sendable object that will be sent through TCP to server
     */
    public void sendTCP(Object sendable) {
        client.sendTCP(sendable);
    }

    /**
     * Sending things to server through UDP.
     *
     * @param sendable object that will be sent through UDP to server
     */
    public void sendUDP(Object sendable) {
        client.sendUDP(sendable);
    }

    /**
     * Add LobbyListener.
     */
    public void addLobbyListener() {
        lobbyListener = new LobbyListener(screenController);
        listeners.add(lobbyListener);
        client.addListener(lobbyListener);
    }

    /**
     * Add LobbyRoomListener.
     */
    public void addLobbyRoomListener() {
        lobbyRoomListener = new LobbyRoomListener(screenController);
        listeners.add(lobbyRoomListener);
        client.addListener(lobbyRoomListener);
    }

    public void addLoadingListener() {
        gameStateListener = new GameStateListener(screenController);
        listeners.add(gameStateListener);
        client.addListener(gameStateListener);
    }

    /**
     * Add Game listeners.
     */
    public void addGameListeners() {
        // Create listeners
        playerListener = new PlayerListener(screenController);
        fireballPositionListener = new SpellListener(screenController);
        healthAndManaListener = new HealthAndManaListener(screenController);
        itemListener = new ItemListener(screenController);
        playZoneListener = new PlayZoneListener(screenController);
        mobListener = new MobListener(screenController);

        // Add to listeners list so they can be removed later
        listeners.add(playerListener);
        listeners.add(fireballPositionListener);
        listeners.add(healthAndManaListener);
        listeners.add(itemListener);
        listeners.add(playZoneListener);
        listeners.add(mobListener);

        // Add listener to client so that messages can be listened to
        client.addListener(playerListener);
        client.addListener(fireballPositionListener);
        client.addListener(healthAndManaListener);
        client.addListener(itemListener);
        client.addListener(playZoneListener);
        client.addListener(mobListener);
        // Add here more game listeners
    }

    /**
     * Remove all listeners.
     */
    public void removeAllListeners() {
        for (Listener listener : listeners) {
            client.removeListener(listener);
        }
    }
}
