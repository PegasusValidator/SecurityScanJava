package de.pegasusvalidator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        JNAInit.init();
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));

        try {
            Parent root = loader.load();

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());

            primaryStage.setScene(new Scene(root, bounds.getWidth(), bounds.getHeight()));
            primaryStage.setTitle("IPhone Pegasus Scanner 2021 Light");
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
