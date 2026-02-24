package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class AppHeader {

    static boolean lightMode = false;

    public static HBox create(BorderPane appRoot, String titleText, String subtitleText) {

        Label title = new Label(titleText);
        title.getStyleClass().add("app-title");

        Button backBtn = new Button("← Main Menu");
        backBtn.getStyleClass().add("secondary");
        backBtn.setOnAction(e -> appRoot.setCenter(MainMenuView.create(appRoot)));

        StackPane toggle = buildToggle(lightMode);
        replaceAndBind(toggle, appRoot);

        HBox left = new HBox(10, title, backBtn, toggle);
        left.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(left);
        HBox.setHgrow(left, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 16, 8, 16));
        header.getStyleClass().add("top-bar");

        appRoot.centerProperty().addListener((obs, oldCenter, newCenter) -> {
            if (newCenter != null && lightMode) {
                //small delay
                javafx.application.Platform.runLater(() -> applyTheme(appRoot, true));
            }
        });

        return header;
    }

    private static void replaceAndBind(StackPane toggle, BorderPane appRoot) {
        toggle.setOnMouseClicked(e -> {
            lightMode = !lightMode;
            StackPane rebuilt = buildToggle(lightMode);
            HBox parent = (HBox) toggle.getParent();
            if (parent != null) {
                int idx = parent.getChildren().indexOf(toggle);
                parent.getChildren().set(idx, rebuilt);
                replaceAndBind(rebuilt, appRoot);
            }
            applyTheme(appRoot, lightMode);
        });
    }

    static StackPane buildToggle(boolean on) {
        Rectangle pill = new Rectangle(46, 24);
        pill.setArcWidth(24);
        pill.setArcHeight(24);
        pill.setFill(on ? Color.web("#3b82f6") : Color.web("#1F2937"));
        pill.setStroke(on ? Color.web("#60a5fa") : Color.web("#374151"));
        pill.setStrokeWidth(1.5);

        Circle knob = new Circle(10, Color.web("#e8eaf0"));
        DropShadow shadow = new DropShadow(3, 0, 1, Color.web("#00000088"));
        knob.setEffect(shadow);

        Label sun = new Label("☀");
        sun.setStyle("-fx-font-size: 9px; -fx-text-fill: " + (on ? "#fbbf24" : "#6b7280") + "; -fx-padding: 0;");

        StackPane knobStack = new StackPane(knob, sun);
        knobStack.setPrefSize(20, 20);
        knobStack.setMaxSize(20, 20);

        StackPane container = new StackPane();
        container.setPrefSize(46, 24);
        container.setMaxSize(46, 24);
        container.setMinSize(46, 24);
        container.getChildren().add(pill);

        if (on) {
            StackPane.setAlignment(knobStack, Pos.CENTER_RIGHT);
            knobStack.setTranslateX(-3);
        } else {
            StackPane.setAlignment(knobStack, Pos.CENTER_LEFT);
            knobStack.setTranslateX(3);
        }
        container.getChildren().add(knobStack);
        container.setStyle("-fx-cursor: hand;");
        return container;
    }

    static void applyTheme(BorderPane appRoot, boolean light) {
        if (appRoot.getScene() == null) return;
        if (light) {
            appRoot.getScene().getRoot().getStyleClass().remove("light-mode");
            appRoot.getScene().getRoot().getStyleClass().add("light-mode");
        } else {
            appRoot.getScene().getRoot().getStyleClass().remove("light-mode");
        }
        updateStyles(appRoot.getScene().getRoot(), light);
    }

    private static void updateStyles(javafx.scene.Node node, boolean light) {
        if (node == null) return;
        if (node instanceof Region r) {
            String s = r.getStyle();
            if (s != null && !s.isBlank()) {
                if (light) {
                    s = s.replace("#111827", "#f1f5f9")
                            .replace("#0f1117", "#f1f5f9")
                            .replace("#0d1117", "#f1f5f9")
                            .replace("#161b27", "#ffffff")
                            .replace("#12151e", "#e2e8f0")
                            .replace("#1a2035", "#ffffff")
                            .replace("#1F2937", "#ffffff")
                            .replace("#1f2937", "#ffffff")
                            .replace("#2a2d3a", "#cbd5e1")
                            .replace("#1e2235", "#e2e8f0")
                            .replace("#374151", "#94a3b8");
                } else {
                    s = s.replace("#f1f5f9", "#111827")
                            .replace("#e2e8f0", "#12151e")
                            .replace("#ffffff", "#1F2937")
                            .replace("#cbd5e1", "#2a2d3a")
                            .replace("#94a3b8", "#374151");
                }
                r.setStyle(s);
            }
        }
        if (node instanceof javafx.scene.Parent p) {
            for (javafx.scene.Node c : p.getChildrenUnmodifiable()) {
                updateStyles(c, light);
            }
        }
    }
}
