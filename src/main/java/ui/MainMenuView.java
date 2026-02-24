package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainMenuView {

    public static VBox create(BorderPane root) {

        VBox layout = new VBox(14);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20, 0, 20, 0));
        layout.setStyle("-fx-background-color: #111827;");

        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);

        //username on menu
        String username = UsernameSetupView.getSavedUsername();
        Label name = new Label(username != null ? username + "'s" : "");
        name.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #e8eaf0;");

        Label title = new Label("Pokémon Statistics");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #e8eaf0;");

        double btnWidth = 320;

        Button addEntryBtn = new Button("Add New Entry");
        Button searchPokemonBtn = new Button("Search for Pokémon Statistics");
        Button searchTagBtn = new Button("Search for Tagged Statistics");
        Button overallStatsBtn = new Button("Overall Statistics");

        String normalStyle =
                "-fx-background-color: #1F2937; -fx-text-fill: #c9ccd6;" +
                        "-fx-border-color: #2a2d3a; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px;" +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 10 20 10 20;";
        String hoverStyle =
                "-fx-background-color: #273449; " +
                        "-fx-text-fill: #e8eaf0;" +
                        "-fx-border-color: #3b82f6; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px;" +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 10 20 10 20;";

        for (Button btn : new Button[]{addEntryBtn, searchPokemonBtn, searchTagBtn, overallStatsBtn}) {
            btn.setPrefWidth(btnWidth);
            btn.setPrefHeight(44);
            btn.setStyle(normalStyle);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        }

        addEntryBtn.setOnAction(e -> root.setCenter(AddEntryView.create(root)));
        searchPokemonBtn.setOnAction(e -> root.setCenter(SearchPokemonDashboardView.create(root)));
        searchTagBtn.setOnAction(e -> root.setCenter(SearchTaggedDashboardView.create(root)));
        overallStatsBtn.setOnAction(e -> root.setCenter(OverallStatsView.create(root)));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label copyright = new Label(
                "All rights belong to The Pokémon Company International. " +
                        "This application is fan-made and does not claim ownership over any Pokémon-related images or trademarks."
        );
        copyright.setStyle("-fx-font-size: 10px; -fx-text-fill: #374151;");
        copyright.setWrapText(true);

        VBox legalBox = new VBox(3, copyright);
        legalBox.setMaxWidth(btnWidth + btnWidth + btnWidth);
        legalBox.setAlignment(Pos.BOTTOM_CENTER);

        layout.getChildren().addAll(topSpacer, name, title, addEntryBtn, searchPokemonBtn, searchTagBtn, overallStatsBtn, spacer, legalBox);
        return layout;
    }
}
