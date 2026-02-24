package ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.IntegerStringConverter;
import model.PokemonEntry;
import service.EntryService;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.control.ContentDisplay.TEXT_ONLY;
import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;
import static javafx.scene.input.KeyCode.ESCAPE;

public class Tables {

    private static final List<String> weatherOptions = List.of(
            "Sunny/Clear", "Fog", "Cloudy", "Rainy", "Snow", "Partly Cloudy", "Windy"
    );

    private static final Map<PokemonEntry, SimpleBooleanProperty> caughtProps = new WeakHashMap<>();
    private static final Map<PokemonEntry, SimpleBooleanProperty> shinyProps = new WeakHashMap<>();
    private static final Map<PokemonEntry, SimpleBooleanProperty> incenseProps = new WeakHashMap<>();

    public static TableView<PokemonEntry> makeEntryTableForFilteredList(
            FilteredList<PokemonEntry> filtered,
            Runnable reloadCurrentTab
    ) {
        TableView<PokemonEntry> table = new TableView<>(filtered);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        // fixa tabellhöjd för att undvika onödig layout
        table.setFixedCellSize(36);

        //datum
        TableColumn<PokemonEntry, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cd -> {
            var d = cd.getValue().getDate();
            return new SimpleStringProperty(d == null ? "" : d.toString());
        });
        colDate.setPrefWidth(95);

        //dag
        TableColumn<PokemonEntry, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        colDay.setPrefWidth(85);

        //tid
        TableColumn<PokemonEntry, String> colTime = editableStringColumn(
                "Time", "time", reloadCurrentTab);
        colTime.setPrefWidth(75);

        //pokie
        TableColumn<PokemonEntry, String> colPokemon = editableStringColumn(
                "Pokémon", "pokemonName", reloadCurrentTab);
        colPokemon.setPrefWidth(100);

        //cp
        TableColumn<PokemonEntry, Integer> colCP = new TableColumn<>("CP");
        colCP.setCellValueFactory(new PropertyValueFactory<>("cp"));
        colCP.setEditable(true);
        colCP.setCellFactory(forTableColumn(
                new IntegerStringConverter()));
        colCP.setOnEditCommit(ev -> {
            ev.getRowValue().setCp(ev.getNewValue() ==
                    null ? 0 : ev.getNewValue());
            EntryService.getInstance().update(ev.getRowValue());
        });
        colCP.setPrefWidth(60);

        //caught checkbox
        TableColumn<PokemonEntry, Boolean> colCaught = new TableColumn<>("Caught");
        colCaught.setCellValueFactory(cell -> {
            PokemonEntry e = cell.getValue();
            return caughtProps.computeIfAbsent(e, entry -> {
                SimpleBooleanProperty p = new SimpleBooleanProperty(entry.isCaught());
                p.addListener((obs, oldV, newV) -> {
                    entry.setCaught(Boolean.TRUE.equals(newV));
                    EntryService.getInstance().update(entry);
                });
                return p;
            });
        });
        colCaught.setCellFactory(CheckBoxTableCell.forTableColumn(colCaught));
        colCaught.setEditable(true);
        colCaught.setPrefWidth(60);

        //shiny checkbox
        TableColumn<PokemonEntry, Boolean> colShiny = new TableColumn<>("Shiny");
        colShiny.setCellValueFactory(cell -> {
            PokemonEntry e = cell.getValue();
            return shinyProps.computeIfAbsent(e, entry -> {
                SimpleBooleanProperty p = new SimpleBooleanProperty(entry.isShiny());
                p.addListener((obs, oldV, newV) -> {
                    entry.setShiny(Boolean.TRUE.equals(newV));
                    EntryService.getInstance().update(entry);
                });
                return p;
            });
        });
        colShiny.setCellFactory(CheckBoxTableCell.forTableColumn(colShiny));
        colShiny.setEditable(true);
        colShiny.setPrefWidth(55);

        //weather dropdown
        TableColumn<PokemonEntry, String> colWeather = new TableColumn<>("Weather");
        colWeather.setCellValueFactory(new PropertyValueFactory<>("weather"));
        colWeather.setEditable(true);
        colWeather.setCellFactory(ComboBoxTableCell.forTableColumn(
                observableArrayList(weatherOptions)
        ));
        colWeather.setOnEditCommit(ev -> {
            ev.getRowValue().setWeather(ev.getNewValue());
            EntryService.getInstance().update(ev.getRowValue());
        });
        colWeather.setPrefWidth(105);

        //park
        TableColumn<PokemonEntry, String> colPark = editableStringColumn(
                "Park", "park", reloadCurrentTab);
        colPark.setPrefWidth(90);

        //location
        TableColumn<PokemonEntry, String> colLocation = editableStringColumn(
                "Location", "location", reloadCurrentTab);
        colLocation.setPrefWidth(90);
        colLocation.setMaxWidth(130);

        //tag
        TableColumn<PokemonEntry, String> colTag = new TableColumn<>("Tag");
        colTag.setCellValueFactory(new PropertyValueFactory<>("tag"));
        colTag.setEditable(true);
        colTag.setCellFactory(col -> new TagAutoCompleteCell());
        colTag.setOnEditCommit(ev -> {
            ev.getRowValue().setTag(ev.getNewValue());
            EntryService.getInstance().update(ev.getRowValue());
        });
        colTag.setPrefWidth(90);

        //event
        TableColumn<PokemonEntry, String> colEvent = editableStringColumn("Event", "event", reloadCurrentTab);
        colEvent.setPrefWidth(90);

        //incense
        TableColumn<PokemonEntry, Boolean> colIncense = new TableColumn<>("Incense");
        colIncense.setCellValueFactory(cell -> {
            PokemonEntry e = cell.getValue();
            return incenseProps.computeIfAbsent(e, entry -> {
                SimpleBooleanProperty p = new SimpleBooleanProperty(entry.isIncense());
                p.addListener((obs, oldV, newV) -> {
                    entry.setIncense(Boolean.TRUE.equals(newV));
                    EntryService.getInstance().update(entry);
                });
                return p;
            });
        });
        colIncense.setCellFactory(CheckBoxTableCell.forTableColumn(colIncense));
        colIncense.setEditable(true);
        colIncense.setPrefWidth(65);

        //incense längd
        TableColumn<PokemonEntry, Integer> colIncDur = new TableColumn<>("Inc.Dur");
        colIncDur.setCellValueFactory(new PropertyValueFactory<>("incenseDuration"));
        colIncDur.setEditable(true);
        colIncDur.setCellFactory(forTableColumn(new IntegerStringConverter()));
        colIncDur.setOnEditCommit(ev -> {
            ev.getRowValue().setIncenseDuration(ev.getNewValue() == null ? 0 : ev.getNewValue());
            EntryService.getInstance().update(ev.getRowValue());
        });
        colIncDur.setPrefWidth(65);

        //delete
        TableColumn<PokemonEntry, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(80);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.getStyleClass().add("destructive");
                deleteBtn.setOnAction(e -> {
                    PokemonEntry entry = getTableView().getItems().get(getIndex());
                    EntryService.getInstance().deleteById(entry.getId());
                    // Rensa cachade properties för borttagen entry
                    caughtProps.remove(entry);
                    shinyProps.remove(entry);
                    incenseProps.remove(entry);
                    reloadCurrentTab.run();
                    ButtonType undo = new ButtonType("Undo", OK_DONE);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Entry deleted.", undo, ButtonType.CLOSE);
                    alert.setHeaderText(null);
                    alert.showAndWait().ifPresent(bt -> {
                        if (bt == undo) {
                            EntryService.getInstance().undoInsertWithId(entry);
                            reloadCurrentTab.run();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(
                colDate, colDay, colTime, colPokemon, colCP,
                colCaught, colShiny,
                colWeather, colPark, colLocation,
                colTag, colEvent,
                colIncense, colIncDur,
                colActions
        );

        return table;
    }

    private static TableColumn<PokemonEntry, String> editableStringColumn(
            String title, String property, Runnable reloadCurrentTab
    ) {
        TableColumn<PokemonEntry, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setEditable(true);
        col.setCellFactory(forTableColumn());
        col.setOnEditCommit(ev -> {
            PokemonEntry e = ev.getRowValue();
            String v = ev.getNewValue();
            switch (property) {
                case "time" -> e.setTime(v);
                case "pokemonName" -> e.setPokemonName(v);
                case "park" -> e.setPark(v);
                case "location" -> e.setLocation(v);
                case "event" -> e.setEvent(v);
            }
            EntryService.getInstance().update(e);
            if (property.equals("pokemonName")) reloadCurrentTab.run();
        });
        return col;
    }

    private static class TagAutoCompleteCell extends TableCell<PokemonEntry, String> {
        private TextField textField;

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                setContentDisplay(GRAPHIC_ONLY);
                textField.requestFocus();
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
            setContentDisplay(TEXT_ONLY);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }
            if (isEditing()) {
                if (textField != null) textField.setText(item);
                setText(null);
                setGraphic(textField);
                setContentDisplay(GRAPHIC_ONLY);
            } else {
                setText(item);
                setGraphic(null);
                setContentDisplay(TEXT_ONLY);
            }
        }

        private void createTextField() {
            textField = new TextField(getItem());
            AutoComplete.bind(textField, EntryService.getInstance().getDistinctTags());
            textField.setOnAction(e -> commitEdit(textField.getText()));
            textField.setOnKeyPressed(e -> {
                if (e.getCode() == ESCAPE) cancelEdit();
            });
            textField.focusedProperty().addListener((obs, was, isNow) -> {
                if (!isNow && isEditing()) commitEdit(textField.getText());
            });
        }
    }
}
