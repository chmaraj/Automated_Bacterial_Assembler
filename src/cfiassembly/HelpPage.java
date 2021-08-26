package cfiassembly;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import java.util.List;
import java.util.Collections;
import java.io.File;

public class HelpPage {
	
	private Stage primaryStage;
	private File FastQCLocation, BBToolsLocation, ProdigalLocation;
	private String javaCall;
	
	public HelpPage(Stage primaryStage, File FastQCLocation, File BBToolsLocation, File ProdigalLocation, String javaCall) {
		this.primaryStage = primaryStage;
		this.FastQCLocation = FastQCLocation;
		this.BBToolsLocation = BBToolsLocation;
		this.ProdigalLocation = ProdigalLocation;
		this.javaCall = javaCall;
	}
	
	public void run() {
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
		banner.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		banner.setId("banner");
		pane.add(banner, 0, 0, 20, 2);
		
		Text helpTitle = new Text("HELP");
		helpTitle.setTextAlignment(TextAlignment.CENTER);
		HBox titleBox = new HBox(10);
		titleBox.setAlignment(Pos.CENTER);
		titleBox.getChildren().add(helpTitle);
		helpTitle.setId("title");
		pane.add(titleBox, 1, 0, 18, 2);
		
		Text helpText = new Text();
		helpText.setText("The purpose of this application is to provide a centralized hub for bioinformatics analysis of\n"
				+ "Illumina reads on CFIA computers. It is written in Java.");
		helpText.setTextAlignment(TextAlignment.CENTER);
		HBox helpBox = new HBox(10);
		helpBox.setAlignment(Pos.CENTER);
		helpBox.getChildren().add(helpText);
		pane.add(helpBox, 1, 2, 18, 2);
		
		Button qcButton = new Button("Read QC");
		qcButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		qcButton.getStyleClass().add("helpButton");
		qcButton.setFocusTraversable(false);
		pane.add(qcButton, 1, 5, 5, 3);
		
		Text qcText = new Text();
		qcText.setText("Runs FastQC on raw read sequence files. Takes a directory of fastq files (gzipped or not)\n" +
					"and returns HTML files which contain a variety of statistics related to quality and quantity\n" +
					"of reads. FastQC files represent statistics of individual sample files.");
		qcText.setTextAlignment(TextAlignment.CENTER);
		HBox qcBox = new HBox(10);
		qcBox.setAlignment(Pos.CENTER);
		qcBox.getChildren().add(qcText);
		pane.add(qcBox, 7, 5, 12, 3);
		
		Button quickButton = new Button("Quick Assembly");
		quickButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		quickButton.getStyleClass().add("helpButton");
		quickButton.setFocusTraversable(false);
		pane.add(quickButton, 1, 9, 5, 3);
		
		Text quickText = new Text();
		quickText.setText("Runs the Tadpole assembler from the BBTools suite on raw read files. Intended as a quick\n" +
						"and dirty QC tool. Takes a directory of fastq files and returns one assembly fastq file per sample,\n" +
						"each containing multiple contigs. Note that Tadpole does not spend much time resolving and\n" +
						"bridging repetitive regions, so while fast, it will return many more contigs than normal.");
		quickText.setTextAlignment(TextAlignment.CENTER);
		HBox quickBox = new HBox(10);
		quickBox.setAlignment(Pos.CENTER);
		quickBox.getChildren().add(quickText);
		pane.add(quickBox, 7, 9, 12, 3);
		
		Button assemblyButton = new Button("Assemble Genome");
		assemblyButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyButton.getStyleClass().add("helpButton");
		assemblyButton.setFocusTraversable(false);
		pane.add(assemblyButton, 1, 13, 5, 3);
		
		Text assemblyText = new Text();
		assemblyText.setText("A modified version of the Bacterial Genome Assembly pipeline found at:\n" +
							"https://github.com/duceppemo/bacteria_genome_assembly. Uses Tadpole instead of Unicycler.\n" +
							"Takes a directory of raw read fastq files and returns one assembly fastq file per sample. Filters,\n" +
							"trims, cleans, and merges reads. Takes a very long time, but more rigorous than Quick Assembly.");
		assemblyText.setTextAlignment(TextAlignment.CENTER);
		HBox assemblyBox = new HBox(10);
		assemblyBox.setAlignment(Pos.CENTER);
		assemblyBox.getChildren().add(assemblyText);
		pane.add(assemblyBox, 7, 13, 12, 3);
		
		Button backButton = new Button("Back");
		backButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		pane.add(backButton, 1, 17, 2, 2);
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				WelcomePage page = new WelcomePage(primaryStage, FastQCLocation, BBToolsLocation, ProdigalLocation, javaCall);
				page.run();
			}
		});
		
		Scene scene = new Scene(pane, 800, 500);
//		scene.getStylesheets().add(HelpPage.class.getResource("resources/HelpPage.css").toString()); // for running in Eclipse
		scene.getStylesheets().add("src/resources/HelpPage.css");
//		pane.setGridLinesVisible(true);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Automated Bacterial Assembler");
		primaryStage.show();
	}

}
