package io.github.naromil.qcsimple.main;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BlockConfigController {
    @FXML
    protected TextField blocFramework, blocColumn, blocRow, blocFloor;

    @FXML
    public void initialize() {
        // Here you can populate fields from your EditorState configuration on load
        // e.g., baseBlockIdField.setText(EditorState.getInstance().getBaseBlockId());
    }

    @FXML
    protected void onSaveAction() {
        String enteredId = blocFramework.getText();
        System.out.println("Saved Block ID mapping: " + enteredId);

        // TODO: Save this mapping to your EditorState configuration here

        closeWindow();
    }

    @FXML
    protected void onCancelAction() {
        closeWindow();
    }

    private void closeWindow() {
        // Grabs the current stage window anchor and closes it cleanly
        Stage stage = (Stage) blocFramework.getScene().getWindow();
        stage.close();
    }
}