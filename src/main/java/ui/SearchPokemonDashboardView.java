package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.PokemonEntry;
import service.EntryService;
import service.ExcelService;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class SearchPokemonDashboardView {

    public static BorderPane create(BorderPane appRoot) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #111827;");

        HBox header = AppHeader.create(appRoot, "Pokémon Statistics", "");

        //kontroller
        ComboBox<String> pokemonCombo = new ComboBox<>();
        pokemonCombo.setEditable(true);
        pokemonCombo.setPromptText("Search or pick Pokémon...");
        pokemonCombo.setPrefWidth(230);

        Spinner<Integer> yearSpinner = new Spinner<>(2000, 2100, Year.now().getValue());
        yearSpinner.setEditable(true);
        yearSpinner.setPrefWidth(100);

        Button loadBtn = new Button("Load");
        Button clearBtn = new Button("Clear");
        Button addBtn = new Button("Add Entry");
        Button exportBtn = new Button("Export Excel");
        Button importBtn = new Button("Import Excel");
        loadBtn.getStyleClass().add("primary");
        addBtn.getStyleClass().add("primary");
        String secStyle = "-fx-background-color: #1F2937; " +
                "-fx-text-fill: #9ca3af; " +
                "-fx-border-color: #374151; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;";
        clearBtn.setStyle(secStyle);
        exportBtn.setStyle(secStyle);
        importBtn.setStyle(secStyle);

        Label pokLabel = new Label("Pokémon:");
        pokLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
        Label yearLabel = new Label("Year:");
        yearLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");

        HBox addBtnSlot = new HBox();
        addBtnSlot.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(10,
                pokLabel, pokemonCombo, yearLabel, yearSpinner,
                loadBtn, clearBtn, addBtn, exportBtn, importBtn,
                spacer, addBtnSlot);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(8, 16, 8, 16));
        controls.setStyle("-fx-background-color: #111827;" +
                "-fx-border-color: transparent transparent #2a2d3a transparent; " +
                "-fx-border-width: 0 0 1 0;");

        root.setTop(new VBox(header, controls));

        //tabpane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (int m = 1; m <= 12; m++) {
            String n = Month.of(m).name();
            tabPane.getTabs().add(new Tab(n.charAt(0) +
                    n.substring(1).toLowerCase()));
        }
        Tab yearTab = new Tab("Year");
        tabPane.getTabs().add(yearTab);
        root.setCenter(tabPane);

        //reload
        final Runnable[] reloadCurrentTab = {null};
        reloadCurrentTab[0] = () -> {
            String filter = pokemonCombo.getEditor().getText();
            int year = yearSpinner.getValue();
            int idx = tabPane.getSelectionModel().getSelectedIndex();
            ObservableList<PokemonEntry> entries = idx < 12
                    ? FXCollections.observableArrayList(EntryService.getInstance().getByPokemonFilterMonth(filter, year, idx + 1))
                    : FXCollections.observableArrayList(EntryService.getInstance().getByPokemonFilterYear(filter, year));
            tabPane.getTabs().get(idx).setContent(PeriodPanel.create(entries, reloadCurrentTab[0], addBtnSlot));
        };

        boolean[] tabDirty = new boolean[13]; // true = behöver laddas om

        Runnable reloadAllTabs = () -> {
            String filter = pokemonCombo.getEditor().getText();
            int year = yearSpinner.getValue();

            java.util.Arrays.fill(tabDirty, true);

            int activeIdx = tabPane.getSelectionModel().getSelectedIndex();
            if (activeIdx < 12) {
                ObservableList<PokemonEntry> e = FXCollections.observableArrayList(
                        EntryService.getInstance().getByPokemonFilterMonth(filter, year, activeIdx + 1));
                addBtnSlot.getChildren().clear();
                tabPane.getTabs().get(activeIdx).setContent(PeriodPanel.create(e, reloadCurrentTab[0], addBtnSlot));
                tabDirty[activeIdx] = false;
            } else {
                ObservableList<PokemonEntry> ye = FXCollections.observableArrayList(
                        EntryService.getInstance().getByPokemonFilterYear(filter, year));
                addBtnSlot.getChildren().clear();
                yearTab.setContent(PeriodPanel.create(ye, reloadCurrentTab[0], addBtnSlot));
                tabDirty[12] = false;
            }

            // uppdatera pokemon lista
            Thread bgThread = new Thread(() -> {
                List<String> names = new ArrayList<>();
                names.add("");
                names.addAll(EntryService.getInstance().getDistinctPokemonNames());
                String cur = pokemonCombo.getEditor().getText();
                javafx.application.Platform.runLater(() -> {
                    pokemonCombo.setItems(FXCollections.observableArrayList(names));
                    pokemonCombo.getEditor().setText(cur);
                });
            });
            bgThread.setDaemon(true);
            bgThread.start();
        };

        loadBtn.setOnAction(e -> {
            reloadAllTabs.run();
            if (yearSpinner.getValue() != Year.now().getValue()) tabPane.getSelectionModel().select(12);
        });
        clearBtn.setOnAction(e -> {
            pokemonCombo.getEditor().setText("");
            pokemonCombo.setValue(null);
            reloadAllTabs.run();
        });
        pokemonCombo.getEditor().setOnAction(e -> reloadAllTabs.run());
        pokemonCombo.setOnAction(e -> {
            if (pokemonCombo.getValue() != null) reloadAllTabs.run();
        });
        addBtn.setOnAction(e -> showAddEntryWindow(reloadAllTabs, appRoot));

        exportBtn.setOnAction(e -> {
            try {
                TableView<PokemonEntry> table = findTableInSelectedTab(tabPane);
                if (table == null) return;
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
                fc.setInitialFileName("pokemon_entries.xlsx");
                java.io.File file = fc.showSaveDialog(root.getScene().getWindow());
                if (file == null) return;
                ExcelService.getInstance().exportEntries(file.toPath(), new ArrayList<>(table.getItems()));
                new Alert(Alert.AlertType.INFORMATION, "Exported " + table.getItems().size() + " rows.").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        });

        importBtn.setOnAction(e -> {
            try {
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
                java.io.File file = fc.showOpenDialog(root.getScene().getWindow());
                if (file == null) return;
                ExcelService.ImportResult res = ExcelService.getInstance().importEntries(file.toPath());
                new Alert(Alert.AlertType.INFORMATION, "Inserted: " + res.inserted() + "\nSkipped: " + res.skipped()).show();
                reloadAllTabs.run();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        });

        //byt tab
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> {
            int idx = n.intValue();
            if (idx >= 0 && idx < tabDirty.length && tabDirty[idx]) {
                addBtnSlot.getChildren().clear();
                reloadCurrentTab[0].run();
                tabDirty[idx] = false;
            } else {
                addBtnSlot.getChildren().clear();
                reloadCurrentTab[0].run();
            }
        });

        tabPane.getSelectionModel().select(java.time.LocalDate.now().getMonthValue() - 1);
        reloadAllTabs.run();

        return root;
    }

    static void showAddEntryWindow(Runnable onSaved, BorderPane appRoot) {
        Stage stage = new Stage();
        stage.setTitle("Add New Entry");
        stage.initModality(Modality.APPLICATION_MODAL);
        BorderPane content = new BorderPane(AddEntryView.create(onSaved));
        content.setStyle("-fx-background-color: #111827;");
        Scene scene = new Scene(content, 480, 620);
        if (appRoot.getScene() != null) scene.getStylesheets().addAll(appRoot.getScene().getStylesheets());
        stage.setScene(scene);
        stage.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private static TableView<PokemonEntry> findTableInSelectedTab(TabPane tabPane) {
        Tab t = tabPane.getSelectionModel().getSelectedItem();
        return t == null ? null : (TableView<PokemonEntry>) findFirstTable(t.getContent());
    }

    private static TableView<?> findFirstTable(Node node) {
        if (node == null) return null;
        if (node instanceof TableView<?> tv) return tv;
        if (node instanceof SplitPane sp) {
            for (Node n : sp.getItems()) {
                var f = findFirstTable(n);
                if (f != null) return f;
            }
        }
        if (node instanceof javafx.scene.Parent p) {
            for (Node c : p.getChildrenUnmodifiable()) {
                var f = findFirstTable(c);
                if (f != null) return f;
            }
        }
        return null;
    }
}
