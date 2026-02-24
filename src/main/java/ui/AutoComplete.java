package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

public class AutoComplete {

    public static void bind(TextField field, List<String> suggestionsSource) {

        ObservableList<String> source = FXCollections.observableArrayList(suggestionsSource);
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);

        Runnable refresh = () -> {
            String text = field.getText();
            if (text == null) text = "";
            String query = text.trim().toLowerCase();

            if (query.isEmpty()) {
                menu.hide();
                return;
            }

            List<String> matches = source.stream()
                    .filter(s -> s != null && s.toLowerCase().contains(query)) // case insensitive
                    .limit(8)
                    .collect(Collectors.toList());

            if (matches.isEmpty()) {
                menu.hide();
                return;
            }

            menu.getItems().clear();
            for (String m : matches) {
                MenuItem item = new MenuItem(m);
                item.setOnAction(e -> {
                    field.setText(m);
                    field.positionCaret(m.length());
                    menu.hide();
                });
                menu.getItems().add(item);
            }

            if (!menu.isShowing()) {
                menu.show(field, Side.BOTTOM, 0, 0);
            }
        };

        //uppdatera när man skriver
        field.textProperty().addListener((obs, oldV, newV) -> refresh.run());

        //stäng när fältet tappar fokus
        field.focusedProperty().addListener((obs, was, is) -> {
            if (!is) menu.hide();
        });

        //esc stänger menyn
        field.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> menu.hide();
            }
        });
    }
}