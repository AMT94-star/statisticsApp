package app;

import database.DatabaseManager;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ui.MainMenuView;
import ui.UsernameSetupView;

import static javafx.stage.Screen.getPrimary;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        DatabaseManager.initializeDatabase();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #111827;");

        Rectangle2D bounds = getPrimary().getVisualBounds();
        double width = bounds.getWidth() * 0.9;
        double height = bounds.getHeight() * 0.9;

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(
                MainApp.class.getResource("/styles/app.css").toExternalForm()
        );

        stage.setTitle("");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();

        if (UsernameSetupView.getSavedUsername() == null) {
            root.setCenter(UsernameSetupView.create(() ->
                    root.setCenter(MainMenuView.create(root))
            ));
        } else {
            root.setCenter(MainMenuView.create(root));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
