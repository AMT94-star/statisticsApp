package ui;

import database.EntryDAO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.PokemonEntry;

public class AddEntryView {

    public static ScrollPane create() {

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;

        DatePicker datePicker = new DatePicker();
        TextField timeField = new TextField();
        TextField pokemonField = new TextField();
        TextField cpField = new TextField();

        CheckBox caughtBox = new CheckBox();
        CheckBox shinyBox = new CheckBox();

        ComboBox<String> weatherBox = new ComboBox<>();
        weatherBox.getItems().addAll(
                "Sunny/Clear", "Fog", "Cloudy", "Rainy", "Snow", "Partly Cloudy", "Windy"
        );

        TextField parkField = new TextField();
        TextField locationField = new TextField();
        TextField tagField = new TextField();
        TextField eventField = new TextField();

        CheckBox incenseBox = new CheckBox();
        TextField incenseDurationField = new TextField();

        // Lägg till rader i formuläret
        row = addRow(grid, row, "Date:", datePicker);
        row = addRow(grid, row, "Time:", timeField);
        row = addRow(grid, row, "Pokémon:", pokemonField);
        row = addRow(grid, row, "CP:", cpField);
        row = addRow(grid, row, "Caught:", caughtBox);
        row = addRow(grid, row, "Shiny:", shinyBox);
        row = addRow(grid, row, "Weather:", weatherBox);
        row = addRow(grid, row, "Park:", parkField);
        row = addRow(grid, row, "Location:", locationField);
        row = addRow(grid, row, "Tag:", tagField);
        row = addRow(grid, row, "Event:", eventField);
        row = addRow(grid, row, "Incense:", incenseBox);
        row = addRow(grid, row, "Incense Duration:", incenseDurationField);

        Button saveBtn = new Button("Save Entry");
        grid.add(saveBtn, 1, row);

        // 💾 SAVE TILL DATABASEN
        saveBtn.setOnAction(e -> {

            try {
                if (datePicker.getValue() == null ||
                        pokemonField.getText().isEmpty() ||
                        cpField.getText().isEmpty() ||
                        weatherBox.getValue() == null) {

                    throw new Exception("Missing required fields");
                }

                var date = datePicker.getValue();
                String day = date.getDayOfWeek().toString();
                String time = timeField.getText();
                String pokemon = pokemonField.getText();
                int cp = Integer.parseInt(cpField.getText());

                boolean caught = caughtBox.isSelected();
                boolean shiny = shinyBox.isSelected();

                String weather = weatherBox.getValue();
                String park = parkField.getText();
                String location = locationField.getText();
                String tag = tagField.getText();
                String event = eventField.getText();

                boolean incense = incenseBox.isSelected();
                int incenseDuration = incenseDurationField.getText().isEmpty()
                        ? 0 : Integer.parseInt(incenseDurationField.getText());

                PokemonEntry entry = new PokemonEntry(
                        date, day, time, pokemon, cp, caught, shiny,
                        weather, park, location, tag, event, incense, incenseDuration
                );

                EntryDAO.insertEntry(entry);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Entry saved!");
                alert.show();

                System.out.println("ENTRY SAVED!");

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please fill required fields (Date, Pokémon, CP, Weather).");
                alert.show();
                ex.printStackTrace();
            }
        });

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private static int addRow(GridPane grid, int row, String label, Control field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        return row + 1;
    }
}