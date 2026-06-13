package naromil.qcsimple;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Loads the FXML layout file
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("main-view.fxml"));

        // Creates a scene with the layout (Width: 320px, Height: 240px)
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        stage.setTitle("My First JavaFX App");
        stage.setScene(scene);
        stage.show(); // Makes the window visible
    }

    public static void main(String[] args) {
        launch();
    }
}