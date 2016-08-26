package com.wamas.ecws.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
		if(!cfg.isFile()) {
			System.err.println("No config found in " + cfg);
			return;
		}
		
		Properties p = new Properties();
		try (InputStream s = new FileInputStream(cfg)) {
			p.load(s);
		}
		
		Map<String, Entry> items = new TreeMap<>();
		for(Map.Entry<?, ?> entry : p.entrySet()) {
			String k = entry.getKey().toString();
			
			String name = null;
			int indexOfWs = k.indexOf("_workspace");
			if(indexOfWs != -1) {
				name = k.substring(0, indexOfWs);
				
				Entry e = items.get(name);
				if(e == null) {
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

		int requiredCols = items.size() <= maxCols ? items.size() : maxCols;
		int requiredRows = (items.size() + maxCols -1) / maxCols;
		Scene scene = new Scene(pane, 5 + (requiredCols * 135), 30 + (requiredRows * 135));
		
		int column = 0;
		int row = 0;
		for(Map.Entry<String, Entry> entry : items.entrySet()) {
			Button btn = new Button(entry.getKey());
			btn.setPrefSize(130, 130);
			btn.setMnemonicParsing(false);
			if(entry.getValue().icon != null) {
				File icon = new File(entry.getValue().icon);
				Image image = new Image(icon.toURI().toString());
				btn.setGraphic(new ImageView(image));
			}
			btn.setContentDisplay(ContentDisplay.TOP);
			
			btn.setOnAction((e) -> {
				ProcessBuilder b = new ProcessBuilder(entry.getValue().eclipse, "-data", entry.getValue().workspace);
				
				if(clean.isSelected()) {
					b.command().add("-clean");
				}
				
				try {
					b.start();
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				
				// exit
				primaryStage.close();
			});
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.getKeyCode(Integer.toString(column + 1))), () -> {
				btn.fire();
			});
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.getKeyCode("Numpad " + Integer.toString(column + 1))), () -> {
				btn.fire();
			});
			
			pane.add(btn, column++, row);
			
			if((column % maxCols) == 0) {
				row++;
				column = 0;
			}
		}
		
		pane.add(clean, 0, column == 0 ? row : row + 1, requiredCols, 1);
		
		scene.addEventHandler(KeyEvent.KEY_PRESSED, (e) -> {
			if(e.getCode() == KeyCode.ESCAPE) {
				primaryStage.close();
			}
		});
		
		primaryStage.setScene(scene);
		
		scene.setFill(Color.TRANSPARENT);
		
		int screenIndex = Integer.parseInt(p.getProperty("screen", "0"));
		Screen target = Screen.getScreens().get(screenIndex);
		
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

}
