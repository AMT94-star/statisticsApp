package ui;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import model.PokemonEntry;
import service.EntryService;
import service.StatsService;

import java.util.ArrayList;
import java.util.List;

public class OverallStatsView {

    public static BorderPane create(BorderPane appRoot) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");

        HBox header = AppHeader.create(appRoot, "Overall Statistics", "");

        //sökfält
        TextField searchField = new TextField();
        searchField.setPromptText("Search Pokémon name or tag...");
        searchField.setPrefWidth(280);
        searchField.setStyle("-fx-background-color: #1F2937; -fx-text-fill: #e8eaf0; " +
                "-fx-border-color: #374151; -fx-border-radius: 6px; -fx-background-radius: 6px; " +
                "-fx-prompt-text-fill: #4b5563; -fx-padding: 6 10 6 10;");

        Button searchBtn = new Button("Search");
        Button clearBtn = new Button("Clear");
        searchBtn.getStyleClass().add("primary");
        clearBtn.setStyle("-fx-background-color: #1F2937; -fx-text-fill: #9ca3af; " +
                "-fx-border-color: #374151; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");

        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");

        HBox controls = new HBox(10, searchLabel, searchField, searchBtn, clearBtn);
        controls.setPadding(new Insets(10, 16, 10, 16));
        controls.setStyle("-fx-background-color: #0d1117; -fx-border-color: transparent transparent #1e2235 transparent; -fx-border-width: 0 0 1 0;");
        controls.setAlignment(Pos.CENTER_LEFT);

        root.setTop(new VBox(header, controls));

        //data
        ObservableList<PokemonEntry> allEntries = FXCollections.observableArrayList();
        reloadAll(allEntries);
        FilteredList<PokemonEntry> filtered = new FilteredList<>(allEntries, e -> true);

        //vänstra tabellen
        TableView<PokemonEntry> table = Tables.makeEntryTableForFilteredList(filtered, () -> reloadAll(allEntries));
        table.setMinWidth(400);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-background-color: #161b27;");

        VBox tableCard = new VBox();
        tableCard.setStyle("-fx-background-color: #161b27;");
        Label tableTitle = new Label("All Entries");
        tableTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; " +
                "-fx-text-fill: #e8eaf0; -fx-padding: 10 0 6 12;");
        tableCard.getChildren().addAll(tableTitle, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        //högra tabellen
        GridPane statGrid = buildStatGrid(filtered);
        VBox statWrapper = new VBox(statGrid);
        statWrapper.setPadding(new Insets(10, 10, 0, 10));
        statWrapper.setStyle("-fx-background-color: #0d1117;");

        PauseTransition statDebounce = new PauseTransition(Duration.millis(150));
        statDebounce.setOnFinished(ev -> {
            GridPane newGrid = buildStatGrid(filtered);
            statWrapper.getChildren().setAll(newGrid);
        });
        filtered.predicateProperty().addListener((obs, o, n) -> {
            statDebounce.stop();
            statDebounce.playFromStart();
        });

        HBox addBtnContainer = new HBox();
        addBtnContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        HBox.setHgrow(addBtnContainer, Priority.ALWAYS);

        HBox chartBar = new HBox(addBtnContainer);
        chartBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        chartBar.setPadding(new Insets(4, 8, 4, 8));
        chartBar.setStyle(
                "-fx-background-color: #0d1117;" +
                        "-fx-border-color: transparent transparent #1e2235 transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Node chartsPanel = CustomChartsPanel.create(filtered, false, "overall stats", addBtnContainer);

        VBox rightCard = new VBox(statWrapper, chartBar, chartsPanel);
        rightCard.setStyle("-fx-background-color: #0d1117;");
        VBox.setVgrow(chartsPanel, Priority.ALWAYS);

        //splitpane
        SplitPane split = new SplitPane(tableCard, rightCard);
        split.setDividerPositions(0.60);
        split.setStyle("-fx-background-color: #0d1117;");
        root.setCenter(split);

        //söklogik
        Runnable applySearch = () -> {
            String query = searchField.getText().trim().toLowerCase();
            filtered.setPredicate(query.isEmpty() ? e -> true : e -> {
                String name = (e.getPokemonName() == null ? "" : e.getPokemonName()).toLowerCase();
                String tag = (e.getTag() == null ? "" : e.getTag()).toLowerCase();
                return name.contains(query) || tag.contains(query);
            });
        };

        searchBtn.setOnAction(e -> applySearch.run());
        searchField.setOnAction(e -> applySearch.run());
        clearBtn.setOnAction(e -> {
            searchField.clear();
            filtered.setPredicate(e2 -> true);
        });

        AutoComplete.bind(searchField, EntryService.getInstance().getDistinctTags());

        return root;
    }

    private static GridPane buildStatGrid(FilteredList<PokemonEntry> filtered) {
        List<PokemonEntry> entries = new ArrayList<>(filtered);
        StatsService.StatsSnapshot snap = StatsService.getInstance().compute(entries);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 10, 0));

        grid.add(statCard("Total entries", String.valueOf(snap.total())), 0, 0);
        grid.add(statCard("Caught", pctLabel(snap.caught(), snap.total())), 1, 0);
        grid.add(statCard("Shiny", snap.total() == 0 ? "–"
                : snap.shiny() + " (" + String.format("%.2f%%", snap.shinyRate() * 100) + ")"), 0, 1);
        grid.add(statCard("Avg CP", snap.total() == 0 ? "–"
                : String.format("%.0f", snap.avgCp())), 1, 1);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, cc);
        return grid;
    }

    private static Node statCard(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #e8eaf0;");
        val.setWrapText(true);
        VBox box = new VBox(4, lbl, val);
        box.setStyle("-fx-background-color: #1a2035; -fx-background-radius: 8px; -fx-padding: 14px;");
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static void reloadAll(ObservableList<PokemonEntry> list) {
        list.setAll(EntryService.getInstance().getAllEntries());
    }

    private static String pctLabel(int part, int total) {
        if (total == 0) return "–";
        return part + " (" + String.format("%.1f%%", 100.0 * part / total) + ")";
    }
}
