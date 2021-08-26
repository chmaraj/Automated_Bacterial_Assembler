package cfiassembly;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.lang.Process;
import java.lang.Runtime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

import utilityclasses.MessageConsumer;
import utilityclasses.Page;

public class QuickAssembly implements Page{
	
	private String separator = File.separator;
	private Stage primaryStage;
	private File readDirectory, outputDirectory, FastQCLocation, BBToolsLocation, ProdigalLocation;
	private ArrayList<String> sampleList = new ArrayList<String>();
	private boolean currentlyRunning = false;
	private ExecutorService mainPool = null;
	private ArrayList<Process> mainProcesses= new ArrayList<Process>();
	private int threads;
	private Text alertText = new Text("");
	private String javaCall;

	public QuickAssembly(Stage primaryStage, File FastQCLocation, File BBToolsLocation, File ProdigalLocation, String javaCall) {
		this.primaryStage = primaryStage;
		this.FastQCLocation = FastQCLocation;
		this.BBToolsLocation = BBToolsLocation;
		this.ProdigalLocation = ProdigalLocation;
		this.javaCall = javaCall;
	}
	
	public void run() {
		GridPane assemblyPane = new GridPane();
		Methods.fillGridPane(assemblyPane, 20, 20);
		
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
		readInput.setOnDragOver(new EventHandler<DragEvent>(){
			public void handle(DragEvent e) {
				if(e.getGestureSource() != readInput && e.getDragboard().hasFiles()) {
					e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
				e.consume();
			}
		});
		readInput.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent e) {
				Dragboard db = e.getDragboard();
				boolean success = false;
				if(db.hasFiles()) {
					List<File> listFiles = db.getFiles();
					readDirectory = listFiles.get(0);
					readInput.setText(readDirectory.getAbsolutePath());
					success = true;
				}
				e.setDropCompleted(success);
				e.consume();
			}
		});
		
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
		outputFileField.setOnDragOver(new EventHandler<DragEvent>(){
			public void handle(DragEvent e) {
				if(e.getGestureSource() != outputFileField && e.getDragboard().hasFiles()) {
					e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
				e.consume();
			}
		});
		outputFileField.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent e) {
				Dragboard db = e.getDragboard();
				boolean success = false;
				if(db.hasFiles()) {
					List<File> listFiles = db.getFiles();
					outputDirectory = listFiles.get(0);
					outputFileField.setText(outputDirectory.getAbsolutePath());
					success = true;
				}
				e.setDropCompleted(success);
				e.consume();
			}
		});
		
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
		
		Text threadPrompt = new Text("Input number of\nthreads to use(1, 2, etc.)");
		threadPrompt.setTextAlignment(TextAlignment.CENTER);
		HBox threadBox = new HBox(10);
		threadBox.setAlignment(Pos.CENTER);
		threadBox.getChildren().add(threadPrompt);
		assemblyPane.add(threadBox, 16, 1, 2, 2);
		
		ComboBox<Integer> threadField = new ComboBox<Integer>();
		threadField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		for(int i = 1; i < Runtime.getRuntime().availableProcessors(); i++) {
			threadField.getItems().add(i);
		}
		threadField.getSelectionModel().selectLast();
		assemblyPane.add(threadField, 16, 3, 3, 1);
		
		Button backButton = new Button("Back");
		backButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyPane.add(backButton, 1, 17, 2, 2);
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				WelcomePage page = new WelcomePage(primaryStage, FastQCLocation, BBToolsLocation, ProdigalLocation, javaCall);
				page.run();
			}
		});
		
		TextArea outputField = new TextArea();
		outputField.setEditable(false);
		assemblyPane.add(outputField, 1, 6, 18, 10);
		
		alertText = new Text();
		alertText.getStyleClass().add("alertText");
		alertText.setTextAlignment(TextAlignment.CENTER);
		HBox alertBox = new HBox(10);
		alertBox.setAlignment(Pos.CENTER);
		alertBox.getChildren().add(alertText);
		assemblyPane.add(alertBox, 1, 16, 18, 1);
		
		Button proceed = new Button("Proceed");
		proceed.setId("proceedButton");
		proceed.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyPane.add(proceed, 9, 17, 2, 2);
		proceed.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				threads = threadField.getSelectionModel().getSelectedItem();
				if(readDirectory == null || outputDirectory == null) {
					alertText.setText("Please enter both an input file directory and an output file directory");
					alertText.setTextAlignment(TextAlignment.CENTER);
				}else if(!Methods.checkReadsCompatible(readDirectory)){
					alertText.setText("Directory containing reads is either empty or contains incompatible files");
					alertText.setTextAlignment(TextAlignment.CENTER);
				}else{
					alertText.setText("");
					RunPipeline run = new RunPipeline(outputField, threads);
					Thread t = new Thread(run);
					t.setDaemon(true);
					t.start();
					currentlyRunning = true;
				}
			}
		});
		
		Scene scene = new Scene(assemblyPane, 800, 500);
//		scene.getStylesheets().add(QuickAssembly.class.getResource("resources/QCPage.css").toString()); // for running in Eclipse
		scene.getStylesheets().add("src/resources/QCPage.css");
		primaryStage.setScene(scene);
//		assemblyPane.setGridLinesVisible(true);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent e) {
				if(currentlyRunning) {
					if(JOptionPane.showConfirmDialog(null, 
													"Exiting now will cause the pipeline to exit early, likely corrupting any data currently being output.\n" +
													"Are you sure you want to exit now?",
													"Exit CFIAssembly",
													JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
						try{
							FileWriter logFile = new FileWriter(outputDirectory.getAbsolutePath() + separator + "log.txt", true);
							String[] outputLines = outputField.getText().split("\n");
							for(String line : outputLines) {
								logFile.write(line + "\n");
							}
							logFile.close();
						}catch(IOException exception) {
							exception.printStackTrace();
						}
						if(mainPool != null && !mainPool.isTerminated()) {
							mainPool.shutdownNow();
						}
						for(Process p : mainProcesses) {
							if(p.isAlive()) {
								p.destroyForcibly();
							}
						}
						Platform.exit();
						System.exit(0);
					}else {
						e.consume();
					}
				}else {
					if(!outputField.getText().isEmpty()) {
						try{
							FileWriter logFile = new FileWriter(outputDirectory.getAbsolutePath() + separator + "log.txt", true);
							String[] outputLines = outputField.getText().split("\n");
							for(String line : outputLines) {
								logFile.write(line + "\n");
							}
							logFile.close();
						}catch(IOException exception) {
							exception.printStackTrace();
						}
					}
					Platform.exit();
					System.exit(0);
				}
			}
		});
		primaryStage.setTitle("Automated Bacterial Assembler");
		primaryStage.show();
	}
	
	public void createSampleList() {
		File[] readList = readDirectory.listFiles();
		for(File entry : readList) {
			String sampleID = entry.getName().replaceAll("_R1_.*", "");
			sampleID = sampleID.replaceAll("_R2_.*", "");
			sampleID = sampleID.replaceAll("_R1\\..*", "");
			sampleID = sampleID.replaceAll("_R2\\..*", "");
			if(!sampleList.contains(sampleID)) {
				sampleList.add(sampleID);
			}
		}
	}
	
	private class RunPipeline extends Task<Void>{
		
		private TextArea outputField;
		private int threads;
		
		public RunPipeline(TextArea outputField, int threads) {
			this.outputField = outputField;
			this.threads = threads;
		}
		
		public Void call() {
			createSampleList();
			TadpoleTask task = new TadpoleTask(outputField, threads);
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
			try {
				t.join();
			}catch(InterruptedException exception) {
				exception.printStackTrace();
			}
			Methods.logMessage(outputField, "Assembly complete");
			if(ProdigalLocation != null) {
				mainPool = Executors.newFixedThreadPool(threads);
				for(File entry : outputDirectory.listFiles()) {
					if(!entry.isDirectory() && entry.getName().endsWith(".fasta")) {
						ProdigalTask task2 = new ProdigalTask(outputField, entry);
						mainPool.submit(task2);
					}
				}
				try {
					mainPool.shutdown();
					mainPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				}catch(Exception exception) {
					exception.printStackTrace();
				}
			}
			Methods.logMessage(outputField, "Gene Prediction complete");
			try{
				FileWriter logFile = new FileWriter(outputDirectory.getAbsolutePath() + separator + "log.txt");
				logFile.write(outputField.getText());
				logFile.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			Methods.makeQALog(new File(outputDirectory.getAbsolutePath() + separator + "QA_log.txt"), WelcomePage.version,
					outputDirectory, readDirectory);
			currentlyRunning = false;
			return null;
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
			File[] readList = readDirectory.listFiles();
			for(String item : sampleList) {
				String in1 = "", in2 = "";
				for(File file : readList) {
					if(file.getName().contains(item) && file.getName().contains("_R1")){
						in1 = file.getAbsolutePath();
					}else if(file.getName().contains(item) && file.getName().contains("_R2")) {
						in2 = file.getAbsolutePath();
					}
				}
				String out = outputDirectory.getAbsolutePath() + separator + item + ".fasta";
				String[] fullProcessCall = new String[] {javaCall, "-ea", "-Xmx" + memJava, "-cp", "./current", "assemble.Tadpole",
						"in1=" + in1, "in2=" + in2, "out=" + out, "threads=" + Integer.toString(this.threads), "k=124", "showstats=t"};
				try {
					Process assemblyRun = new ProcessBuilder(fullProcessCall).directory(BBToolsLocation).start();
					mainProcesses.add(assemblyRun);
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
	
	public class ProdigalTask extends Task<Void> {
		
		private TextArea outputField;
		private File entry;
		private String line;
		private MessageConsumer consumer;
		
		public ProdigalTask(TextArea outputField, File entry) {
			this.outputField = outputField;
			this.entry = entry;
		}
		
		public Void call() {
			BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
			consumer = new MessageConsumer(messageQueue, this.outputField);
			String processCall = "";
			if(System.getProperties().getProperty("os.name").contains("Windows")){
				processCall = ProdigalLocation.getAbsolutePath() + separator + "prodigal.windows.exe";
			}else {
				processCall = "./prodigal.linux";
			}
			String i = entry.getAbsolutePath();
			File output = new File(outputDirectory.getAbsolutePath() + separator + "Gene_Prediction");
			output.mkdir();
			String entryName = entry.getName().split("\\.")[0];
			String o = output.getAbsolutePath() + separator + entryName + "_results.gbk";
			String transFile = output.getAbsolutePath() + separator + entryName + "_protein.gbk";
			String nucFile = output.getAbsolutePath() + separator + entryName + "_nucleotide.gbk";
			String[] fullProcessCall = new String[] {processCall, "-i", i, "-o", o, "-a", transFile, "-d", nucFile};
			try {
				Process run = new ProcessBuilder(fullProcessCall).directory(ProdigalLocation).start();
				mainProcesses.add(run);
				BufferedReader error = new BufferedReader(new InputStreamReader(run.getErrorStream()));
				Platform.runLater(() -> consumer.start());
				while((this.line = error.readLine()) != null) {
					try {
						messageQueue.put(this.line);
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				consumer.stop();
				error.close();
				try {
					run.waitFor();
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
				
			}catch(IOException e) {
				e.printStackTrace();
			}
			mainProcesses.clear();
			return null;
		}
		
	}
}
