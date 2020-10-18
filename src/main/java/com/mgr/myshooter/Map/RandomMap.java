package com.mgr.myshooter.Map;

import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.Material;
import com.mgr.engine.Texture;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.management.InvalidAttributeValueException;
import java.util.*;

public class RandomMap {

    private final int MAX_CELLS = 10;

    // For now the dimensions need to be multiples of Room (numStripsPerWall)
    private final int CELL_WIDTH_METERS   = 200;
    private final int CELL_HEIGHT_METERS  = 60;
    private final int CELL_FLOOR_METERS   = 300;
    private final int SPACE_BETWEEN_ROOMS_METERS = 40;

    private  int maxPointLights = 5;
    private  int maxSpotLights  = 5;

    private int[][] mapMatrix = new int[MAX_CELLS][MAX_CELLS];
    private int totalGeneratedRooms = 0;
    private List<Room> rooms;
    private PropertiesLoader props;

    // In the future there will be more themes. Themes will determine different things on the map like textures
    private final MapTheme mapTheme = MapTheme.SPACESHIP;

    private Map<WallOrientation, Material> mapMaterials = null;


    /*private final List<String> roomTypes = Arrays.asList("NSWE","NSW","NSE","NS","WE","W","E","N","S","SE","SW","NW","NE");
    private final Map<String, String> directionOpposites = new HashMap<String, String>() {{
        put("N", "S");
        put("S", "N");
        put("E", "W");
        put("W", "E");
    }};*/

    public RandomMap(final int maxPointLights, final int maxSpotLights, final PropertiesLoader props){
        this.maxPointLights = maxPointLights;
        this.maxSpotLights  = maxSpotLights;
        this.props = props;
    }

    private Integer getRandomIntNumberInRange(final Integer min, final Integer max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private void generateMap(Integer posX, Integer posY ,Integer totalNumRooms){

            if (totalGeneratedRooms < totalNumRooms){

                mapMatrix[posX][posY] = totalGeneratedRooms+1;

                // try to create room on West side
                if (posX > 0 && mapMatrix[posX-1][posY] == 0) {
                    if (getRandomIntNumberInRange(0,10) % 2 == 0) { // If the random number is divisible by 2 then create the extra room
                        totalGeneratedRooms +=1;
                        generateMap(posX-1, posY, totalNumRooms);
                    }
                }

                // try to create room on East side
                if (posX < MAX_CELLS-1 && mapMatrix[posX+1][posY] == 0) {
                    if (getRandomIntNumberInRange(0,10) % 2 == 0) { // If the random number is divisible by 2 then create the extra room
                        totalGeneratedRooms +=1;
                        generateMap(posX+1, posY, totalNumRooms);
                    }
                }

                // try to create room on North side
                if (posY > 0 && mapMatrix[posX][posY-1] == 0) {
                    if (getRandomIntNumberInRange(0,10) % 2 == 0) { // If the random number is divisible by 2 then create the extra room
                        totalGeneratedRooms +=1;
                        generateMap(posX, posY-1, totalNumRooms);
                    }
                }

                // try to create room on South side
                if (posY < MAX_CELLS-1 && mapMatrix[posX][posY+1] == 0) {
                    if (getRandomIntNumberInRange(0,10) % 2 == 0) { // If the random number is divisible by 2 then create the extra room
                        totalGeneratedRooms +=1;
                        generateMap(posX, posY+1, totalNumRooms);
                    }
                }
            }
    }

    private List<Room> generateRoomsFromMap() throws InvalidAttributeValueException {

        final List<Room> result = new ArrayList<>();

        // Read the map plan and generate the actual com.mgr.myshooter.Map.Room object with it's connections
        for (int x=0;x<MAX_CELLS;x++)
            for (int y=0;y<MAX_CELLS;y++){

                int roomIndex = mapMatrix[x][y];
                if (roomIndex>0){
                    Room room = new Room(roomIndex, 1.0f, this.maxPointLights, this.maxSpotLights,
                                         CELL_WIDTH_METERS, CELL_HEIGHT_METERS, CELL_FLOOR_METERS,
                                         mapMaterials);
                    //Link with room on the West
                    if (x > 0 && mapMatrix[x-1][y] != 0)
                        room.setPathWithRoomIndex(WallOrientation.LEFT,mapMatrix[x-1][y]);
                    //Link with room on the East
                    if (x < MAX_CELLS-1 && mapMatrix[x+1][y] != 0)
                        room.setPathWithRoomIndex(WallOrientation.RIGHT,mapMatrix[x+1][y]);
                    //Link with room on the North
                    if (y > 0 && mapMatrix[x][y-1] != 0)
                        room.setPathWithRoomIndex(WallOrientation.FRONT,mapMatrix[x][y-1]);
                    //Link with room on the South
                    if (y < MAX_CELLS-1 && mapMatrix[x][y+1] != 0)
                        room.setPathWithRoomIndex(WallOrientation.BACK,mapMatrix[x][y+1]);

                    room.setWorldPosition(calcRoomWorldPosition(x, y));
                    result.add(room);
                }

            }

        // Once all the rooms have been crated and their connections has been calculated
        // we can create all the geometry
        for (Room room : result)
            room.init();

        return result;
    }

    private Vector3f calcRoomWorldPosition(final int x, final int y) {

        //return new Vector3f(0.0f,0.0f,0.0f);
        Vector3f result = new Vector3f(x*(CELL_WIDTH_METERS + SPACE_BETWEEN_ROOMS_METERS), 0.0f, y*(CELL_FLOOR_METERS + SPACE_BETWEEN_ROOMS_METERS));
        return result;
    }

    private void cleanAllMaterials() {
        for (final WallOrientation dir : WallOrientation.values()) {
            final Material material = mapMaterials.get(dir);
            if (material != null) material.cleanUp();
        }
    }


    private Map<WallOrientation, Texture> generateTexturesForRoom() throws Exception {

        // In the future when implementing multiple themes
        //if (mapTheme == MapTheme.SPACESHIP) {
        //}
        final HashMap<WallOrientation, Texture> result = new HashMap<>();

        final Texture wallTexture = new Texture(this.props.getBaseTexturesFolder() + "\\panel1\\panel1_Base_Color.jpg");
        final Texture floorTexture = new Texture(this.props.getBaseTexturesFolder() + "\\panel4\\panel4_Base_Color.jpg");
        final Texture ceilingTexture = new Texture(this.props.getBaseTexturesFolder() + "\\panel6\\panel6_Base_Color.jpg");

        for (final WallOrientation dir : WallOrientation.values()) {
            if (dir != WallOrientation.UP && dir != WallOrientation.DOWN) {
                result.put(dir, wallTexture);
            } else if (dir == WallOrientation.UP) {
                result.put(dir, ceilingTexture);
            } else {
                result.put(dir, floorTexture);
            }
        }

        return result;
    }

    private Map<WallOrientation, Material> generateMaterialsForRooms() throws Exception {

        final HashMap<WallOrientation, Material> result = new HashMap<>();

        final Map<WallOrientation, Texture> textures = generateTexturesForRoom();

        for (final WallOrientation dir : WallOrientation.values()) {
            result.put(dir, new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    textures.get(dir), 0.6f));
        }

        return result;
    }

    public void generateRandomMap(Integer totalNumberRooms) {

        try {

            generateMap(0, 0, totalNumberRooms);

            // Create Materials for rooms
            if (mapMaterials != null)
                cleanAllMaterials();
            mapMaterials = generateMaterialsForRooms();

            rooms = generateRoomsFromMap();

        } catch (Exception ex) {
            System.out.println("ERROR generating random map " + ex.getMessage());
        }

    }

    public void printConsoleMapPlan(){
         for (int x=0;x<MAX_CELLS;x++) {
             String line = "";
             for (int y = 0; y < MAX_CELLS; y++) {
                line += Integer.toString(mapMatrix[x][y]) + "     ";
             }
             System.out.println(line);
         }
    }

    public Pair<Integer,int[][]> getMapPlanAsIntMatrix(){
        return Pair.of(MAX_CELLS,mapMatrix);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void cleanUp(){
        for (Room room : rooms)
            room.cleanUp();
        cleanAllMaterials();
    }

    public Vector2i getPLayerCellPosition(final Vector3f worldPlayerPosition){
        int x_cell = Math.round(worldPlayerPosition.x / CELL_WIDTH_METERS);
        int y_cell = Math.round(worldPlayerPosition.z / CELL_FLOOR_METERS);
        return new Vector2i(x_cell,y_cell);
    }

}
