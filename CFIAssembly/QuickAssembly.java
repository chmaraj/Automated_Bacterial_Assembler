package bacterialgenomeassembly;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.lang.Process;
import java.lang.Runtime;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QuickAssembly implements Page{
	
	Stage primaryStage;
	File readDirectory, outputDirectory, FastQCLocation, BBToolsLocation;
//	private String[] options, userDefined;

	public QuickAssembly(Stage primaryStage, File FastQCLocation, File BBToolsLocation) {
		this.primaryStage = primaryStage;
		this.FastQCLocation = FastQCLocation;
		this.BBToolsLocation = BBToolsLocation;
//		this.options = new String[] {"Threads", "k", "showstats"};
//		this.userDefined = new String[] {"3", "124", "t"};
	}
	
	public void run() {
		GridPane assemblyPane = new GridPane();
		ColumnConstraints cC = new ColumnConstraints();
		cC.setPercentWidth(5);
		List<ColumnConstraints> colCopies = Collections.nCopies(20, cC);
		assemblyPane.getColumnConstraints().addAll(colCopies);
		RowConstraints rC = new RowConstraints();
		rC.setPercentHeight(5);
		List<RowConstraints> rowCopies = Collections.nCopies(20, rC);
		assemblyPane.getRowConstraints().addAll(rowCopies);
		
		Text disclaimer = new Text("Disclaimer: The quick assembly takes raw sequence reads as input. \n"
				+ "Please note that the lack of read pre-processing will mean a very fast, but potentially relatively low-quality assembly. "
				+ "Also note that this process will take a large amount of RAM");
		disclaimer.setId("quick_disclaimer");
		disclaimer.setTextAlignment(TextAlignment.CENTER);
		HBox disclaimerBox = new HBox(10);
		disclaimerBox.setAlignment(Pos.CENTER);
		disclaimerBox.getChildren().add(disclaimer);
		assemblyPane.add(disclaimerBox, 1, 0, 18, 1);
		
		Text inputPrompt = new Text("Please enter a directory which contains the raw reads");
		inputPrompt.setTextAlignment(TextAlignment.CENTER);
		HBox inputBox = new HBox(10);
		inputBox.setAlignment(Pos.CENTER);
		inputBox.getChildren().add(inputPrompt);
		assemblyPane.add(inputBox, 1, 1, 7, 1);
		
		TextField readInput = new TextField();
		readInput.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		readInput.setEditable(false);
		assemblyPane.add(readInput, 1, 2, 11, 1);
		
		Button browseInputs = new Button("Browse...");
		browseInputs.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		browseInputs.getStyleClass().add("browseButton");
		assemblyPane.add(browseInputs, 13, 2, 2, 1);
		browseInputs.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				readDirectory = directoryChooser.showDialog(primaryStage);
				readInput.setText(readDirectory.getAbsolutePath());
			}
		});
		
		Text outputPrompt = new Text("Please enter a directory which will receive the output contigs");
		outputPrompt.setTextAlignment(TextAlignment.CENTER);
		HBox outputBox = new HBox(10);
		outputBox.setAlignment(Pos.CENTER);
		outputBox.getChildren().add(outputPrompt);
		assemblyPane.add(outputBox, 1, 3, 8, 1);
		
		TextField outputFileField = new TextField();
		outputFileField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		outputFileField.setEditable(false);
		assemblyPane.add(outputFileField, 1, 4, 11, 1);
		
		Button browseOutputs = new Button("Browse...");
		browseOutputs.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		browseOutputs.getStyleClass().add("browseButton");
		assemblyPane.add(browseOutputs, 13, 4, 2, 1);
		browseOutputs.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				outputDirectory = directoryChooser.showDialog(primaryStage);
				outputFileField.setText(outputDirectory.getAbsolutePath());
			}
		});
		
//		Button advancedButton = new Button("Advanced\nOptions");
//		advancedButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
//		advancedButton.setTextAlignment(TextAlignment.CENTER);
//		assemblyPane.add(advancedButton, 16, 2, 2, 2);
		
		Text threadPrompt = new Text("Input number of\nthreads to use(1, 2, etc.)");
		threadPrompt.setTextAlignment(TextAlignment.CENTER);
		HBox threadBox = new HBox(10);
		threadBox.setAlignment(Pos.CENTER);
		threadBox.getChildren().add(threadPrompt);
		assemblyPane.add(threadBox, 16, 1, 2, 2);
		
		TextField threadField = new TextField();
		threadField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		threadField.setAlignment(Pos.CENTER);
		threadField.setText(Integer.toString(Runtime.getRuntime().availableProcessors() - 1));
		assemblyPane.add(threadField, 16, 3, 3, 2);
		
		Button backButton = new Button("Back");
		backButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyPane.add(backButton, 1, 17, 2, 2);
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				WelcomePage page = new WelcomePage(primaryStage, FastQCLocation, BBToolsLocation);
				page.run();
			}
		});
		
		TextArea outputField = new TextArea();
		outputField.setEditable(false);
		assemblyPane.add(outputField, 1, 6, 18, 10);
		
		Text alertText = new Text();
		alertText.setId("alertText");
		HBox alertBox = new HBox(10);
		alertBox.setAlignment(Pos.CENTER);
		alertBox.getChildren().add(alertText);
		assemblyPane.add(alertText, 1, 16, 18, 1);
		
		Button proceed = new Button("Proceed");
		proceed.setId("proceedButton");
		proceed.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyPane.add(proceed, 9, 17, 2, 2);
		proceed.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				int threads = 0;
				try {
					threads = Integer.parseInt(threadField.getText());
				}catch(Exception exception) {
					alertText.setText("You need to enter a number in the Threads field");
					return;
				}
				if(readDirectory == null || outputDirectory == null) {
					alertText.setText("Please enter both an input file directory and an output file directory");
				}else if(!checkReadsCompatible()){
					alertText.setText("Directory containing reads is either empty or contains incompatible files");
				}else if(threads >= Runtime.getRuntime().availableProcessors()){
					alertText.setText("Thread count must be at most 1 less than total number of cores available");
				}else{
					alertText.setText("");
					TadpoleTask task = new TadpoleTask(outputField, threads);
					Thread t = new Thread(task);
					t.start();
				}
			}
		});
		
		Button helpButton = new Button("Help");
		helpButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyPane.add(helpButton, 17, 17, 2, 2);
		helpButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				
			}
		});
		
		Scene scene = new Scene(assemblyPane, 800, 500);
		scene.getStylesheets().add("QCPage.css");
		primaryStage.setScene(scene);
//		assemblyPane.setGridLinesVisible(true);
		primaryStage.show();
	}
	
	public boolean checkReadsCompatible() {
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
	
	private class TadpoleTask extends Task<Void> {
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		private int threads;
		public TadpoleTask(TextArea outputField, int threads) {
			this.outputField = outputField;
			this.threads = threads;
		}
		
		@Override
		protected Void call() {
			BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
			consumer = new MessageConsumer(messageQueue, this.outputField);
			String memJava = "7g";
			String processCall = "java -ea -Xmx" + memJava + " -cp ./current assemble.Tadpole";
			File[] readList = readDirectory.listFiles();
			ArrayList<String> sampleList = new ArrayList<String>();
			for(File entry : readList) {
				String sampleID = entry.getName().replace("_R1_001.fastq.gz", "");
				sampleID = sampleID.replace("_R2_001.fastq.gz", "");
				if(!sampleList.contains(sampleID)) {
					sampleList.add(sampleID);
				}
			}
			for(String item : sampleList) {
				String in1 = readDirectory.getAbsolutePath() + "\\" + item + "_R1_001.fastq.gz";
				String in2 = readDirectory.getAbsolutePath() + "\\" + item + "_R2_001.fastq.gz";
				String out = outputDirectory.getAbsolutePath() + "\\" + item;
				String options =  " in1=" + in1 + " in2=" + in2 + " out=" + out + " threads=" + this.threads + " k=124 showstats=t";
				String fullProcessCall = processCall + options;
				try {
					Process assemblyRun = Runtime.getRuntime().exec(fullProcessCall, null, BBToolsLocation);
					BufferedReader error = new BufferedReader(new InputStreamReader(assemblyRun.getErrorStream()));
					Platform.runLater(() -> consumer.start());
					while((this.line = error.readLine())!= null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					consumer.stop();
					error.close();
					try {
						assemblyRun.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
	
//	public String[] getOptions() {
//		return this.options;
//	}
//	
//	public void setOptions(String[] options) {
//		this.options = options;
//	}
//	
//	public String[] getUserDefined() {
//		return this.userDefined;
//	}
//	
//	public void setUserDefined(String[] userDefined) {
//		this.userDefined = userDefined;
//	}
}
