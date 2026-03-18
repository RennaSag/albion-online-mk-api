package com.albionmarket;

import com.albionmarket.ui.TelaLogin;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage palco) {


        //diferentes tamanho de icone pro windows se adaptar
        palco.getIcons().addAll(
                new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/icone.png"), 16, 16, true, true),
                new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/icone.png"), 32, 32, true, true),
                new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/icone.png"), 64, 64, true, true),
                new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/icone.png"), 128, 128, true, true),
                new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/icone.png"), 256, 256, true, true)
        );

        // palco.setMaximized(true);
        // palco.setResizable(true);
        new TelaLogin(palco).mostrar();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
