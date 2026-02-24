package ui;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.PokemonEntry;

public class PeriodPanel {

    public static Node create(ObservableList<PokemonEntry> entries, Runnable reload) {
        return create(entries, reload, null);
    }

    public static Node create(ObservableList<PokemonEntry> entries, Runnable reload, HBox addBtnSlot) {
        FilteredList<PokemonEntry> filtered = new FilteredList<>(entries, e -> true);

        //vänster tabell
        TableView<PokemonEntry> table = Tables.makeEntryTableForFilteredList(filtered, reload);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox tableCard = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        //höger grafer
        if (addBtnSlot != null) addBtnSlot.getChildren().clear();
        Node chartsPanel = CustomChartsPanel.create(filtered, false, "default", addBtnSlot);

        VBox rightCard = new VBox(chartsPanel);
        rightCard.setStyle("-fx-background-color: #111827;");
        VBox.setVgrow(chartsPanel, Priority.ALWAYS);

        SplitPane split = new SplitPane(tableCard, rightCard);
        split.setDividerPositions(0.55);
        split.setStyle("-fx-background-color: #111827;");

        //göm delaren
        split.skinProperty().addListener((obs, o, n) ->
                split.lookupAll(".split-pane-divider").forEach(d ->
                        d.setStyle("-fx-background-color: #111827; -fx-padding: 0 1 0 1;"))
        );

        return split;
    }
}
