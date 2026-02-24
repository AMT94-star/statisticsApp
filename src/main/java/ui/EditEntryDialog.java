package ui;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.PokemonEntry;
import util.TextNormalizer;

public class EditEntryDialog {

    //returnerar true om användaren tryckte save
    public static boolean show(PokemonEntry entry) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Entry");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField pokemonField = new TextField(safe(entry.getPokemonName()));
        TextField cpField = new TextField(String.valueOf(entry.getCp()));

        CheckBox caughtBox = new CheckBox();
        caughtBox.setSelected(entry.isCaught());

        CheckBox shinyBox = new CheckBox();
        shinyBox.setSelected(entry.isShiny());

        ComboBox<String> weatherBox = new ComboBox<>();
        weatherBox.setItems(FXCollections.observableArrayList(
                "Sunny/Clear",
                "Fog",
                "Cloudy",
                "Rainy",
                "Snow",
                "Partly Cloudy",
                "Windy"
        ));
        weatherBox.setValue(entry.getWeather());
        TextField locationField = new TextField(safe(entry.getLocation()));
        TextField tagField = new TextField(safe(entry.getTag()));

        grid.addRow(0, new Label("Pokémon"), pokemonField);
        grid.addRow(1, new Label("CP"), cpField);
        grid.addRow(2, new Label("Caught"), caughtBox);
        grid.addRow(3, new Label("Shiny"), shinyBox);
        grid.addRow(4, new Label("Weather"), weatherBox);
        grid.addRow(5, new Label("Location"), locationField);
        grid.addRow(6, new Label("Tag"), tagField);

        dialog.getDialogPane().setContent(grid);

        //visa dialogen
        var result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != saveBtn) return false;

        //save
        try {
            String pokemon = pokemonField.getText().trim();
            if (pokemon.isEmpty()) throw new IllegalArgumentException("Pokémon name can't be empty");

            int cp = Integer.parseInt(cpField.getText().trim());
            if (cp < 0) throw new IllegalArgumentException("CP must be >= 0");

            entry.setPokemonName(TextNormalizer.smartTitle(pokemonField.getText()));
            entry.setCp(cp);
            entry.setCaught(caughtBox.isSelected());
            entry.setShiny(shinyBox.isSelected());
            entry.setWeather(weatherBox.getValue());
            entry.setLocation(locationField.getText().trim());
            entry.setTag(TextNormalizer.smartTitle(tagField.getText().trim()));

            return true;

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid values. Check Pokémon name and CP.").show();
            return false;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}