package com.allen.joseph.NexusUpgrader;

import com.allen.joseph.NexusUpgrader.Gui.UpgradeGui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 
 * @author Joseph Allen
 *
 */
public class NexusRootUpgraderMain extends Application{
	
	static public void main(String[] args){
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		UpgradeGui upgradeGui = new UpgradeGui();
		upgradeGui.buildUi(primaryStage);
	}
}