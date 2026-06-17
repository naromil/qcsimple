package io.github.naromil.qcsimple;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 1. Get the visual bounds of the primary screen
        // (Using getVisualBounds() excludes the OS taskbar/dock)
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // 2. Calculate proportional dimensions based on your reference ratio
        double calculatedWidth = screenWidth * (960.0 / 1920.0);
        double calculatedHeight = screenHeight * (640.0 / 1080.0);

        // 3. Load the FXML layout file
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));

        // 4. Pass the dynamically calculated width and height to the Scene
        Scene scene = new Scene(fxmlLoader.load(), calculatedWidth, calculatedHeight);

        stage.setTitle("Quarterchunk Simple");
        stage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(MainApp.class.getResourceAsStream("icon.png"))));
        stage.setScene(scene);
        stage.show(); // Makes the window visible
    }

    public static void main(String[] args) {
        launch(args); // It's good practice to pass args here
    }
}