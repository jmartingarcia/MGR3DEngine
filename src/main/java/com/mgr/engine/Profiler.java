package com.mgr.engine;


import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

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
