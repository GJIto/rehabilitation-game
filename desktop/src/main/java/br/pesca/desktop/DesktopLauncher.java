package br.pesca.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import br.pesca.PescaGame;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Pesca no Ritmo");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new PescaGame(), config);
    }
}
