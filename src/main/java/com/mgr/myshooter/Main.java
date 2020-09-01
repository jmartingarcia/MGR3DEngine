package com.mgr.myshooter;

import com.mgr.engine.GameEngine;
import com.mgr.engine.IGameLogic;

import java.nio.charset.Charset;

public class Main {

    public static void main(String[] args) {

        try {
            boolean vSync = true;
            IGameLogic gameLogic = new MGRGame();
            GameEngine gameEng = new GameEngine("MGR GAME", 1024, 768, vSync, gameLogic);
            gameEng.run();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }

}