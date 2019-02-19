package bacterialgenomeassembly;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.stage.DirectoryChooser;

import javafx.concurrent.Task;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.lang.Runtime;
import java.lang.Process;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;
import java.util.Collections;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QCPage implements Page{
	
//	private String separator = "/";

	private Stage primaryStage;
	private File readDirectory, outputDirectory, FastQCLocation, BBToolsLocation, ProdigalLocation;
	private boolean readDirectoryCompatible = false;
//	private String[] options, userDefined;
	
	public QCPage(Stage primaryStage, File FastQCLocation, File BBToolsLocation, File ProdigalLocation) {
		this.primaryStage = primaryStage;
		this.FastQCLocation = FastQCLocation;
		this.BBToolsLocation = BBToolsLocation;
		this.ProdigalLocation = ProdigalLocation;
//		if(System.getProperties().getProperty("os.name").contains("Windows")) {
//			this.separator = "\\";
//		}else {
//			this.separator = "/";
//		}
	}
	
	public void run() {
//		QCPage currentPage = this;
		GridPane qcPane = new GridPane();
		ColumnConstraints cC = new ColumnConstraints();
		cC.setPercentWidth(5);
		List<ColumnConstraints> colCopies = Collections.nCopies(20, cC);
		qcPane.getColumnConstraints().addAll(colCopies);
		RowConstraints rC = new RowConstraints();
		rC.setPercentHeight(5);
		List<RowConstraints> rowCopies = Collections.nCopies(20, rC);
		qcPane.getRowConstraints().addAll(rowCopies);
		
		Text readFilePrompt = new Text("Please select the directory which contains your read file(s)");
		readFilePrompt.getStyleClass().add("prompt");
		readFilePrompt.setTextAlignment(TextAlignment.LEFT);
		HBox promptBox = new HBox(10);
		promptBox.getChildren().add(readFilePrompt);
		promptBox.setAlignment(Pos.CENTER);
		qcPane.add(promptBox, 1, 1);
		
		TextField readFileField = new TextField();
		readFileField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		readFileField.setEditable(false);
		qcPane.add(readFileField, 1, 2, 11, 1);
		
		Button browseFiles = new Button("Browse...");
		browseFiles.getStyleClass().add("browseButton");
		browseFiles.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		qcPane.add(browseFiles, 13, 2, 2, 1);
		browseFiles.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				readDirectory = directoryChooser.showDialog(primaryStage);
				readFileField.setText(readDirectory.getAbsolutePath());
				if(!checkCompatible(readDirectory)) {
					Text alertText = new Text("The directory chosen either contains no files or contains files incompatible with FastQC");
					alertText.getStyleClass().add("alertText");
					alertText.setTextAlignment(TextAlignment.CENTER);
					HBox alertBox = new HBox(10);
					alertBox.setAlignment(Pos.CENTER);
					alertBox.getChildren().add(alertText);
					qcPane.add(alertBox, 1, 3, 14, 1);
				}else {
					readDirectoryCompatible = true;
				}
			}
		});
		
		Text outputFilePrompt = new Text("Please select a directory to receive the output");
		outputFilePrompt.getStyleClass().add("prompt");
		outputFilePrompt.setTextAlignment(TextAlignment.CENTER);
		HBox outputBox = new HBox(10);
		outputBox.getChildren().add(outputFilePrompt);
		outputBox.setAlignment(Pos.CENTER);
		qcPane.add(outputBox, 1, 3);
		
		TextField outputFileField = new TextField();
		outputFileField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		outputFileField.setEditable(false);
		qcPane.add(outputFileField, 1, 4, 11, 1);
		
		Button browseOutputs = new Button("Browse...");
		browseOutputs.getStyleClass().add("browseButton");
		browseOutputs.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		qcPane.add(browseOutputs, 13, 4, 2, 1);
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
//		qcPane.add(advancedButton, 16, 2, 2, 2);
//		advancedButton.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent e) {
//				AdvancedOptions page = new AdvancedOptions(currentPage);
//				page.defineOptions();
//			}
//		});
		
		Text threadPrompt = new Text("Input number of\nthreads to use(1, 2, etc.)");
		threadPrompt.setTextAlignment(TextAlignment.CENTER);
		HBox threadBox = new HBox(10);
		threadBox.setAlignment(Pos.CENTER);
		threadBox.getChildren().add(threadPrompt);
		qcPane.add(threadBox, 16, 1, 2, 2);
		
		TextField threadField = new TextField();
		threadField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		threadField.setAlignment(Pos.CENTER);
		threadField.setText(Integer.toString(Runtime.getRuntime().availableProcessors() - 1));
		qcPane.add(threadField, 16, 3, 3, 2);
		
		Text alertText = new Text();
		alertText.getStyleClass().add("alertText");
		alertText.setTextAlignment(TextAlignment.CENTER);
		HBox alertBox = new HBox(10);
		alertBox.setAlignment(Pos.CENTER);
		alertBox.getChildren().add(alertText);
		qcPane.add(alertBox, 1, 16, 18, 1);
		
		TextArea outputField = new TextArea();
		outputField.setEditable(false);
		outputField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		qcPane.add(outputField, 1, 6, 18, 10);
		
		Button proceed = new Button("Proceed");
		proceed.setId("proceedButton");
		proceed.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		qcPane.add(proceed, 9, 17, 2, 2);
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
				}else if(!readDirectoryCompatible){
					alertText.setText("Directory containing reads is either empty or contains incompatible files");
				}else if(threads >= Runtime.getRuntime().availableProcessors()){
					alertText.setText("Thread count must be at most 1 less than total number of cores available");
				}else{
					alertText.setText("");
					FastQCCall task = new FastQCCall(outputField, threads);
					Thread t = new Thread(task);
					t.start();
				}
			}
		});
		
		Button back = new Button("Back");
		back.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		qcPane.add(back, 1, 17, 2, 2);
		back.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				WelcomePage page = new WelcomePage(primaryStage, FastQCLocation, BBToolsLocation, ProdigalLocation);
				page.run();
			}
		});
		
//		Button helpButton = new Button("Help");
//		helpButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
//		qcPane.add(helpButton, 17, 17, 2, 2);
//		helpButton.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent e) {
//				
//			}
//		});
		
		Scene scene = new Scene(qcPane, 800, 500);
		scene.getStylesheets().add("QCPage.css");
		primaryStage.setScene(scene);
//		qcPane.setGridLinesVisible(true);
		primaryStage.show();
	}
	
//This method checks to see if the submitted directory contains only files compatible with FastQC
//Aka fastq, sam, and bam files. 
	public boolean checkCompatible(File directory) {
		File[] readList = directory.listFiles();
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
			if(!match) {
				compatible = false;
				break;
			}
		}
		if(compatible) {
			return true;
		}
		return false;
	}
	
	private class FastQCCall extends Task<Void>{
		
		private TextArea outputField;
		private int threads;
		
		public FastQCCall(TextArea outputField, int threads) {
			this.outputField = outputField;
			this.threads = threads;
		}
		
		@Override
		protected Void call() {
			
			long start = System.nanoTime();
			
			Platform.runLater(() -> outputField.appendText("Threads: " + threads));
			
			ExecutorService pool = Executors.newFixedThreadPool(threads);
			File[] readList = readDirectory.listFiles();
			for(File entry : readList) {
				FastQCTask task = new FastQCTask(entry, outputField);
				pool.submit(task);
			}
			try {
				pool.shutdown();
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			MoveTask relocate = new MoveTask(readDirectory.listFiles());
			Thread rt = new Thread(relocate);
			rt.start();
			try {
				rt.join();
			}catch(Exception e) {
				e.printStackTrace();
			}
			Platform.runLater(() -> outputField.appendText("\nRelocating files to output directory"));
			
			long end = System.nanoTime();
			double elapsedTime = (end - start) / 1000000000.0;
			outputField.appendText("\nTime elapsed in seconds: " + elapsedTime);
			return null;
		}
	}
	
	private class FastQCTask extends Task<Void>{
		
		private TextArea outputField;
		private File file;
		private String line;
		private MessageConsumer consumer;
		
		public FastQCTask(File file, TextArea outputField) {
			this.outputField = outputField;
			this.file = file;
		}
			
		@Override
		protected Void call() {
			BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
			consumer = new MessageConsumer(messageQueue, this.outputField);
			String processCall = "java -Xmx500m -classpath .:./sam-1.103.jar:./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication";
			if(System.getProperties().getProperty("os.name").contains("Windows")) {
				processCall = "java -Xmx500m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication";
			}
			String fullProcessCall = processCall + " " + file.getAbsolutePath();
			try {
				Process readRun = Runtime.getRuntime().exec(fullProcessCall, null, FastQCLocation);
				BufferedReader stdout = new BufferedReader(new InputStreamReader(readRun.getErrorStream()));
				Platform.runLater(() -> consumer.start());
				while((this.line = stdout.readLine()) != null) {
					try {
						messageQueue.put(this.line);;
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				consumer.stop();
				stdout.close();
				try{
					/*
					 * THIS IS KEY. Without waiting for the cmd process to run, this will exit back to the thread pool, 
					 * and so the thread pool will schedule another process from the queue THINKING that this thread has 
					 * completed, since it returned already. That is why you can end up with far more threads active than
					 * are intended from the threadpool, because it does not consider the processes called by the Process call
					 * to be part of the threads active in the threadpool, UNLESS you specifically wait for them in the Task being
					 * run by the threadpool worker. 
					 */
					readRun.waitFor(); 
				}catch (InterruptedException x) {
					x.printStackTrace();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private class MoveTask extends Task<Void> {
		
		private File[] readFiles;
		
		public MoveTask(File[] readList) {
			this.readFiles = readList;
		}
		
		@Override
		protected Void call() {
			
			for(File item : readFiles) {
				String itemName = item.getName();
				String[] splitString = itemName.split("\\.");
				if(splitString[(splitString.length - 1)].equals("html") || splitString[(splitString.length - 1)].equals("zip")) {
					item.renameTo(new File(outputDirectory.getAbsolutePath() + "/" + item.getName()));
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


