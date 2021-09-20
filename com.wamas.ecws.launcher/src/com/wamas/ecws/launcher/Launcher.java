package com.wamas.ecws.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static final class Entry {

        String workspace;
        String eclipse;
        String icon;

        @Override
        public String toString() {
            return "Eclipse: " + eclipse + ", Workspace: " + workspace + ", Icon: " + icon;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Eclipse Workspace Launcher");
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        String config = System.getProperty("ecws.config", System.getProperty("user.home") + "/.ecwsrc");
        File cfg = new File(config);
        if (!cfg.isFile()) {
            System.err.println("No config found in " + cfg);
            return;
        }

        Properties p = new Properties();
        try (InputStream s = new FileInputStream(cfg)) {
            p.load(s);
        }

        Map<String, Entry> items = new TreeMap<>();
        for (Map.Entry<?, ?> entry : p.entrySet()) {
            String k = entry.getKey().toString();

            String name = null;
            int indexOfWs = k.indexOf("_workspace");
            if (indexOfWs != -1) {
                name = k.substring(0, indexOfWs);

                Entry e = items.get(name);
                if (e == null) {
                    e = new Entry();
                    items.put(name, e);
                }

                e.workspace = entry.getValue().toString();
                e.eclipse = p.getProperty(name + "_eclipse");
                e.icon = p.getProperty(name + "_icon");
            }
        }

        int maxCols = Integer.parseInt(p.getProperty("columns", "6"));

        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setHgap(5);
        pane.setPadding(new Insets(5));

        CheckBox clean = new CheckBox("Start with -clean");
        clean.setSelected(Boolean.parseBoolean(p.getProperty("clean", "false")));

        ArrayList<TextField> arrayJFRTextFields = new ArrayList<>();

        Tooltip toolTipJFR = new Tooltip("JFR = Java FlightRecorder");
        toolTipJFR.setShowDelay(Duration.seconds(0));
        toolTipJFR.setStyle("-fx-font-size: 15");

        CheckBox checkFlightRecorder = new CheckBox("Start with JFR");
        checkFlightRecorder.setTooltip(toolTipJFR);
        checkFlightRecorder.setSelected(false);

        CheckBox checkCustomFlightRecorder = new CheckBox("Custom JFR options");
        checkCustomFlightRecorder.setSelected(false);

        TextField flightRecorder = new TextField(p.getProperty("flightRecorder", "-XX:+FlightRecorder"));
        flightRecorder.setVisible(true);
        flightRecorder.setEditable(true);
        arrayJFRTextFields.add(flightRecorder);

        TextField startFlightRecording = new TextField(
                p.getProperty("startFlightRecording", "-XX:StartFlightRecording=dumponexit=true,filename=C:\\myrecording.jfr"));
        startFlightRecording.autosize();
        startFlightRecording.setVisible(true);
        startFlightRecording.setEditable(true);
        arrayJFRTextFields.add(startFlightRecording);

        TextField flightReorderOptions = new TextField(
                p.getProperty("flightReorderOptions", "-XX:FlightReorderOptions=stackdepth=1000"));
        flightReorderOptions.autosize();
        flightReorderOptions.setVisible(true);
        flightReorderOptions.setEditable(true);
        arrayJFRTextFields.add(flightReorderOptions);

        int requiredCols = items.size() <= maxCols ? items.size() : maxCols;
        int requiredRows = (items.size() + maxCols - 1) / maxCols;
        Scene scene = new Scene(pane, 5 + (requiredCols * 135), 30 + (requiredRows * 135));

        boolean fastToolTip = Boolean.parseBoolean(p.getProperty("fastToolTip", "false"));

        checkCustomFlightRecorder.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (checkCustomFlightRecorder.isSelected()) {
                    checkFlightRecorder.setSelected(true);
                }

            }

        });

        checkFlightRecorder.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (!checkFlightRecorder.isSelected()) {
                    checkCustomFlightRecorder.setSelected(false);
                }

            }

        });

        int column = 0;
        int row = 0;
        for (Map.Entry<String, Entry> entry : items.entrySet()) {
            VBox btnBox = new VBox();
            Button btn = new Button(entry.getKey());
            btn.setGraphic(btnBox);
            btn.setPrefSize(130, 130);
            btn.setMnemonicParsing(false);

            Tooltip tt = new Tooltip(entry.getKey());
            if (fastToolTip) {
                btn.setOnMouseEntered(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        Point2D p = btn.localToScreen(btn.getLayoutBounds().getMaxX(), btn.getLayoutBounds().getMaxY());
                        tt.show(btn, p.getX(), p.getY());
                    }
                });
                btn.setOnMouseExited(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        tt.hide();
                    }
                });
            } else {
                btn.setTooltip(tt);
            }

            int accelKeyCode = getAccelKeyCode(column, row, maxCols);

            if (accelNeeded(accelKeyCode) && Boolean.parseBoolean(p.getProperty("showAccelKeyNo", "true"))) {
                btnBox.getChildren().add(new Label(String.valueOf(accelKeyCode)));
            }

            if (entry.getValue().icon != null) {
                File icon = new File(entry.getValue().icon);
                Image image = new Image(icon.toURI().toString());
                btnBox.getChildren().add(new ImageView(image));
            }
            btn.setContentDisplay(ContentDisplay.TOP);

            btn.setOnAction((e) -> {
                ProcessBuilder b = new ProcessBuilder(entry.getValue().eclipse, "-data", entry.getValue().workspace);

                if (checkCustomFlightRecorder.isSelected()) {
                    createPopUpDialog(primaryStage, p, arrayJFRTextFields);
                }

                if (clean.isSelected()) {
                    b.command().add("-clean");
                }

                if (checkFlightRecorder.isSelected()) {

                    b.command().add("-vmargs");
                    b.command().add(flightRecorder.getText().replaceAll("\\s", ""));
                    b.command().add(startFlightRecording.getText().replaceAll("\\s", ""));
                    b.command().add(flightReorderOptions.getText().replaceAll("\\s", ""));

                    System.out.println(b.command());

                }

                if (checkCustomFlightRecorder.isSelected()) {

                    b.command().add("-vmargs");
                    b.command().add(flightRecorder.getText().replaceAll("\\s", ""));
                    b.command().add(startFlightRecording.getText().replaceAll("\\s", ""));
                    b.command().add(flightReorderOptions.getText().replaceAll("\\s", ""));

                    System.out.println(b.command());

                }

                try {
                    b.start();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // exit
                primaryStage.close();
            });
            if (accelNeeded(accelKeyCode)) {
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.getKeyCode(Integer.toString(accelKeyCode))), () -> {
                    btn.fire();
                });
                scene.getAccelerators()
                        .put(new KeyCodeCombination(KeyCode.getKeyCode("Numpad " + Integer.toString(accelKeyCode))), () -> {
                            btn.fire();
                        });
            }

            pane.add(btn, column++, row);

            if ((column % maxCols) == 0) {
                row++;
                column = 0;
            }
        }

        pane.add(clean, 0, column == 0 ? row : row + 1, requiredCols, 1);

        pane.add(checkFlightRecorder, 0, column == 0 ? row : row + 2, requiredCols, 1);
        pane.add(checkCustomFlightRecorder, 0, column == 0 ? row : row + 3, requiredCols, 1);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (e) ->

        {
            if (e.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
            }
        });

        primaryStage.setScene(scene);

        scene.setFill(Color.TRANSPARENT);

        int screenIndex = Integer.parseInt(p.getProperty("screen", "0"));
        ObservableList<Screen> screens = Screen.getScreens();
        Screen target;
        if (screens.size() > screenIndex) {
            target = screens.get(screenIndex);
        } else {
            target = screens.get(0);
        }

        Rectangle2D screenBounds = target.getVisualBounds();
        primaryStage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - scene.getWidth()) / 2);
        primaryStage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - scene.getHeight()) / 2);

        Rectangle clip = new Rectangle(scene.getWidth(), scene.getHeight());
        clip.setArcHeight(10);
        clip.setArcWidth(10);
        pane.setClip(clip);
        pane.setStyle("-fx-background-color: white");

        primaryStage.show();
    }

    private boolean accelNeeded(int keyCode) {
        return (keyCode <= 9);
    }

    private int getAccelKeyCode(int column, int row, int maxCols) {
        return (row * maxCols + column + 1);
    }

    private void createPopUpDialog(Stage parent, Properties p, ArrayList<TextField> arrayTextFields) {
        // Create the custom dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("JFR Command-line Arguments");

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // Create the TextFields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 20, 10));

        int row = 0;
        for (TextField textField : arrayTextFields) {
            textField.setPrefWidth(textField.getText().length() * 6.5); //Why 6.5? just a trial number
            grid.add(textField, 0, row);
            row++;
        }

        dialog.getDialogPane().setContent(grid);

        int screenIndex = Integer.parseInt(p.getProperty("screen", "0"));
        ObservableList<Screen> screens = Screen.getScreens();
        Screen target;
        if (screens.size() > screenIndex) {
            target = screens.get(screenIndex);
        } else {
            target = screens.get(0);
        }

        Rectangle2D screenBounds = target.getVisualBounds();

        dialog.setX(screenBounds.getMinX() + (screenBounds.getWidth() - grid.getWidth()) / 3);
        dialog.setY(screenBounds.getMinY() + (screenBounds.getHeight() - grid.getHeight()) / 3);
        dialog.showAndWait();

        parent.setOnCloseRequest(event -> {
            dialog.close();
        });
    }

}
