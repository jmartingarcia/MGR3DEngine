package com.mgr.engine;

import java.util.*;
//import java.util.stream.Collectors;

public class RandomMap {

    private final int MAX_CELLS = 10;
    private final int CELL_SIDE_METERS = 30;
    private int[][] mapMatrix = new int[MAX_CELLS][MAX_CELLS];
    private int totalGeneratedRooms = 0;
    private List<Room> rooms;

    /*private final List<String> roomTypes = Arrays.asList("NSWE","NSW","NSE","NS","WE","W","E","N","S","SE","SW","NW","NE");
    private final Map<String, String> directionOpposites = new HashMap<String, String>() {{
        put("N", "S");
        put("S", "N");
        put("E", "W");
        put("W", "E");
    }};*/

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

    private List<Room> generateRoomsFromMap(){

        final List<Room> result = new ArrayList<>();

        // Read the map plan and generate the actual com.mgr.engine.Room object with it's connections
        for (int x=0;x<MAX_CELLS;x++)
            for (int y=0;y<MAX_CELLS;y++){

                int roomIndex = mapMatrix[x][y];
                if (roomIndex>0){
                    Room room = new Room(roomIndex);
                    //Link with room on the West
                    if (x > 0 && mapMatrix[x-1][y] != 0)
                        room.setPathWithRoomIndex("W",mapMatrix[x-1][y]);
                    //Link with room on the East
                    if (x < MAX_CELLS-1 && mapMatrix[x+1][y] != 0)
                        room.setPathWithRoomIndex("E",mapMatrix[x+1][y]);
                    //Link with room on the North
                    if (y > 0 && mapMatrix[x][y-1] != 0)
                        room.setPathWithRoomIndex("N",mapMatrix[x][y-1]);
                    //Link with room on the South
                    if (y < MAX_CELLS-1 && mapMatrix[x][y+1] != 0)
                        room.setPathWithRoomIndex("S",mapMatrix[x][y+1]);

                    room.setMapPosition(x,y);
                    result.add(room);
                }

            }

        return result;
    }

    public void generateRandomMap(Integer totalNumberRooms) {
        generateMap(MAX_CELLS/2, MAX_CELLS/2 , totalNumberRooms);
        rooms = generateRoomsFromMap();
        for (Room room : rooms)
            room.createModel(CELL_SIDE_METERS);

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

    public void drawMap() {
        //for (com.mgr.engine.Room room : rooms)
        //    room.drawRoom();
        Room room = rooms.get(0);
        room.drawRoom();
    }

    public void cleanUp(){
        for (Room room : rooms)
            room.cleanUp();
    }

}
