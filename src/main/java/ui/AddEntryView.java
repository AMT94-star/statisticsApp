package ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.PokemonEntry;
import service.EntryService;

import java.time.LocalDate;

public class AddEntryView {

    //används från huvudmenyn
    public static VBox create(BorderPane appRoot) {
        VBox page = new VBox(10);
        page.setPadding(new Insets(10));
        page.setStyle("-fx-background-color: #111827;");
        var header = AppHeader.create(appRoot, "Add New Entry", "Fill in the form and save.");
        ScrollPane form = createForm(null);
        page.getChildren().addAll(header, form);
        return page;
    }

    //popup variant
    public static ScrollPane create(Runnable onSaved) {
        return createForm(onSaved);
    }

    private static ScrollPane createForm(Runnable onSaved) {

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #111827;");

        int row = 0;

        DatePicker datePicker = new DatePicker(LocalDate.now());
        stylePicker(datePicker);

        TextField timeField = styledField();
        TextField pokemonField = styledField();
        TextField cpField = styledField();
        CheckBox caughtBox = styledCheck();
        CheckBox shinyBox = styledCheck();

        ComboBox<String> weatherBox = new ComboBox<>();
        weatherBox.getItems().addAll(
                "Sunny/Clear", "Fog", "Cloudy", "Rainy", "Snow", "Partly Cloudy", "Windy"
        );
        styleCombo(weatherBox);

        TextField parkField = styledField();
        TextField locationField = styledField();
        TextField tagField = styledField();
        TextField eventField = styledField();
        CheckBox incenseBox = styledCheck();
        TextField incenseDurationField = styledField();

        //autocomplete
        AutoComplete.bind(pokemonField, EntryService.getInstance().getDistinctPokemonNames());
        AutoComplete.bind(tagField, EntryService.getInstance().getDistinctTags());

        row = addRow(grid, row, "Date *", datePicker);
        row = addRow(grid, row, "Time", timeField);
        row = addRow(grid, row, "Pokémon *", pokemonField);
        row = addRow(grid, row, "CP", cpField);
        row = addRow(grid, row, "Caught", caughtBox);
        row = addRow(grid, row, "Shiny", shinyBox);
        row = addRow(grid, row, "Weather *", weatherBox);
        row = addRow(grid, row, "Park", parkField);
        row = addRow(grid, row, "Location", locationField);
        row = addRow(grid, row, "Tag", tagField);
        row = addRow(grid, row, "Event", eventField);
        row = addRow(grid, row, "Incense", incenseBox);
        row = addRow(grid, row, "Incense Duration", incenseDurationField);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        grid.add(errorLabel, 0, row, 2, 1);
        row++;

        Button saveBtn = new Button("💾  Save Entry");
        saveBtn.setStyle(
                "-fx-background-color: #1d4ed8; -fx-text-fill: #eff6ff; " +
                        "-fx-font-weight: 700; -fx-font-size: 13px; " +
                        "-fx-background-radius: 8px; -fx-padding: 8 24 8 24; -fx-cursor: hand;"
        );
        saveBtn.setPrefWidth(200);
        grid.add(saveBtn, 0, row, 2, 1);

        saveBtn.setOnAction(e -> {
            errorLabel.setText("");
            try {
                if (datePicker.getValue() == null) {
                    errorLabel.setText("Date is required.");
                    return;
                }
                String pokemonName = pokemonField.getText().trim();
                if (pokemonName.isEmpty()) {
                    errorLabel.setText("Pokémon name is required.");
                    return;
                }
                if (weatherBox.getValue() == null) {
                    errorLabel.setText("Weather is required.");
                    return;
                }

                LocalDate date = datePicker.getValue();
                String day = capitalize(date.getDayOfWeek().toString());
                String time = timeField.getText().trim();

                int cp = 0;
                if (!cpField.getText().trim().isEmpty()) cp = Integer.parseInt(cpField.getText().trim());

                int incenseDuration = 0;
                if (!incenseDurationField.getText().trim().isEmpty())
                    incenseDuration = Integer.parseInt(incenseDurationField.getText().trim());

                PokemonEntry entry = new PokemonEntry(
                        date, day, time, pokemonName, cp,
                        caughtBox.isSelected(), shinyBox.isSelected(),
                        weatherBox.getValue(),
                        parkField.getText().trim(), locationField.getText().trim(),
                        tagField.getText().trim(), eventField.getText().trim(),
                        incenseBox.isSelected(), incenseDuration
                );
                EntryService.getInstance().add(entry);

                //återställ formulär
                datePicker.setValue(LocalDate.now());
                timeField.clear();
                pokemonField.clear();
                cpField.clear();
                caughtBox.setSelected(false);
                shinyBox.setSelected(false);
                weatherBox.setValue(null);
                parkField.clear();
                locationField.clear();
                tagField.clear();
                eventField.clear();
                incenseBox.setSelected(false);
                incenseDurationField.clear();

                if (onSaved != null) {
                    onSaved.run();
                    ((javafx.stage.Stage) saveBtn.getScene().getWindow()).close();
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "Entry saved!").show();
                }

            } catch (NumberFormatException ex) {
                errorLabel.setText("CP and Incense Duration must be numbers.");
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Something went wrong: " + ex.getMessage());
            }
        });

        VBox wrapper = new VBox(grid);
        wrapper.setStyle("-fx-background-color: #111827;");

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #111827; -fx-background: #111827;");
        return scroll;
    }

    private static int addRow(GridPane grid, int row, String labelText, Control field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px; -fx-font-weight: 600;");
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
        return row + 1;
    }

    private static TextField styledField() {
        TextField f = new TextField();
        f.setStyle(
                "-fx-background-color: #1F2937; -fx-text-fill: #e8eaf0; " +
                        "-fx-border-color: #374151; -fx-border-radius: 6px; -fx-background-radius: 6px; " +
                        "-fx-prompt-text-fill: #4b5563; -fx-padding: 6 10 6 10;"
        );
        f.setPrefWidth(220);
        return f;
    }

    private static CheckBox styledCheck() {
        CheckBox cb = new CheckBox();
        cb.setStyle("-fx-text-fill: #e8eaf0;");
        return cb;
    }

    private static void styleCombo(ComboBox<String> cb) {
        cb.setStyle(
                "-fx-background-color: #1F2937; -fx-text-fill: #e8eaf0; " +
                        "-fx-border-color: #374151; -fx-border-radius: 6px; -fx-background-radius: 6px;"
        );
        cb.setPrefWidth(224);
    }

    private static void stylePicker(DatePicker dp) {
        dp.setStyle(
                "-fx-background-color: #1F2937; -fx-text-fill: #e8eaf0; " +
                        "-fx-border-color: #374151; -fx-border-radius: 6px; -fx-background-radius: 6px;"
        );
        dp.getEditor().setStyle(
                "-fx-background-color: #1F2937; -fx-text-fill: #e8eaf0; " +
                        "-fx-border-color: transparent; -fx-padding: 6 10 6 10;"
        );
        dp.setPrefWidth(224);
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
