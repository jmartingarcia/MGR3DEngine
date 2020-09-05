package com.mgr.myshooter;

import com.mgr.engine.DirectionalLight;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.sql.Time;
import java.util.List;

public class World {

    private final Integer MAX_NUMBER_ROOMS = 5;
    private RandomMap map;
    private DirectionalLight sun;
    private int time; //In minutes: from 0 to 1440, where 720 is noon

    public World(){
        map = new RandomMap();

        //Assume time starts as noon, the sun is right on top of us
        sun  = new DirectionalLight(new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -1.0f, 0.0f), 0.5f);
        time = 720;
    }

    public void init(){
        map.generateRandomMap(MAX_NUMBER_ROOMS);
    }

    public List<Room> getMapRooms(){
        return map.getRooms();
    }

    public void cleanUp() {
        map.cleanUp();
    }

    public void setTime(int minutesAfterMidnight) {
        time = minutesAfterMidnight;
        calcSunPositionAndLightColor();
    }

    public void addTime(int elapsedMinutes) {
        System.out.println(" Time is "+ time/60 +" hours");
        time += elapsedMinutes;
        if (time >= 1440) { // 24 hours
            time = time - 1440;
        } else if (time <= 0) { // In case I ever let the time rewind
            time = 1440 + time;
        }
        calcSunPositionAndLightColor();
    }

    public DirectionalLight getSunLight() {
        return sun;
    }

    // Based on time calculates the position of the sun and color of the light
    private void calcSunPositionAndLightColor() {
        // When noon will be 0 degrees, when 6 am will be -90 and 6 pm 90
        float lightAngle = time/4 - 180;

        if (lightAngle > 90 || lightAngle < -90) {
            sun.setIntensity(0); // night
        } else if (lightAngle <= -80 || lightAngle >= 80) { // As closer to dawn or dusk the intensity is different and the color too (if the light)
            float factor = 1 - (Math.abs(lightAngle) - 80) / 10.0f;
            sun.setIntensity(factor);
            sun.setColor(new Vector3f(sun.getColor().x, Math.max(factor, 0.9f), Math.max(factor, 0.5f) ));
        } else {
            sun.setIntensity(1);
            sun.setColor(new Vector3f(1,1,1));
        }
        double angRad = Math.toRadians(lightAngle);
        sun.setDirection(new Vector3f((float) Math.sin(angRad),(float) Math.cos(angRad),sun.getDirection().z));
    }
}
