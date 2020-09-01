package com.mgr.engine;


import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBEasyFont.*;

public class Profiler {


    private final Map<String, String> profilerInfo = new HashMap<>();;


    public void Init(){
    }

    public void displayProfileData() {
        for (String key : profilerInfo.keySet()) {
            System.out.println(key + " : " + profilerInfo.get(key));
        }
    }

    public void setProfilerEntry(String key, String value) {
         if (profilerInfo.containsKey(key)) {
            String oldValue = profilerInfo.get(key);
            profilerInfo.replace(key, oldValue, value);
        } else {
            profilerInfo.put(key, value);
        }
    }

}
