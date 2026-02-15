package app;

import database.DatabaseManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ui.MainMenuView;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        // Starta databasen när appen startar
        DatabaseManager.initializeDatabase();

        // Skapa TabPane (flikar)
        TabPane tabPane = new TabPane();

        Tab mainMenuTab = new Tab("Main Menu");
        mainMenuTab.setContent(MainMenuView.create());
        Tab monthlyTab = new Tab("Monthly Statistics");
        Tab yearlyTab = new Tab("Year Statistics");

        // Gör så att man inte kan stänga flikar
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