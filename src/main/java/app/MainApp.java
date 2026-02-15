package app;

import database.DatabaseManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ui.MainMenuView;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        // Starta databasen när appen startar
        DatabaseManager.initializeDatabase();

        TabPane tabPane = new TabPane();

        BorderPane mainMenuRoot = new BorderPane();
        mainMenuRoot.setCenter(MainMenuView.create(mainMenuRoot));

        Tab mainMenuTab = new Tab("Main Menu", mainMenuRoot);
        Tab monthlyTab = new Tab("Monthly Statistics");
        Tab yearlyTab = new Tab("Year Statistics");

        mainMenuTab.setClosable(false);
        monthlyTab.setClosable(false);
        yearlyTab.setClosable(false);

        tabPane.getTabs().addAll(mainMenuTab, monthlyTab, yearlyTab);

        Scene scene = new Scene(tabPane, 1200, 800);

        stage.setTitle("Kevin's Pokémon Statistics");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}