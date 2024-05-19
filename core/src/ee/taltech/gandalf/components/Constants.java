package ee.taltech.gandalf.components;

import java.util.List;

public class Constants {
    public static float PPM = 32;
    public static float MOVEMENT_THRESHOLD = 0.5f;
    public static float FIREBALL_WIDTH = 72 / PPM;
    public static float FIREBALL_HEIGHT = 32 / PPM;
    public static float PLASMA_WIDTH = 64 / PPM;
    public static float PLASMA_HEIGHT = 48 / PPM;
    public static float METEOR_WIDTH = 96 / PPM;
    public static float METEOR_HEIGHT = 64 / PPM;
    public static float KUNAI_WIDTH = 32 / PPM;
    public static float KUNAI_HEIGHT = 16 / PPM;
    public static final Integer TILE_WIDTH_OR_HEIGHT = 32;
    public static final Integer MAP_WIDTH_OR_HEIGHT = 300;
    public static final float MOVEMENT_SPEED = 4;
    public static final float INTERP_FACTOR = 0.3f; // For overwrite smoothness
    public static final float MAX_TILES_SEEN_WIDTH = 26;
    public static final float MAX_TILES_SEEN_HEIGHT = 13;
    public static final float MIN_CHUNKS_SEEN = 10f;
    public static final float STATUS_BAR_WIDTH_PX = 60;
    public static final int FIRST_ZONE_TEXTURE_SIZE = 375;
    public static final int SECOND_ZONE_TEXTURE_SIZE = 1200;
    public static final int THIRD_ZONE_TEXTURE_SIZE = 460;
    public static final int FIRST_ZONE_RADIUS = 140;
    public static final int SECOND_ZONE_RADIUS = 76;
    public static final int THIRD_ZONE_RADIUS = 29;
    public static final int[] BACKGROUND_LAYERS = {0, 1, 2, 3, 4, 5, 6, 7, 10}; // Magic order of layers :).
    public static final List<String> LAYERS_TO_BE_ORDERED = List.of("props on props", "props", "Merchant", "rocks", "vegetation");
    public static final int PUMPKIN_WIDTH = 1;
    public static final int PUMPKIN_HEIGHT = 1;

}
