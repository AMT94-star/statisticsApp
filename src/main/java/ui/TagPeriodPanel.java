package ui;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.PokemonEntry;

public class TagPeriodPanel {

    public static Node create(ObservableList<PokemonEntry> periodEntries, Runnable reloadCurrentTab) {
        return create(periodEntries, reloadCurrentTab, null);
    }

    public static Node create(ObservableList<PokemonEntry> periodEntries, Runnable reloadCurrentTab, HBox addBtnSlot) {
        FilteredList<PokemonEntry> filtered = new FilteredList<>(periodEntries, e -> true);

        TableView<PokemonEntry> table = Tables.makeEntryTableForFilteredList(filtered, reloadCurrentTab);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox tableCard = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        if (addBtnSlot != null) addBtnSlot.getChildren().clear();
        Node chartsPanel = CustomChartsPanel.create(filtered, true, "default", addBtnSlot);

        VBox rightCard = new VBox(chartsPanel);
        rightCard.setStyle("-fx-background-color: #111827;");
        VBox.setVgrow(chartsPanel, Priority.ALWAYS);

        SplitPane split = new SplitPane(tableCard, rightCard);
        split.setDividerPositions(0.55);
        split.setStyle("-fx-background-color: #111827;");
        split.skinProperty().addListener((obs, o, n) ->
            split.lookupAll(".split-pane-divider").forEach(d ->
                d.setStyle("-fx-background-color: #111827; -fx-padding: 0 1 0 1;"))
        );

        return split;
    }
}
