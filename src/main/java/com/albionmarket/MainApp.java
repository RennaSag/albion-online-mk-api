package com.albionmarket;

import com.albionmarket.ui.TelaChangelog;
import com.albionmarket.ui.TelaLogin;
import javafx.application.Application;
import javafx.stage.Stage;
import com.albionmarket.ui.AutoUpdater;

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

        new TelaLogin(palco).mostrar();

        if (TelaChangelog.deveExibir()) {
            new TelaChangelog(palco).mostrar();
        }

        AutoUpdater.verificar(palco);


    }

    public static void main(String[] args) {
        launch(args);
    }
}
