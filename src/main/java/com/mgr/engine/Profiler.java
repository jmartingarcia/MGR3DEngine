package com.mgr.engine;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profiler {


    private final Map<String, TextItem> profilerInfo = new HashMap<>();;


    public void Init(){
    }

    public List<TextItem> getTextItems() {
        return new ArrayList<TextItem>(profilerInfo.values());
    }

    public void displayProfileData() {
        for (String key : profilerInfo.keySet()) {
           System.out.println(key + " : " + profilerInfo.get(key));
        }
    }

    public void setProfilerEntry(String key, String value) {
         if (profilerInfo.containsKey(key)) {
            TextItem item = profilerInfo.get(key);
            item.setText(value);
        } else {
             try {
                 TextItem item = new TextItem(value, "/Users/mgarciar/Documents/Personal/Workspace/MGR3DEngine/build/resources/main/font_texture.png", 16, 16);
                 profilerInfo.put(key, item);
                 //Set position of text on screen
                 float posy = (profilerInfo.values().size())*30.0f;
                 item.setPosition(0.0f, posy, 0);
             } catch (Exception ex){
               System.out.println("Could not set profile entry with key " + key + ". Error = " + ex.getMessage());
             }
        }
    }

}
