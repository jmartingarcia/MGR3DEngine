package com.mgr.myshooter.Map;

import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.Material;
import com.mgr.engine.Texture;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.*;

import javax.management.InvalidAttributeValueException;
import java.lang.Math;
import java.util.*;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomMap {

    private final int MAX_CELLS = 10;

    // For now the dimensions need to be multiples of Room (numStripsPerWall)
    private final int CELL_WIDTH_METERS   = 200;
    private final int DOOR_WIDTH          = CELL_WIDTH_METERS/5;
    private final int CELL_HEIGHT_METERS  = 60;
    private final int CELL_DEPTH_METERS = 300;
    private final int SPACE_BETWEEN_ROOMS_METERS = 40;

    private  int maxPointLights = 5;
    private  int maxSpotLights  = 5;

    private int[][] mapMatrix = new int[MAX_CELLS][MAX_CELLS];
    private int totalGeneratedRooms = 0;
    private List<Room> rooms;

    // The Key is the room index to that has this connectors. Two or more rooms can share same connectors.
    private HashMap<Integer, ArrayList<RoomConnector>> connectors;
    // This list will contain same connectors as on the Hashmap but as a list and unique.
    private List<RoomConnector> uniqueConnectors;

    private PropertiesLoader props;

    // In the future there will be more themes. Themes will determine different things on the map like textures
    private final MapTheme mapTheme = MapTheme.SPACESHIP;

    private Map<WallOrientation, Material> mapMaterials = null;
    private Map<WallOrientation, Material> connectorsMaterials = null;


    public RandomMap(final int maxPointLights, final int maxSpotLights, final PropertiesLoader props){
        this.maxPointLights = maxPointLights;
        this.maxSpotLights  = maxSpotLights;
        this.props = props;
    }

    private Integer getRandomIntNumberInRange(final Integer min, final Integer max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }


    public List<RoomConnector> getConnectors() {
        return uniqueConnectors;
    }

    public List<RoomConnector> getConnectorsOnRoomIndex(final int roomIndex) {
        return connectors.get(roomIndex);
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
        for (int y=0;y<MAX_CELLS;y++)
            for (int x=0;x<MAX_CELLS;x++){

                int roomIndex = mapMatrix[x][y];
                if (roomIndex>0){
                    Room room = new Room(roomIndex, 1.0f, this.maxPointLights, this.maxSpotLights,
                                         CELL_WIDTH_METERS, CELL_HEIGHT_METERS, CELL_DEPTH_METERS, DOOR_WIDTH,
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

        // Once all the rooms have been created and their connections has been calculated
        // we can create all the geometry
        for (Room room : result)
            room.init();

        return result;
    }

    private Vector3f calcRoomWorldPosition(final int x, final int y) {

        //return new Vector3f(0.0f,0.0f,0.0f);
        Vector3f result = new Vector3f(x*(CELL_WIDTH_METERS + SPACE_BETWEEN_ROOMS_METERS), 0.0f, y*(CELL_DEPTH_METERS + SPACE_BETWEEN_ROOMS_METERS));
        return result;
    }

    private void cleanAllMaterials() {
        for (final WallOrientation dir : WallOrientation.values()) {
            if (mapMaterials != null) {
                final Material material = mapMaterials.get(dir);
                if (material != null) material.cleanUp();
            }
            if (connectorsMaterials != null) {
                final Material material = connectorsMaterials.get(dir);
                if (material != null) material.cleanUp();
            }
        }
    }


    private Map<WallOrientation, Texture> generateTexturesForRoom() throws Exception {

        // In the future when implementing multiple themes
        //if (mapTheme == MapTheme.SPACESHIP) {
        //}
        final HashMap<WallOrientation, Texture> result = new HashMap<>();

        final Texture wallTexture  = new Texture(this.props.getBaseTexturesFolder() + "\\walls_spaceship.png");
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

    private Map<WallOrientation, Texture> generateTexturesForConnectors() throws Exception {

        // In the future when implementing multiple themes
        //if (mapTheme == MapTheme.SPACESHIP) {
        //}
        final HashMap<WallOrientation, Texture> result = new HashMap<>();

        final Texture wallTexture = new Texture(this.props.getBaseTexturesFolder() + "\\tunnel.png");


        for (final WallOrientation dir : WallOrientation.values()) {
            result.put(dir, wallTexture);
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

    private Map<WallOrientation, Material> generateMaterialsForConnectors() throws Exception {

        final HashMap<WallOrientation, Material> result = new HashMap<>();

        final Map<WallOrientation, Texture> textures = generateTexturesForConnectors();

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
            if (mapMaterials != null || connectorsMaterials != null)
                cleanAllMaterials();
            mapMaterials = generateMaterialsForRooms();

            // Create Materials for connectors
            connectorsMaterials = generateMaterialsForConnectors();

            rooms = generateRoomsFromMap();
            connectors = generateConnectors();
            // The connectors contains a hashmap with connector per room. Because two or more rooms can share same connectors
            // same connector will be referenced more than once on the hashmap.
            // For speed in some processes I need the unique list of connectors. This method call generates the unique list.
            uniqueConnectors = generateUniqueConnectors();

        } catch (Exception ex) {
            System.out.println("ERROR generating random map " + ex.getMessage());
        }
    }

    private HashMap<Integer, ArrayList<RoomConnector>> generateConnectors() throws Exception {

        final HashMap<Integer, ArrayList<RoomConnector>> new_connectors = new HashMap<>();

        // This list just helps to not created duplicated connectors.
        // When a connector is created between two rooms a string with format:
        // "room1Index:room2Index" will be created, then before creating a new connector
        // we will verify the string "room1Index:room2Index" or "room2Index:room1Index" does not
        // already exists.
        final ArrayList<String> processedConnectors = new ArrayList<>();

        int connectorIndex = 0;

        for (final Room room : rooms) {
            final Map<WallOrientation, Integer> roomConnections = room.getAdjacentRooms();
            for (final WallOrientation dir : WallOrientation.values()){
                if (roomConnections.get(dir) == -1) continue;

                //Check if this connector has already been created from the other room
                final String currentConnectorName = room.getIndex() + ":" + roomConnections.get(dir);
                final String oppositeConnectorName = roomConnections.get(dir) + ":" + room.getIndex();
                if (processedConnectors.stream().filter(s -> s.equalsIgnoreCase(oppositeConnectorName)).collect(Collectors.toList()).isEmpty()) {
                    processedConnectors.add(currentConnectorName);
                } else {
                    continue;
                }

                // The RoomConnector is assumed to always be pointing north. But rooms could be connected on their west or east walls
                // In that case I need to switch Width vs Depth
                int connectorWidth = DOOR_WIDTH;
                int connectorDepth = SPACE_BETWEEN_ROOMS_METERS;
                if (dir == WallOrientation.LEFT || dir == WallOrientation.RIGHT) {
                    int tempValue = connectorWidth;
                    connectorWidth = connectorDepth;
                    connectorDepth = tempValue;
                }
                RoomConnector newConnector = new RoomConnector(++connectorIndex, 1.0f, 1, 1, connectorWidth,
                        CELL_HEIGHT_METERS, connectorDepth, 0, connectorsMaterials, dir);
                newConnector.setWorldPosition(calcConnectorWorldPosition(room, dir, connectorDepth));
                newConnector.init();

                // Assign connector to current room and to destination room
                ArrayList<RoomConnector> currentRoomConnectors = null;
                if (new_connectors.containsKey(room.getIndex())) {
                    currentRoomConnectors = new_connectors.get(room.getIndex());
                } else {
                    currentRoomConnectors = new ArrayList<>();
                }
                currentRoomConnectors.add(newConnector);
                new_connectors.put(room.getIndex(), currentRoomConnectors);

                ArrayList<RoomConnector> destRoomConnectors = null;
                if (new_connectors.containsKey(roomConnections.get(dir))) {
                    destRoomConnectors = new_connectors.get(roomConnections.get(dir));
                } else {
                    destRoomConnectors = new ArrayList<>();
                }
                destRoomConnectors.add(newConnector);
                new_connectors.put(roomConnections.get(dir), destRoomConnectors); //Both rooms share same connector
            }

        }

        return new_connectors;
    }

    private List<RoomConnector> generateUniqueConnectors() {
        // Generate the unique list of connectors
        // The Hashmap of connectors will have duplicate references since is stored as connectors by room
        // and rooms share same connector.

        final ArrayList<RoomConnector> resultList = new ArrayList<>();
        for (final int roomIndex : connectors.keySet()){
            resultList.addAll(connectors.get(roomIndex)); // All connectors all rooms in one list but with possible duplicates
        }

        // Converting to a set will remove all duplicates
        Set<RoomConnector> targetSet = new HashSet<>(resultList);
        final ArrayList<RoomConnector> uniqueList = new ArrayList<>(targetSet);
        return uniqueList;
    }

    private Vector3f calcConnectorWorldPosition(final Room room, final WallOrientation dir, final int connectorDepth) throws Exception {

          if (dir == WallOrientation.LEFT) {
              return new Vector3f(room.getWorldPosition().x - room.roomWidth/2 - connectorDepth/2,
                                  room.getWorldPosition().y,
                                  room.getWorldPosition().z);
          } else if (dir == WallOrientation.RIGHT) {
              return new Vector3f(room.getWorldPosition().x + room.roomWidth/2 + connectorDepth/2,
                      room.getWorldPosition().y,
                      room.getWorldPosition().z);
          } else if (dir == WallOrientation.FRONT) {
              return new Vector3f(room.getWorldPosition().x,
                      room.getWorldPosition().y,
                      room.getWorldPosition().z - room.roomDepth/2 - connectorDepth/2);
          } else if (dir == WallOrientation.BACK) {
              return new Vector3f(room.getWorldPosition().x,
                      room.getWorldPosition().y,
                      room.getWorldPosition().z + room.roomDepth/2 + connectorDepth/2);
          }

          throw new Exception("Not valid value of WallOrientation for Room Connector");
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
        totalGeneratedRooms = 0;
        mapMatrix = new int[MAX_CELLS][MAX_CELLS];
        for (Room room : rooms)
            room.cleanUp();
        for (RoomConnector connector : uniqueConnectors)
            connector.cleanUp();
        cleanAllMaterials();
    }

    public Vector2i getPLayerCellPosition(final Vector3f worldPlayerPosition){
        int x_cell = Math.round(worldPlayerPosition.x / CELL_WIDTH_METERS);
        int y_cell = Math.round(worldPlayerPosition.z / CELL_DEPTH_METERS);
        return new Vector2i(x_cell,y_cell);
    }

//    public List<Planef> getPlanesFromRoomAtCell(final Vector2i cellPosition) {
//        final int roomIndex = mapMatrix[cellPosition.x][cellPosition.y];
//        final Room room = rooms.stream()
//                               .filter(r->r.getIndex() == roomIndex)
//                               .collect(Collectors.toList()).get(0);
//        final List<RoomConnector> roomConnectors = room.getRoomConnectors();
//
//    }
}
