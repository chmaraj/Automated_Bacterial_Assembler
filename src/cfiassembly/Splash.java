package cfiassembly;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import utilityclasses.Find;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;

public class Splash extends Application {
	
	private String sep = File.separator;
	private Stage primaryStage;
	private static File FastQCLocation = null, BBToolsLocation = null, ProdigalLocation = null, JavaLocation = null;
	private static String javaCall;

	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		GridPane pane = new GridPane();
		Methods.fillGridPane(pane, 20, 20);
		
		Label banner = new Label();
		banner.setId("banner");
		banner.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		pane.add(banner, 0, 0, 20, 4);
		
		Text title = new Text("ABA");
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
//		scene.getStylesheets().add(Splash.class.getResource("resources/Splash.css").toString()); // for running in Eclipse
		scene.getStylesheets().add("src/resources/Splash.css");
		primaryStage.setScene(scene);
		primaryStage.setTitle("Automated Bacterial Assembler");
		primaryStage.show();
		
		FindDependencies task = new FindDependencies();
		progressText.textProperty().bind(task.messageProperty());
		progress.progressProperty().bind(task.progressProperty());
		Thread t = new Thread(task);
		t.start();
	}
	
	private class FindDependencies extends Task<Void>{
		
		public FindDependencies() {
		}
		
		@Override
		public Void call() {
			updateMessage("Finding Java");
			String codeLocation = Splash.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String codeParent = codeLocation;
			try{
				codeParent = (new File(codeLocation)).getCanonicalPath();
			}catch(IOException e) {
				e.printStackTrace();
			}
			Path dir = Paths.get(codeParent);
//			Path dir = new File("C:\\Users\\ChmaraJ\\Desktop").toPath(); // for running in Eclipse
			Find.Finder finder4;
			if(System.getProperties().getProperty("os.name").contains("Windows")) {
				finder4 = new Find.Finder("**ABA_java_runtime_windows", dir);
			}else {
				finder4 = new Find.Finder("**ABA_java_runtime_linux", dir);
			}
			for(Path path : finder4.run()) {
				File javapath = path.toFile();
				if(javapath.isDirectory()) {
					for(File item : javapath.listFiles()) {
						if(item.isDirectory()) {
							for(File item2 : item.listFiles()) {
								if(System.getProperties().getProperty("os.name").contains("Windows")) {
									if(item2.getName().equals("java.exe")) {
										JavaLocation = item.getAbsoluteFile();
									}
								}else {
									if(item2.getName().equals("java")) {
										JavaLocation = item.getAbsoluteFile();
									}
								}
							}
						}
					}
				}
			}
			if(System.getProperties().getProperty("os.name").contains("Windows")) {
				javaCall = JavaLocation.getAbsolutePath() + sep + "java.exe";
			}else {
				javaCall = JavaLocation.getAbsolutePath() + sep + "java";
			}
			updateProgress(1, 4);
			updateMessage("Finding FastQC");
			Find.Finder finder = new Find.Finder("**FastQC", dir);
			for(Path path : finder.run()) {
				File directory = path.toFile();
				if(directory.isDirectory()) {
					for(File file : directory.listFiles()) {
						if(file.getName().contains("run_fastqc.bat")) {
							FastQCLocation = path.toFile();
						}
					}
				}
			}
			if(FastQCLocation == null) {
				JOptionPane.showMessageDialog(null, "No FastQC found. Please download and unpack FastQC, located here:" +
						"https://www.bioinformatics.babraham.ac.uk/projects/fastqc/.\nAlternatively, if you have" +
						"FastQC downloaded, ensure that the containing folder is entitled 'FastQC'");
			}
			updateProgress(2, 4);
			updateMessage("Finding BBToolsSuite");
			Find.Finder finder2 = new Find.Finder("**bbmap", dir);
			for(Path path: finder2.run()) {
				File directory = path.toFile();
				for(File file : directory.listFiles()) {
					if(file.getName().contains("tadpole.sh")) {
						BBToolsLocation = path.toFile();
					}
				}
			}
			if(BBToolsLocation == null) {
				JOptionPane.showMessageDialog(null, "No BBTools Suite found. Please download and unpack BBTools, located here:" +
						"https://sourceforge.net/projects/bbmap/. \n Alternatively, if you have BBMap downloaded," +
						"ensure that the containing folder is entitled 'bbMap'");
			}
			updateProgress(3, 4);
			updateMessage("Finding Prodigal");
			Find.Finder finder3 = new Find.Finder("**prodigal.*", dir);
			for(Path path : finder3.run()) {
				if(System.getProperties().getProperty("os.name").contains("Windows") && path.toString().contains(".exe")) {
					ProdigalLocation = path.toFile().getParentFile();
				}else if(!System.getProperties().getProperty("os.name").contains("Windows") && path.toString().contains("linux")) {
					ProdigalLocation = path.toFile().getParentFile();
				}
			}
			if(FastQCLocation == null && BBToolsLocation == null) {
				JOptionPane.showMessageDialog(null, "No FastQC or BBTools located. Exiting");
				System.exit(-1);
			}
			updateProgress(4, 4);
			updateMessage("Done");
			WelcomePage page = new WelcomePage(primaryStage, FastQCLocation, BBToolsLocation, ProdigalLocation, javaCall);
			Platform.runLater(() -> page.run());
			return null;
		}
	}
	
	public static void run(String[] args) {		
		Application.launch(Splash.class);
	}
}
