package com.allen.joseph.NexusUpgrader.Gui;

import java.io.File;
import java.io.IOException;

import com.allen.joseph.NexusUpgrader.Utils.EventBusUtil;
import com.allen.joseph.NexusUpgrader.Utils.Utilities;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * 
 * @author Joseph Allen
 *
 */
public class UpgradeGui{
	public static EventBus eventBus = new EventBus();
	public final static int VISIBLE = 0;
	public final static int START = 1;
	public final static int FINISH = 2;
	
	private Stage primaryStage = null;
	private File destFile = null;
	private TextField upgradeFilePathTextField = null;
	private TextField recoveryFilePathTextField = null;
	private TextField superSuFilePathTextField = null;
	private ProgressIndicator progressIndicator = null;
	private Button flash = null;
	private TextArea flashResultsTextArea = null;
	
	/**
	 * 
	 * @param primaryStage
	 * @throws Exception
	 */
	public void buildUi(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		EventBusUtil.register(this);
		
		progressIndicator = new ProgressIndicator(0);
		progressIndicator.setVisible(false);
		primaryStage.setTitle("Nexus Upgrader");
		
		upgradeFilePathTextField = new TextField("");
		upgradeFilePathTextField.setEditable(false);
		recoveryFilePathTextField = new TextField("");
		recoveryFilePathTextField.setEditable(false);
		superSuFilePathTextField = new TextField("");
		superSuFilePathTextField.setEditable(false);
		
		Button selectUpgradeFileButton = createButton("Upgrade Image File Path", upgradeFilePathTextField);
		Button recoveryFileButton = createButton("Recovery File Path", recoveryFilePathTextField);
		Button superSuFileButton = createButton("Super Su File Path", superSuFilePathTextField);
		
		flash = new Button("Flash");
		flash.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				File upgradeFile = new File(upgradeFilePathTextField.getText());
				destFile = new File(upgradeFile.getParent() + "\\Temp");
				if(destFile.exists()){
					Utilities.delete(destFile);
					destFile.mkdir();
				}
				Utilities.unzip(upgradeFile, destFile, progressIndicator);
			}
		});
		
		VBox root = new VBox();
		root.getChildren().add(createMenu());
		root.getChildren().add(selectUpgradeFileButton);
		root.getChildren().add(upgradeFilePathTextField);
		root.getChildren().add(recoveryFileButton);
		root.getChildren().add(recoveryFilePathTextField);
		root.getChildren().add(superSuFileButton);
		root.getChildren().add(superSuFilePathTextField);
		root.getChildren().add(flash);
		root.getChildren().add(progressIndicator);
		flashResultsTextArea = new TextArea();
		flashResultsTextArea.setEditable(false);
		root.getChildren().add(flashResultsTextArea);
		
		Scene scene = new Scene(root);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		primaryStage.setWidth(primaryScreenBounds.getWidth() * .27);
		primaryStage.setHeight(primaryScreenBounds.getHeight() * .37);
		scene.setFill(Color.OLDLACE);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	/**
	 * 
	 * @param buttonText
	 * @param textFieldToEdit
	 * @return
	 */
	private Button createButton(String buttonText, TextField textFieldToEdit){
		Button button = new Button(buttonText);
		button.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Select File");
				File file = fileChooser.showOpenDialog(primaryStage);
				textFieldToEdit.setText(file.getAbsolutePath());
			}
		});
		return button;
	}
	
	/**
	 * Returns the menu for the Gui
	 * @return
	 */
	private MenuBar createMenu(){
		MenuBar menuBar = new MenuBar();
		
		Menu menuFile = new Menu("File");
		MenuItem menuFileExit = new MenuItem("Exit");
		menuFileExit.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				try{
					Utilities.delete(destFile);
				}catch(Exception e){
					e.printStackTrace();
				}
				System.exit(0);
			}
		});
		menuFile.getItems().add(menuFileExit);
		Menu menuAbout = new Menu("Disclaimer");
		MenuItem aboutMenuItem = new MenuItem("The developer(s) of this tool are not responsible\n"
				                            + "for any damages to your device. By using this tool your are\n"
				                            + "accepting all responsibility for anything that happens to\n"
				                            + "your device. This can include, but is not limited to voiding\n"
				                            + "the warranty or bricking the device. Proceed at your own risk.");
		menuAbout.getItems().add(aboutMenuItem);
		menuBar.getMenus().addAll(menuFile, menuAbout);
		
		return menuBar;
	}
	/**
	 * Handles Events from the EventBus
	 * @param event
	 */
	@Subscribe
	public void progressEvent(String event){
		System.out.println("Processing Event: " + event.toString());
		if(event.equals("Start")){
			flash.setDisable(true);
			progressIndicator.setVisible(true);
			progressIndicator.setProgress(-1);
		}
		else if(event.equals("FinishUnzip")){ 
			flash.setDisable(false);
			Utilities.upgradeNexus(destFile.getPath(), recoveryFilePathTextField.getText() , superSuFilePathTextField.getText());
		}
		else if(event.equals("FinishFlash")){
			progressIndicator.setProgress(1);
		}
		else if(event.contains("Error")){
			Utilities.delete(destFile);
			progressIndicator.setProgress(0);
		}
		else{
			flashResultsTextArea.setText(flashResultsTextArea.getText() + "\n" + event);
			flashResultsTextArea.setScrollTop(Double.MAX_VALUE);
		}
	}
}
