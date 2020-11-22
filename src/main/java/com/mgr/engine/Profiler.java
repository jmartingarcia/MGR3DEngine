package com.mgr.engine;


import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.items.TextItem;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profiler {


    private final Map<String, TextItem> profilerInfo = new HashMap<>();;

    private PropertiesLoader props;

    public void init(PropertiesLoader props){
        this.props = props;
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
                 TextItem item = new TextItem(value, this.props.getBaseTexturesFolder() + "\\font_texture.png", 16, 16);
                 profilerInfo.put(key, item);
                 //Set position of text on screen
                 float posy = (profilerInfo.values().size())*30.0f;
                 item.setPosition(new Vector3f(0.0f, posy, 0));
             } catch (Exception ex){
               System.out.println("Could not set profile entry with key " + key + ". Error = " + ex.getMessage());
             }
        }
    }

    public void cleanUp() {
        for (String key : profilerInfo.keySet()) {
            TextItem item = profilerInfo.get(key);
            if (item != null) item.cleanUp();
        }
    }

}
