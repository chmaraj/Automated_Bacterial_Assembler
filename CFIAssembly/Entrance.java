package bacterialgenomeassembly;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.lang.Process;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.swing.JOptionPane;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.Desktop;
import java.net.URI;

public class Entrance extends Application {
	
	private Stage primaryStage;
	private static File FastQCLocation, BBToolsLocation;

	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		GridPane pane = new GridPane();
		ColumnConstraints cC = new ColumnConstraints();
		cC.setPercentWidth(5);
		List<ColumnConstraints> colCopies = Collections.nCopies(20, cC);
		pane.getColumnConstraints().addAll(colCopies);
		RowConstraints rC = new RowConstraints();
		rC.setPercentHeight(5);
		List<RowConstraints> rowCopies = Collections.nCopies(20, rC);
		pane.getRowConstraints().addAll(rowCopies);
		
		Label banner = new Label();
		banner.setId("banner");
		banner.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		pane.add(banner, 0, 0, 20, 4);
		
		Text title = new Text("CFIAssembly");
		title.setId("title");
		title.setTextAlignment(TextAlignment.LEFT);
		pane.add(title, 1, 1, 20, 2);
		
		Text progressText = new Text();
		progressText.setTextAlignment(TextAlignment.CENTER);
		progressText.setId("progress_text");
		HBox textBox = new HBox(10);
		textBox.setAlignment(Pos.CENTER);
		textBox.getChildren().add(progressText);
		pane.add(textBox, 2, 12, 16, 1);
		
		ProgressBar progress = new ProgressBar();
		progress.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		pane.add(progress, 2, 14, 16, 1);
		
		Scene scene = new Scene(pane, 500, 300);
		scene.getStylesheets().add("Splash.css");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		CheckDependenciesWindows task = new CheckDependenciesWindows();
		progressText.textProperty().bind(task.messageProperty());
		progress.progressProperty().bind(task.progressProperty());
		Thread t = new Thread(task);
		t.start();
	}
	
	public static void main(String[] args) {		
		Entrance entrance = new Entrance();
		Application.launch(entrance.getClass());
	}
	
	private class CheckDependenciesWindows extends Task<Void>{
		
		public CheckDependenciesWindows() {
			
		}
		
		@Override
		public Void call() {
			updateMessage("Finding Java");
			if(!findJavaWindows()) {
				JOptionPane.showMessageDialog(null, "No Java found in PATH. Please put Java in PATH prior to running.");
				System.exit(-1);
			}
			updateProgress(1, 3);
			updateMessage("Finding FastQC");
			if(!findFastQCWindows()) {
				JOptionPane.showMessageDialog(null, "No FastQC found. Please download and unpack FastQC, located here:" +
								"https://www.bioinformatics.babraham.ac.uk/projects/fastqc/");
				if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(new URI("https://www.bioinformatics.babraham.ac.uk/projects/fastqc/"));
					}catch(Exception exception) {
						exception.printStackTrace();
					}
				}
				System.exit(-1);
			}
			updateProgress(2, 3);
			updateMessage("Finding BBTools");
			if(!findBBToolsWindows()) {
				JOptionPane.showMessageDialog(null, "No BBTools Suite found. Please download and unpack BBTools, located here:" +
								"https://sourceforge.net/projects/bbmap/");
				if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(new URI("https://sourceforge.net/projects/bbmap/"));
					}catch(Exception exception) {
						exception.printStackTrace();
					}
				}
				System.exit(-1);
			}
			updateProgress(3, 3);
			WelcomePage page = new WelcomePage(primaryStage, FastQCLocation, BBToolsLocation);
			Platform.runLater(() -> page.run());
			return null;
		}
	}
	
//	private static void checkDependenciesLinux() {
//		
//	}
	
	private static boolean findJavaWindows() {
//		File dir = new File("C:/");
		ArrayList<String> results = new ArrayList<String>();
		try{
			String line;
//			Process p = new ProcessBuilder("java", "-version").directory(dir).start();
			Process p = Runtime.getRuntime().exec("java -version");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while((line = stdout.readLine()) != null) {
				results.add(line);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		if(results.get(0).contains("java version")) {
			return true;
		}
		return false;
	}
	
	private static boolean findFastQCWindows() {
		Path dir = Paths.get(System.getProperties().getProperty("user.home"));
		Find.Finder finder = new Find.Finder("**FastQC", dir);
		for(Path path : finder.run()) {
			String[] splitPath = path.toString().split("\\\\");
			if(splitPath[splitPath.length - 2].contains("fastqc_v")) {
				FastQCLocation = path.toFile();
				return true;
			}
		}		
		return false;
	}
	
	private static boolean findBBToolsWindows() {
		Path dir = Paths.get(System.getProperties().getProperty("user.home"));
		Find.Finder finder = new Find.Finder("**bbMap", dir);
		for(Path path: finder.run()) {
			String[] splitPath = path.toString().split("\\\\");
			if(splitPath[splitPath.length - 2].matches("BBMap_\\d{2}\\.\\d{2}")) { //Pattern has to match "BBMap" followed by 2 digits, a period, then 2 digits
				BBToolsLocation = path.toFile();
				return true;
			}
		}
		return false;
	}
}
