package io.github.naromil.qcsimple.main;

import io.github.naromil.qcsimple.data.DataConverter;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BlockConfigController {
    @FXML
    protected TextField blocFramework, blocColumn, blocRow, blocFloor;

    @FXML
    public void initialize() {
        // 1. Load data from DataConverter into the UI
        // We use a fallback ternary operator just in case a field is somehow null
        blocFramework.setText(DataConverter.frameworkId != null ? DataConverter.frameworkId : "");
        blocColumn.setText(DataConverter.columnId != null ? DataConverter.columnId : "");
        blocRow.setText(DataConverter.rowId != null ? DataConverter.rowId : "");
        blocFloor.setText(DataConverter.floorId != null ? DataConverter.floorId : "");
    }

    @FXML
    protected void onSaveAction() {
        // 2. Push data from the UI back into the DataConverter
        DataConverter.frameworkId = blocFramework.getText().trim();
        DataConverter.columnId = blocColumn.getText().trim();
        DataConverter.rowId = blocRow.getText().trim();
        DataConverter.floorId = blocFloor.getText().trim();

        System.out.println("Saved Block ID mappings successfully.");
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