package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainMenuView {

    public static VBox create(BorderPane root) {

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Button addEntryBtn = new Button("Add New Entry");
        Button searchPokemonBtn = new Button("Search Pokémon Statistics");
        Button searchTagBtn = new Button("Search Tagged Pokémon");
        Button overallStatsBtn = new Button("Overall Statistics");
        Button yearlyStatsBtn = new Button("Past Yearly Statistics");

        addEntryBtn.setPrefWidth(250);
        searchPokemonBtn.setPrefWidth(250);
        searchTagBtn.setPrefWidth(250);
        overallStatsBtn.setPrefWidth(250);
        yearlyStatsBtn.setPrefWidth(250);

        //klicka addentry och fyll i formulär
        addEntryBtn.setOnAction(e ->
                root.setCenter(AddEntryView.create())
        );

        layout.getChildren().addAll(
                addEntryBtn,
                searchPokemonBtn,
                searchTagBtn,
                overallStatsBtn,
                yearlyStatsBtn
        );

        return layout;
    }
}