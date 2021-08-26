package cfiassembly;

import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Collections;

public class Methods {
	
	public static void fillGridPane(GridPane pane, int cols, int rows) {
		ColumnConstraints cC = new ColumnConstraints();
		cC.setPercentWidth(100.0 / cols);
		List<ColumnConstraints> colCopies = Collections.nCopies(20, cC);
		pane.getColumnConstraints().addAll(colCopies);
		RowConstraints rC = new RowConstraints();
		rC.setPercentHeight(100.0 / rows);
		List<RowConstraints> rowCopies = Collections.nCopies(20, rC);
		pane.getRowConstraints().addAll(rowCopies);
	}
	
	public static boolean checkReadsCompatible(File readDirectory) {
		File[] readList = readDirectory.listFiles();
		if(readList.length == 0) {
			return false;
		}
		boolean compatible = true;
		for(File entry : readList) {
			String[] splitString = entry.getName().split("\\.");
			boolean match = false;
			for(String string : splitString) {
				if(string.equals("fastq") || string.equals("fq") || string.equals("bam") || string.equals("sam")) {
					match = true;
				}
			}
			if(match == false) {
				compatible = false;
				break;
			}
		}
		if(compatible == true) {
			return true;
		}else {
			return false;
		}
	}

	public static void makeQALog(File qLog, String version, File outputDir, File inputDir) {
		try(FileWriter writer = new FileWriter(qLog)) {
			String sep = System.getProperty("line.separator");
			writer.write("CFIAssembly version: " + version);
			writer.write(sep);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			writer.write("Date run: " + dateFormat.format(date));
			writer.write(sep);
			writer.write("Run by user: " + System.getProperty("user.name"));
			writer.write(sep);
			writer.write("Output Folder: " + outputDir.getAbsolutePath());
			writer.write(sep);
			writer.write("Input File(s) :");
			for(File file : inputDir.listFiles()) {
				writer.write(sep);
				writer.write(file.getAbsolutePath());
			}
			writer.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkVersion() {
		try{
			URL url = new URL("https://github.com/chmaraj/BGA-Application/releases");
			try{
				URLConnection connection = url.openConnection();
				InputStream in = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				int latestVersion = 0;
				while((line = reader.readLine()) != null) {
					if(line.contains("<a href=\"/chmaraj/BGA-Application/releases/tag")){
						if(line.contains("</a>")) {
							String version_name = line.split(">")[1];
							version_name = version_name.split("<")[0];
							version_name = version_name.split(" ")[version_name.split(" ").length - 1];
							version_name = version_name.substring(1, version_name.length());
							String[] splitVersion = version_name.split("\\.");
							version_name = String.join("", splitVersion);
							if(Integer.parseInt(version_name) > latestVersion) {
								latestVersion = Integer.parseInt(version_name);
							}
						}
					}
				}
				String currentVersion = String.join("", WelcomePage.version.substring(1, WelcomePage.version.length()).split("\\."));
				if(Integer.parseInt(currentVersion) == latestVersion) {
					return true;
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
		}catch(MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void logMessage(TextArea outputField, String msg) {
		Platform.runLater(() -> outputField.appendText("\n" + msg + "\n"));
	}
}
