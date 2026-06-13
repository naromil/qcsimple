package naromil.qcsimple;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    // This injects the Label from the FXML file based on its fx:id
    @FXML
    private Label welcomeText;

    // This method runs automatically when the button is clicked
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("You clicked the button! It works! 🎉");
    }
}