package com.mgr.engine;

public class GameEngine implements Runnable {

    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;
    private final Window window;
    private final Timer timer;
    private final IGameLogic gameLogic;
    private MouseInput mouseInput;



    public GameEngine(String windowTitle, int width, int height, boolean vSync, IGameLogic gameLogic) throws Exception {
        window = new Window(windowTitle, width, height, vSync);
        this.gameLogic = gameLogic;
        timer = new Timer();
        mouseInput = new MouseInput();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            // Clean up everything since the game is closing
            cleanUp();
        }
    }

    protected void init() throws Exception {
        window.init();
        timer.init();
        gameLogic.init(window);
        mouseInput.init(window);
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS; //Updates per second (separate then frame per second). This is more for physics calculation. More important!
        int   numberFrames = 0;
        double fps_time = 0;

        boolean running = true;
        while (running && !window.windowShouldClose()) {

            elapsedTime = timer.getElapsedTime();

            // Calculate the actual frames per second
            if (fps_time >= 1.0f){
                gameLogic.setActualFramesPerSecond(numberFrames);
                numberFrames = 0;
                fps_time = 0;
            }

            fps_time += elapsedTime;
            numberFrames++;

            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();

            if (!window.isvSync()) {
                sync();
            }
        }
    }

    private void sync() {
        float loopSlot = 1f / TARGET_FPS;  // Frames per second
        double endTime = timer.getLastLoopTime() + loopSlot;
        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }

    protected void input() {

        mouseInput.input(window);
        gameLogic.input(window);

    }

    protected void update(float interval) {

        gameLogic.update(interval, mouseInput);
    }

    protected void render() {
        gameLogic.render(window);
        window.update();
    }

    protected void cleanUp() {
        gameLogic.cleanUp();
    }
}
