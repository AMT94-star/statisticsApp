package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.PokemonEntry;
import service.EntryService;
import service.ExcelService;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchTaggedDashboardView {

    public static BorderPane create(BorderPane appRoot) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #111827;");

        HBox header = AppHeader.create(appRoot, "Tagged Statistics", "");

        ComboBox<String> tagCombo = new ComboBox<>();
        tagCombo.setEditable(true);
        tagCombo.setPromptText("Search or pick tag...");
        tagCombo.setPrefWidth(230);
        tagCombo.setStyle("-fx-background-color: #111827; -fx-text-fill: #e8eaf0;");

        Spinner<Integer> yearSpinner = new Spinner<>(2000, 2100, Year.now().getValue());
        yearSpinner.setEditable(true);
        yearSpinner.setPrefWidth(100);
        yearSpinner.setMinWidth(100);
        yearSpinner.setMaxWidth(100);
        yearSpinner.setStyle("-fx-background-color: #111827;");

        Button loadBtn = new Button("Load");
        Button clearBtn = new Button("Clear");
        Button addBtn = new Button("Add Entry");
        Button exportBtn = new Button("Export Excel");
        Button importBtn = new Button("Import Excel");

        loadBtn.getStyleClass().add("primary");
        addBtn.getStyleClass().add("primary");

        String secStyle = "-fx-background-color: #111827; -fx-text-fill: #9ca3af; " +
                "-fx-border-color: #374151; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;";
        clearBtn.setStyle(secStyle);
        exportBtn.setStyle(secStyle);
        importBtn.setStyle(secStyle);

        Label tagLabel = new Label("Tag:");
        Label yearLabel = new Label("Year:");
        tagLabel.setStyle("-fx-text-fill: #9ca3af;");
        yearLabel.setStyle("-fx-text-fill: #9ca3af;");

        HBox addBtnSlot = new HBox();
        addBtnSlot.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(10,
                tagLabel, tagCombo, yearLabel, yearSpinner,
                loadBtn, clearBtn, addBtn, exportBtn, importBtn,
                spacer, addBtnSlot
        );
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(8, 16, 8, 16));
        controls.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-border-color: transparent transparent #2a2d3a transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        root.setTop(new VBox(header, controls));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #111827;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        for (int m = 1; m <= 12; m++) {
            String name = Month.of(m).name();
            Tab t = new Tab(name.charAt(0) + name.substring(1).toLowerCase());
            t.setClosable(false);
            tabPane.getTabs().add(t);
        }
        Tab yearTab = new Tab("Year");
        yearTab.setClosable(false);
        tabPane.getTabs().add(yearTab);

        root.setCenter(tabPane);

        final Runnable[] reloadCurrentTab = new Runnable[1];

        reloadCurrentTab[0] = () -> {
            String tag = tagCombo.getEditor().getText();
            int year = yearSpinner.getValue();
            int idx = tabPane.getSelectionModel().getSelectedIndex();

            ObservableList<PokemonEntry> entries;
            if (idx < 12) {
                entries = FXCollections.observableArrayList(
                        EntryService.getInstance().getByTagMonth(tag, year, idx + 1)
                );
            } else {
                entries = FXCollections.observableArrayList(
                        EntryService.getInstance().getByTagYear(tag, year)
                );
            }
            addBtnSlot.getChildren().clear();
            tabPane.getTabs().get(idx).setContent(TagPeriodPanel.create(entries, reloadCurrentTab[0], addBtnSlot));
        };

        boolean[] tabDirty = new boolean[13];

        Runnable reloadAllTabs = () -> {
            String tag = tagCombo.getEditor().getText();
            int year = yearSpinner.getValue();

            Arrays.fill(tabDirty, true);

            int activeIdx = tabPane.getSelectionModel().getSelectedIndex();
            if (activeIdx < 12) {
                ObservableList<PokemonEntry> entries = FXCollections.observableArrayList(
                        EntryService.getInstance().getByTagMonth(tag, year, activeIdx + 1));
                addBtnSlot.getChildren().clear();
                tabPane.getTabs().get(activeIdx).setContent(TagPeriodPanel.create(entries, reloadCurrentTab[0], addBtnSlot));
                tabDirty[activeIdx] = false;
            } else {
                ObservableList<PokemonEntry> ye = FXCollections.observableArrayList(
                        EntryService.getInstance().getByTagYear(tag, year));
                addBtnSlot.getChildren().clear();
                yearTab.setContent(TagPeriodPanel.create(ye, reloadCurrentTab[0], addBtnSlot));
                tabDirty[12] = false;
            }

            Thread bgThread = new Thread(() -> {
                List<String> tags = new ArrayList<>();
                tags.add("");
                tags.addAll(EntryService.getInstance().getDistinctTags());
                String cur = tagCombo.getEditor().getText();
                javafx.application.Platform.runLater(() -> {
                    tagCombo.setItems(FXCollections.observableArrayList(tags));
                    tagCombo.getEditor().setText(cur);
                });
            });
            bgThread.setDaemon(true);
            bgThread.start();
        };

        loadBtn.setOnAction(e -> {
            int selectedYear = yearSpinner.getValue();
            int currentYear = Year.now().getValue();
            reloadAllTabs.run();
            if (selectedYear != currentYear) {
                tabPane.getSelectionModel().select(12);
            }
        });

        clearBtn.setOnAction(e -> {
            tagCombo.getEditor().setText("");
            tagCombo.setValue(null);
            tagCombo.getSelectionModel().clearSelection();
            reloadAllTabs.run();
        });

        tagCombo.getEditor().setOnAction(e -> reloadAllTabs.run());
        tagCombo.setOnAction(e -> {
            if (tagCombo.getValue() != null) reloadAllTabs.run();
        });

        addBtn.setOnAction(e -> SearchPokemonDashboardView.showAddEntryWindow(reloadAllTabs, appRoot));

        exportBtn.setOnAction(e -> {
            try {
                TableView<PokemonEntry> table = findTableInSelectedTab(tabPane);
                if (table == null) {
                    new Alert(Alert.AlertType.WARNING, "No table found.").show();
                    return;
                }
                List<PokemonEntry> toExport = new ArrayList<>(table.getItems());
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.setTitle("Export to Excel");
                fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
                fc.setInitialFileName("tagged_entries.xlsx");
                java.io.File file = fc.showSaveDialog(root.getScene().getWindow());
                if (file == null) return;
                ExcelService.getInstance().exportEntries(file.toPath(), toExport);
                new Alert(Alert.AlertType.INFORMATION, "Exported " + toExport.size() + " rows.").show();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
            }
        });

        importBtn.setOnAction(e -> {
            try {
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.setTitle("Import from Excel");
                fc.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter(
                                "Excel (*.xlsx)", "*.xlsx"));
                java.io.File file = fc.showOpenDialog(root.getScene().getWindow());

                if (file == null) return;
                ExcelService.ImportResult res = ExcelService.getInstance().importEntries(file.toPath());
                String msg = "Inserted: " + res.inserted() + "\nSkipped: " + res.skipped();
                if (!res.errors().isEmpty())
                    msg += "\n\nErrors:\n" + String.join("\n", res.errors().stream().limit(5).toList());
                new Alert(Alert.AlertType.INFORMATION, msg).show();
                reloadAllTabs.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Import failed: " + ex.getMessage()).show();
            }
        });

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

        int currentMonth = LocalDate.now().getMonthValue();
        tabPane.getSelectionModel().select(currentMonth - 1);
        reloadAllTabs.run();

        return root;
    }

    @SuppressWarnings("unchecked")
    private static TableView<PokemonEntry> findTableInSelectedTab(TabPane tabPane) {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        if (selected == null) return null;
        return (TableView<PokemonEntry>) findFirstTableView(selected.getContent());
    }

    private static TableView<?> findFirstTableView(Node node) {
        if (node == null) return null;
        if (node instanceof TableView<?> tv) return tv;
        if (node instanceof SplitPane sp) {
            for (Node n : sp.getItems()) {
                TableView<?> f = findFirstTableView(n);
                if (f != null) return f;
            }
        }
        if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                TableView<?> f = findFirstTableView(child);
                if (f != null) return f;
            }
        }
        return null;
    }
}
