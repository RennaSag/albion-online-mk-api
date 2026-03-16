package com.albionmarket;

import com.albionmarket.ui.TelaLogin;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage palco) {

        palco.setMaximized(true);
        palco.setResizable(true);
        new TelaLogin(palco).mostrar();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
