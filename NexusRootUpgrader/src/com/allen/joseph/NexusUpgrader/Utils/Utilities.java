package com.allen.joseph.NexusUpgrader.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import com.allen.joseph.NexusUpgrader.Gui.UpgradeGui;

import javafx.scene.control.ProgressIndicator;

/**
 * 
 * @author Joseph Allen
 *
 */
public class Utilities extends Thread{
	private static ProgressIndicator progressIndicator = null;
	private static File sourceFile = null;
	private static File destDir = null;
	
    public static void unzip(File sourceFile, File destDir, ProgressIndicator pi) {
    	Utilities.setSourceFile(sourceFile);
    	Utilities.setDestDir(destDir);
    	Utilities.setProgressIndicator(pi);
    	Utilities util = new Utilities();
    	util.start();
    }
    
    public void run(){
    	EventBusUtil.publish("Start");
    	unzipFile(sourceFile, destDir);
    	File[] fileList = destDir.listFiles();
    	File[] baseFileList = fileList[0].listFiles();
    	moveImgFiles(fileList[0], destDir);
    	
    	for(File file : baseFileList){
    		if(file.getName().contains("image")){
    			unzipFile(file, destDir);
    		}
    	}
		EventBusUtil.publish("FinishUnzip");
    }
    
    private static void moveImgFiles(File source, File destDir){
    	EventBusUtil.publish("Moving: " + source.getName());
    	for(File file : source.listFiles()){
    		if(file.getName().contains("img")){
    			file.renameTo(new File(destDir.getAbsolutePath() + "\\" + file.getName()));
    		}
    	}
    }
    
    private static void unzipFile(File sourceFile, File destDir){
    	EventBusUtil.publish("Unzipping: " + sourceFile.getName());
    	Archiver archiver = null;
    	if(sourceFile.getAbsolutePath().contains("tgz")){
    		archiver = ArchiverFactory.createArchiver("tar", "gz");
    	}
    	else if( sourceFile.getAbsolutePath().contains("zip")){
    		archiver = ArchiverFactory.createArchiver("zip");
    	}
    	try {
			archiver.extract(sourceFile, destDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static String findFileMatching(String filepath, String pattern){
    	File file = new File(filepath);
    	File[] files = file.listFiles();
    	
    	for(File fileCandidate : files){
    		if(fileCandidate.getName().contains(pattern)){
    			return fileCandidate.getName();
    		}
    	}
    	return null;
    }
    
    public static void upgradeNexus(String upgradeFilepath, String recovery, String superSu){
		Thread upgrade = new Thread(new Runnable(){
			public void run(){
				try{
					System.out.println("Flashing");
					//Check for an android device
					UpgradeCommands.runCommand("adb", "devices");
					//Reboot to bootloader
					UpgradeCommands.runCommand("adb", "reboot-bootloader");
					Thread.sleep(3000);
					//Check for an android device
					UpgradeCommands.runCommand("fastboot", "devices");
					//Flash the bootloader
					UpgradeCommands.runCommand("fastboot", "flash", "bootloader" , upgradeFilepath + "\\" + Utilities.findFileMatching(upgradeFilepath, "bootloader"));
					//Reboot the bootloader
					UpgradeCommands.runCommand("fastboot", "reboot-bootloader");
					Thread.sleep(3000);
					//Flash the radio
					UpgradeCommands.runCommand("fastboot", "flash", "radio" , upgradeFilepath + "\\" + Utilities.findFileMatching(upgradeFilepath, "radio"));
					//Reboot the bootloader
					UpgradeCommands.runCommand("fastboot", "reboot-bootloader");
					Thread.sleep(3000);
					//Flash the boot image
					UpgradeCommands.runCommand("fastboot", "flash", "boot" , upgradeFilepath + "\\" + "boot.img");
					//Flash the cache image
					UpgradeCommands.runCommand("fastboot", "flash", "cache" , upgradeFilepath + "\\" + "cache.img");
					//Flash the system image
					UpgradeCommands.runCommand("fastboot", "flash", "system" , upgradeFilepath + "\\" + "system.img");
					//Flash the vendor image
					UpgradeCommands.runCommand("fastboot", "flash", "vendor" , upgradeFilepath + "\\" + "vendor.img");
					if(recovery != null && !recovery.equals("")){
						//Flash recovery with recovery
						UpgradeCommands.runCommand("fastboot", "flash", "recovery" , recovery);
					}
					else{
						//Flash recovery with Google Recovery
						UpgradeCommands.runCommand("fastboot", "flash", "recovery" , upgradeFilepath + "\\" + Utilities.findFileMatching(upgradeFilepath, "recovery"));
					}
					
					if(superSu != null && !superSu.equals("")){
						//Reboot to recovery
						JOptionPane.showMessageDialog(null, "1) Please boot into recovery\n2) Select Advanced\n3)Select ADB Sideload\n4)Click ok on this window when ready.", "Reboot Recovery", JOptionPane.PLAIN_MESSAGE);
						//Sideload Su Binaries
						UpgradeCommands.runCommand("adb", "sideload", superSu);
						//Reboot to system
						UpgradeCommands.runCommand("adb", "reboot");
					}
					else{
						UpgradeCommands.runCommand("fastboot", "reboot");
					}

					EventBusUtil.publish("FinishFlash");
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					Utilities.delete(new File(upgradeFilepath));
				}
			}
		});
		upgrade.start();
    }

	public static ProgressIndicator getProgressIndicator() {
		return progressIndicator;
	}

	public static void setProgressIndicator(ProgressIndicator progressIndicator) {
		Utilities.progressIndicator = progressIndicator;
	}

	public static File getSourceFile() {
		return sourceFile;
	}

	public static void setSourceFile(File sourceFile) {
		Utilities.sourceFile = sourceFile;
	}

	public static File getDestDir() {
		return destDir;
	}

	public static void setDestDir(File destDir) {
		Utilities.destDir = destDir;
	}

	public static void delete(File inputFile) {
		if(inputFile.isDirectory()){
			File[] files = inputFile.listFiles();
			for(File file : files){
				if(file.isDirectory()){
					delete(file);
				}
				file.delete();
			}
		}
		inputFile.delete();
	}
}