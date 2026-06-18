package io.github.naromil.qcsimple;

import javafx.scene.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

public class EditorController {
    private static Group worldRoot;
    private static Group cameraPivot;
    private static double mousePosX, mousePosY;
    private static double mouseOldX, mouseOldY;

    protected static void initEditorScene(StackPane renderPane) {
        worldRoot = new Group();

        // 1. Setup Camera and Pivot
        cameraPivot = new Group();
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-50); // Pull the camera back so we can see the center
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        cameraPivot.getChildren().add(camera);
        worldRoot.getChildren().add(cameraPivot);

        // 2. Create the SubScene
        // Using BALANCED antialiasing keeps the edges crisp without killing performance
        SubScene subScene = new SubScene(worldRoot, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#3c3f41")); // A nice dark editor background
        subScene.setCamera(camera);

        // Bind the SubScene size to the StackPane so it resizes with the window
        subScene.widthProperty().bind(renderPane.widthProperty());
        subScene.heightProperty().bind(renderPane.heightProperty());

        // 3. Add to UI
        renderPane.getChildren().add(subScene);

        // 4. Attach interactive controls
        setupMouseControls(subScene);
    }

    private static void setupMouseControls(SubScene subScene) {
        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        cameraPivot.getTransforms().addAll(rotateX, rotateY);

        subScene.setOnMousePressed(event -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            double mouseDeltaX = (mousePosX - mouseOldX);
            double mouseDeltaY = (mousePosY - mouseOldY);

            // Adjust rotation based on drag distance
            if (event.isPrimaryButtonDown()) { // Left click drag to orbit
                rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * 0.5); // Pitch
                rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * 0.5); // Yaw
            }
        });

        subScene.setOnMouseClicked(event -> {
            // Prevent building/destroying if the user was just dragging the camera around
            if (!event.isStillSincePress()) return;

            PickResult pickResult = event.getPickResult();
            Node intersectedNode = pickResult.getIntersectedNode();

            // Check if the ray cast actually hit a Box (and not empty background space)
            if (intersectedNode instanceof Box clickedBlock) {
                
                if (event.getButton() == MouseButton.PRIMARY) {
                    // LEFT CLICK: Destroy block
                    // destroyBlock(clickedBlock);
                    worldRoot.getChildren().remove(clickedBlock);
                    // TODO
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                    // RIGHT CLICK: Build new block
                    // buildBlock(clickedBlock, pickResult.getIntersectedPoint());
                }
            }
            // Check if the ray cast doesn't hit a Box but there are no Box present
            else {
                buildStructure(0, 0, 0, "grass");
                // TODO
            }
        });
    }

    public static void buildStructure(int x, int y, int z, String blockName) {
        // 1x1x1 unit size for a standard block
        Box block = new Box(1, 1, 1);

        // Position the block. Notice the negative 'y' to flip the axis!
        block.setTranslateX(x);
        block.setTranslateY(-y);
        block.setTranslateZ(z);

        // Apply a basic color based on the block name for testing
        PhongMaterial material = new PhongMaterial();
        if (blockName.contains("stone")) material.setDiffuseColor(Color.GRAY);
        else if (blockName.contains("wood") || blockName.contains("log")) material.setDiffuseColor(Color.BROWN);
        else material.setDiffuseColor(Color.MAGENTA); // Missing texture fallback

        block.setMaterial(material);

        // Add interactivity to individual blocks
        block.setOnMouseClicked(event -> {
            System.out.println("Clicked block at: " + x + ", " + y + ", " + z);
            // Here you can change the material to show it is selected!
        });

        worldRoot.getChildren().add(block);
    }
}
