package com.albionmarket;

import com.albionmarket.ui.TelaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage palco) {
        TelaPrincipal tela = new TelaPrincipal();

        Scene cena = new Scene(tela.getCriarLayout(), 1280, 800);
        cena.getStylesheets().add(
            getClass().getResource("/estilos.css").toExternalForm()
        );

        palco.setTitle("Albion Market: Consulta de Preços");
        palco.setScene(cena);
        palco.setMinWidth(900);
        palco.setMinHeight(600);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
