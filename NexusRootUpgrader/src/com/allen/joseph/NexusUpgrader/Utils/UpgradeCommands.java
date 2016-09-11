package com.allen.joseph.NexusUpgrader.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 
 * @author Joseph Allen
 *
 */
public class UpgradeCommands {
	private static String adb = ".\\files\\adb.exe";
	private static String fastboot = ".\\files\\fastboot.exe";
	
	/**
	 * 
	 * @param command Command
	 */
	public static int runCommand(String command, String arg){
		return runCommand(command, arg, null, null);
	}

	/**
	 * 
	 * @param command Command
	 */
	public static int runCommand(String command, String arg1, String arg2){
		return runCommand(command, arg1, arg2, null);
	}
	
	/**
	 * 
	 * @param command Command
	 */
	public static int runCommand(String command, String arg1, String arg2, String arg3){
		try {
			EventBusUtil.publish("****Running " + command + " with argument: " + arg1 + ", " + arg2 + ", " + arg3);
			Process process = null;
			if(command.toLowerCase().equals("adb")){
				command = adb;
			}
			else if(command.toLowerCase().equals("fastboot")){
				command = fastboot;
			}
			ProcessBuilder pb = null;
			if(arg2 != null && arg3 != null){
				pb = new ProcessBuilder(command, arg1, arg2, arg3);
			}
			else if (arg2 != null){
				pb = new ProcessBuilder(command, arg1, arg2);
			}
			else{
				pb = new ProcessBuilder(command, arg1);
			}
			pb.redirectErrorStream(true);
			process = pb.start();
						
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
				EventBusUtil.publish(line);
			}

			if(process.exitValue() != 0){
				EventBusUtil.publish("!!!Error running command!!!");
				Thread.currentThread().interrupt();
			}
			return process.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}
