package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MainMenuView {
    public static VBox create() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(10);

        Button addEntryButton = new Button("Add New Entry");
        Button searchPokemonButton = new Button("Search Pokémon Statistics");
        Button searchTagsButton = new Button("Search Tagged Pokemon");
        Button overallButton = new Button("Overall Statistics");
        Button yearlyButton = new Button("Yearly Statistics");

        addEntryButton.setPrefWidth(250);
        searchPokemonButton.setPrefWidth(250);
        searchTagsButton.setPrefWidth(250);
        overallButton.setPrefWidth(250);
        yearlyButton.setPrefWidth(250);

        layout.getChildren().addAll(
                addEntryButton,
                searchPokemonButton,
                searchTagsButton,
                overallButton,
                yearlyButton);
        return layout;
    }
}
