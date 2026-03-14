package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UsernameSetupView {

    private static Path getDataFile() {
        String appData = System.getenv("APPDATA");
        if (appData == null) appData = System.getProperty("user.home");
        Path dir = Path.of(appData, "PokemonStatistics");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
        }
        return dir.resolve("user.txt");
    }

    public static String getSavedUsername() {
        try {
            Path f = getDataFile();
            if (!Files.exists(f)) return null;
            String val = Files.readString(f).trim();
            return val.isEmpty() ? null : val;
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveUsername(String name) {
        try {
            Files.writeString(getDataFile(), name.trim());
        } catch (IOException ignored) {
        }
    }

    public static VBox create(Runnable onComplete) {

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(24);
        root.setStyle("-fx-background-color: #0f1623;");

        // ---- Pokéball ----
        StackPane pokeball = buildPokeball();

        // ---- Hälsning ----
        Label greeting = new Label("Hi my love :)");
        greeting.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #6b7280;" +
                        "-fx-font-weight: 400;"
        );

        // ---- Textfält ----
        TextField nameField = new TextField();
        nameField.setPromptText("Name:");
        nameField.setMaxWidth(400);
        nameField.setPrefHeight(48);
        nameField.setStyle(
                "-fx-background-color: #1a2235;" +
                        "-fx-text-fill: #e8eaf0;" +
                        "-fx-prompt-text-fill: #4b5563;" +
                        "-fx-border-color: #2a3347;" +
                        "-fx-border-radius: 24px;" +
                        "-fx-background-radius: 24px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 0 20 0 20;"
        );

        // ---- Felmeddelande ----
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        // ---- Knapp ----
        Button continueBtn = new Button("Press to continue");
        continueBtn.setMaxWidth(340);
        continueBtn.setPrefHeight(52);
        continueBtn.setStyle(
                "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-background-radius: 26px; " +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;"
        );
        continueBtn.setOnMouseEntered(e ->
                continueBtn.setStyle(continueBtn.getStyle().replace("#3b82f6", "#2563eb"))
        );
        continueBtn.setOnMouseExited(e ->
                continueBtn.setStyle(continueBtn.getStyle().replace("#2563eb", "#3b82f6"))
        );

        Runnable submit = () -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                errorLabel.setText("Please enter a Trainer name.");
                errorLabel.setVisible(true);
                return;
            }
            if (name.length() > 32) {
                errorLabel.setText("Name must be 32 characters or fewer.");
                errorLabel.setVisible(true);
                return;
            }
            saveUsername(name);
            onComplete.run();
        };

        continueBtn.setOnAction(e -> submit.run());
        nameField.setOnAction(e -> submit.run());
        nameField.textProperty().addListener((obs, o, n) -> errorLabel.setVisible(false));

        root.getChildren().addAll(pokeball, greeting, nameField, errorLabel, continueBtn);

        return root;
    }

    private static StackPane buildPokeball() {
        //yttre cirkel
        Circle outer = new Circle(48, Color.web("#2a3347"));

        //horisontell linje
        Rectangle line = new Rectangle(96, 5, Color.web("#0f1623"));

        //inre ring
        Circle ring = new Circle(14, Color.web("#0f1623"));

        //centrum punkt
        Circle center = new Circle(9, Color.web("#2a3347"));

        StackPane ball = new StackPane(outer, line, ring, center);
        ball.setPrefSize(96, 96);
        ball.setMaxSize(96, 96);
        return ball;
    }
}
