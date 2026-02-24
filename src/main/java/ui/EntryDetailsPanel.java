package ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.PokemonEntry;
import service.EntryService;

public class EntryDetailsPanel {

    public static Node create(TableView<PokemonEntry> table, Runnable reloadCurrentTab) {

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        Label title = new Label("Selected Entry");
        title.getStyleClass().add("section-title");

        TextField time = new TextField();
        TextField pokemon = new TextField();
        TextField cp = new TextField();

        ComboBox<String> weather = new ComboBox<>();
        weather.getItems().addAll("Sunny/Clear", "Fog", "Cloudy", "Rainy", "Snow", "Partly Cloudy", "Windy");

        TextField park = new TextField();
        TextField location = new TextField();
        TextField tag = new TextField();
        TextField event = new TextField();

        CheckBox caught = new CheckBox();
        CheckBox shiny = new CheckBox();
        CheckBox incense = new CheckBox();
        TextField incenseDuration = new TextField();

        int r = 0;
        r = row(grid, r, "Time", time);
        r = row(grid, r, "Pokémon", pokemon);
        r = row(grid, r, "CP", cp);
        r = row(grid, r, "Weather", weather);
        r = row(grid, r, "Park", park);
        r = row(grid, r, "Location", location);
        r = row(grid, r, "Tag", tag);
        r = row(grid, r, "Event", event);
        r = row(grid, r, "Caught", caught);
        r = row(grid, r, "Shiny", shiny);
        r = row(grid, r, "Incense", incense);
        r = row(grid, r, "Incense Duration", incenseDuration);

        Button save = new Button("Save Changes");
        save.getStyleClass().add("primary");

        Label hint = new Label("Click a row in the table to load it here.");
        hint.getStyleClass().add("muted");

        VBox box = new VBox(10, title, hint, grid, save);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(10));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            time.setText(nz(sel.getTime()));
            pokemon.setText(nz(sel.getPokemonName()));
            cp.setText(String.valueOf(sel.getCp()));
            weather.setValue(sel.getWeather());
            park.setText(nz(sel.getPark()));
            location.setText(nz(sel.getLocation()));
            tag.setText(nz(sel.getTag()));
            event.setText(nz(sel.getEvent()));
            caught.setSelected(sel.isCaught());
            shiny.setSelected(sel.isShiny());
            incense.setSelected(sel.isIncense());
            incenseDuration.setText(String.valueOf(sel.getIncenseDuration()));
        });

        save.setOnAction(e -> {
            PokemonEntry sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                new Alert(Alert.AlertType.WARNING, "Select a row first.").show();
                return;
            }
            sel.setTime(time.getText());
            sel.setPokemonName(pokemon.getText());
            sel.setCp(parseInt(cp.getText()));
            sel.setWeather(weather.getValue());
            sel.setPark(park.getText());
            sel.setLocation(location.getText());
            sel.setTag(tag.getText());
            sel.setEvent(event.getText());
            sel.setCaught(caught.isSelected());
            sel.setShiny(shiny.isSelected());
            sel.setIncense(incense.isSelected());
            sel.setIncenseDuration(parseInt(incenseDuration.getText()));

            EntryService.getInstance().update(sel);
            reloadCurrentTab.run();
        });

        return box;
    }

    private static int row(GridPane g, int r, String label, Control c) {
        g.add(new Label(label), 0, r);
        g.add(c, 1, r);
        return r + 1;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static int parseInt(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}