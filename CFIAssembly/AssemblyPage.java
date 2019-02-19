package bacterialgenomeassembly;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Process;
import java.lang.Runtime;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AssemblyPage implements Page{

	private String separator = "/";
	
	private Stage primaryStage;
	private File readDirectory, outputDirectory, FastQCLocation, BBToolsLocation, ProdigalLocation;
	private File logs, qc, fastqc, trimmed, merged, corrected, assembly, fastqcRaw;
	private File[] readList;
	private ArrayList<String> sampleList = new ArrayList<String>();
	private int threads = Runtime.getRuntime().availableProcessors();
//	private String[] options, userDefined;
	
	public AssemblyPage(Stage primaryStage, File FastQCLocation, File BBToolsLocation, File ProdigalLocation) {
		this.primaryStage = primaryStage;
		this.FastQCLocation = FastQCLocation;
		this.BBToolsLocation = BBToolsLocation;
		this.ProdigalLocation = ProdigalLocation;
		if(System.getProperties().getProperty("os.name").contains("Windows")) {
			this.separator = "\\";
		}else {
			this.separator = "/";
		}
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
		
		Text disclaimerText = new Text("Disclaimer: this pipeline will create a variety of directories within the designated output directory. \n"
				+ "Also please note that this pipeline will run for an extended period, so try to run overnight if possible.");
		disclaimerText.setId("assembly_disclaimer");
		disclaimerText.setTextAlignment(TextAlignment.CENTER);	
		HBox disclaimerBox = new HBox(10);
		disclaimerBox.setAlignment(Pos.CENTER);
		disclaimerBox.getChildren().add(disclaimerText);
		assemblyPane.add(disclaimerBox, 0, 0, 20, 1);
		
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
				readList = readDirectory.listFiles();
			}
		});
		
		Text outputPrompt = new Text("Please enter a directory which will house the output directories");
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
		threadField.setText(Integer.toString(threads - 1));
		assemblyPane.add(threadField, 16, 3, 3, 2);
		
		Text alertText = new Text();
		alertText.getStyleClass().add("alertText");
		HBox alertBox = new HBox(10);
		alertBox.setAlignment(Pos.CENTER);
		alertBox.getChildren().add(alertText);
		assemblyPane.add(alertBox, 1, 16, 18, 1);
		
		Button backButton = new Button("Back");
		backButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyPane.add(backButton, 1, 17, 2, 2);
		backButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				WelcomePage page = new WelcomePage(primaryStage, FastQCLocation, BBToolsLocation, ProdigalLocation);
				page.run();
			}
		});
		
		TextArea outputField = new TextArea();
		outputField.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		outputField.setEditable(false);
		assemblyPane.add(outputField, 1, 6, 18, 10);	
		
		Button proceed = new Button("Proceed");
		proceed.setId("proceedButton");
		proceed.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		assemblyPane.add(proceed, 9, 17, 2, 2);
		proceed.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
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
				}else {
					alertText.setText("");
					RunAssemblyPipeline task = new RunAssemblyPipeline(outputField);
					Thread t = new Thread(task);
					t.start();
				}
			}
		});	
		
//		Button helpButton = new Button("Help");
//		helpButton.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
//		assemblyPane.add(helpButton, 17, 17, 2, 2);
//		helpButton.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent e) {
//				
//			}
//		});
		
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
	
	public class RunAssemblyPipeline extends Task<Void> {
		
		private TextArea outputField;
		
		public RunAssemblyPipeline(TextArea outputField) {
			this.outputField = outputField;
		}
		
		public Void call() {
			createBaseDirectories();
			logMessage(outputField,"Finished creating base directories");
			if(FastQCLocation != null) {
				runFastQCTask(outputField);
				logMessage(outputField,"Finished FastQC task");
			}
			populateSampleList();
			logMessage(outputField,"Finished creating sample list");
			runFilterByTileTask(outputField);
			logMessage(outputField,"Finished filtering by tile");
			runQualityFilterTask(outputField);
			logMessage(outputField,"Finished filtering by quality");
			runRemoveArtifactsTask(outputField);
			logMessage(outputField,"Finished removing artifacts");
			runBBMergeTask(outputField);
			logMessage(outputField,"Finished merging reads");
			runClumpifyTask(outputField);
			logMessage(outputField,"Finished clumping");
			runTadpoleCorrectTask(outputField);
			logMessage(outputField,"Finished correcting reads");
			runMergeOverlapTask(outputField);
			logMessage(outputField,"Finished merging overlapping reads");
			if(FastQCLocation != null) {
				runMergeFastQCTask(outputField);
				logMessage(outputField,"Finished FastQC on merged reads");
			}
			runTadpoleAssemblyTask(outputField);
			logMessage(outputField,"Finished assembling contigs");
			if(ProdigalLocation != null) {
				runProdigalTask(outputField);
				logMessage(outputField, "Finished running Prodigal");
			}
			return null;
		}
	}
	
	public void createBaseDirectories() {
		logs = new File(outputDirectory.getAbsolutePath() + separator + "logs");
		logs.mkdirs();
		qc = new File(outputDirectory.getAbsolutePath() + separator + "QC");
		qc.mkdirs();
		fastqc = new File(qc.getAbsolutePath() + separator + "fastqc");
		fastqc.mkdirs();
		fastqcRaw = new File(fastqc.getAbsolutePath() + separator + "raw");
		fastqcRaw.mkdirs();
		trimmed = new File(outputDirectory.getAbsolutePath() + separator + "trimmed");
		trimmed.mkdirs();
		merged = new File(outputDirectory.getAbsolutePath() + separator + "merged");
		merged.mkdirs();
		corrected = new File(outputDirectory.getAbsolutePath() + separator + "corrected");
		corrected.mkdirs();
		assembly = new File(outputDirectory.getAbsolutePath() + separator + "assembly");
		assembly.mkdirs();
	}
	
	public void populateSampleList() {
		readList = readDirectory.listFiles();
		for(File entry : readList) {
			String entryName = entry.getName();
			String sampleID = entryName.replaceAll("_R1.*", "");
			sampleID = sampleID.replaceAll("_R2.*", "");
			if(sampleList.isEmpty() || !sampleList.contains(sampleID)) {
				sampleList.add(sampleID);
			}
		}
	}
	
	public void runFastQCTask(TextArea outputField) {
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		for(File entry : readList) {
			FastQCTask qcTask = new FastQCTask(outputField, entry);
			pool.submit(qcTask);
		}
		try {
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		logMessage(outputField,"Beginning Relocation");
		RelocateTask task = new RelocateTask();
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
		try {
			t.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runFilterByTileTask(TextArea outputField) {
		FilterByTileTask filterTask = new FilterByTileTask(outputField);
		Thread t2 = new Thread(filterTask);
		t2.setDaemon(true);
		t2.start();
		try {
			t2.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runQualityFilterTask(TextArea outputField) {
		QualityFilterTask qualityTask = new QualityFilterTask(outputField);
		Thread t3 = new Thread(qualityTask);
		t3.setDaemon(true);
		t3.start();
		try {
			t3.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runRemoveArtifactsTask(TextArea outputField) {
		RemoveArtifactsTask removeTask = new RemoveArtifactsTask(outputField);
		Thread t4 = new Thread(removeTask);
		t4.setDaemon(true);
		t4.start();
		try {
			t4.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runBBMergeTask(TextArea outputField) {
		BBMergeTask mergeTask = new BBMergeTask(outputField);
		Thread t5 = new Thread(mergeTask);
		t5.setDaemon(true);
		t5.start();
		try {
			t5.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runClumpifyTask(TextArea outputField) {
		ClumpifyTask clumpifyTask = new ClumpifyTask(outputField);
		Thread t6 = new Thread(clumpifyTask);
		t6.setDaemon(true);
		t6.start();
		try {
			t6.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runTadpoleCorrectTask(TextArea outputField) {
		TadpoleCorrectTask tadpoleCorrect = new TadpoleCorrectTask(outputField);
		Thread t7 = new Thread(tadpoleCorrect);
		t7.setDaemon(true);
		t7.start();
		try {
			t7.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runMergeOverlapTask(TextArea outputField) {
		MergeOverlapTask mergeOverlapTask = new MergeOverlapTask(outputField);
		Thread t8 = new Thread(mergeOverlapTask);
		t8.setDaemon(true);
		t8.start();
		try {
			t8.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runMergeFastQCTask(TextArea outputField) {
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		for(String entry : sampleList) {
			MergeFastQCTask mergeFastQC = new MergeFastQCTask(outputField, entry);
			pool.submit(mergeFastQC);
		}
		try {
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		logMessage(outputField,"Relocating merged reads");
		RelocateMergedTask task = new RelocateMergedTask();
		Thread t = new Thread(task);
		t.start();
		try {
			t.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runTadpoleAssemblyTask(TextArea outputField) {
		TadpoleAssemblyTask assemblyTask = new TadpoleAssemblyTask(outputField);
		Thread t10 = new Thread(assemblyTask);
		t10.setDaemon(true);
		t10.start();
		try {
			t10.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void runProdigalTask(TextArea outputField) {
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		for(File entry : assembly.listFiles()) {
			ProdigalTask task = new ProdigalTask(outputField, entry);
			pool.submit(task);
		}
		try {
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private class FastQCTask extends Task<Void>{
		
		private TextArea outputField;
		private File entry;
		private String line;
		private MessageConsumer consumer;
		
		public FastQCTask(TextArea outputField, File entry) {
			this.outputField = outputField;
			this.entry = entry;
		}
			
		@Override
		protected Void call() {
			BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
			this.consumer = new MessageConsumer(messageQueue, this.outputField);
			String processCall = "java -Xmx500m -classpath .:./sam-1.103.jar:./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication";
			if(System.getProperties().getProperty("os.name").contains("Windows")) {
				processCall = "java -Xmx500m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication";
			}
			String fullProcessCall = processCall + " " + entry.getAbsolutePath();
			try {
				Process readRun = Runtime.getRuntime().exec(fullProcessCall, null, FastQCLocation);
				BufferedReader stdout = new BufferedReader(new InputStreamReader(readRun.getErrorStream()));
				Platform.runLater(() -> this.consumer.start());
				while((this.line = stdout.readLine()) != null) {
					try{
						messageQueue.put(this.line);
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				this.consumer.stop();
				stdout.close();
				try{
					readRun.waitFor();
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private class RelocateTask extends Task<Void>{
		
		public RelocateTask() {
			
		}
		
		@Override
		protected Void call() {
			readList = readDirectory.listFiles();
			for(File item : readList) {
				String itemName = item.getName();
				String[] splitString = itemName.split("\\.");
				if(splitString[(splitString.length - 1)].equals("html") || splitString[(splitString.length - 1)].equals("zip")) {
					item.renameTo(new File(fastqcRaw.getAbsolutePath() + separator + item.getName()));
				}	
			}
			return null;
		}
	}
	
	private class RelocateMergedTask extends Task<Void>{
		
		public RelocateMergedTask() {
			
		}
		
		@Override
		protected Void call() {
			for(String entry : sampleList) {
				File mergedFastQCDirectory = new File(fastqc.getAbsolutePath() + separator + "merged" + separator + entry);
				mergedFastQCDirectory.mkdirs();
				File mixedReadsDirectory = new File(merged.getAbsolutePath() + separator + entry);
				File[] mixedReads = mixedReadsDirectory.listFiles();
				for(File item : mixedReads) {
					String itemName = item.getName();
					String[] splitString = itemName.split("\\.");
					if(splitString[splitString.length - 1].equals("html") || splitString[splitString.length - 1].equals("zip")) {
						item.renameTo(new File(mergedFastQCDirectory.getAbsolutePath() + separator + item.getName()));
					}
				}
			}
			return null;
		}
	}
	
	private class FilterByTileTask extends Task<Void> {
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public FilterByTileTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				File entryDirectory = new File(trimmed.getAbsolutePath() + separator + entry);
				entryDirectory.mkdirs();
				String in1 = "", in2 = "";
				for(File item : readDirectory.listFiles()) {
					if(item.getName().contains("_R1") && item.getName().contains(entry)) {
						in1 = item.getAbsolutePath();
					}else if(item.getName().contains("_R2") && item.getName().contains(entry)) {
						in2 = item.getAbsolutePath();
					}
				}
				String out1 = entryDirectory.getAbsolutePath() + separator + entry + "_Filtered_1P.fastq.gz";
				String out2 = entryDirectory.getAbsolutePath() + separator + entry + "_Filtered_2P.fastq.gz";
				String options = " in1=" + in1 + " in2=" + in2 + " out1=" + out1 + " out2=" + out2 + " threads=" + threads + " ziplevel=9";
				String runFilterByTile = "java -ea -Xmx7g -cp ./current hiseq.AnalyzeFlowCell" + options;
				try {
					Process runFilter = Runtime.getRuntime().exec(runFilterByTile, null, BBToolsLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(runFilter.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try {
						runFilter.waitFor();
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
	
	private class QualityFilterTask extends Task<Void>{
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public QualityFilterTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				String in1 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Filtered_1P.fastq.gz";
				String in2 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Filtered_2P.fastq.gz";
				String out1 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Trimmed_1P.fastq.gz";
				String out2 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Trimmed_2P.fastq.gz";
				String ref = BBToolsLocation.getAbsolutePath() + separator + "resources" + separator + "adapters.fa";
				String options = " in1=" + in1 + " in2=" + in2 + " out1=" + out1 + " out2=" + out2 + 
						" ktrim=r k=23 mink=11 hdist=1 tbo tpe qtrim=lr trimq=10 minlen=64 ziplevel=9 ordered=t threads=" + threads + " ref=" + ref;
				String runQualityFilter = "java -ea -Xmx7g -cp ./current jgi.BBDuk" + options;
				logMessage(outputField, runQualityFilter);
				try {
					Process runFilter = Runtime.getRuntime().exec(runQualityFilter, null, BBToolsLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(runFilter.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try {
						runFilter.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
				File in1File = new File(in1);
				File in2File = new File(in2);
				in1File.delete();
				in2File.delete();
			}			
			return null;
		}
	}

	private class RemoveArtifactsTask extends Task<Void>{
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public RemoveArtifactsTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				String in1 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Trimmed_1P.fastq.gz";
				String in2 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Trimmed_2P.fastq.gz";
				String out1 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Cleaned_1P.fastq.gz";
				String out2 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Cleaned_2P.fastq.gz";
				String ref = BBToolsLocation.getAbsolutePath() + separator + "resources" + separator + "phix174_ill.ref.fa.gz";
				String options = " in1=" + in1 + " in2=" + in2 + " out1=" + out1 + " out2=" + out2 + 
						" ziplevel=9 k=31 ordered=t threads=" + threads + " ref=" + ref;
				String runRemoveArtifacts = "java -ea -Xmx7g -cp ./current jgi.BBDuk" + options;
				try {
					Process runRemove = Runtime.getRuntime().exec(runRemoveArtifacts, null, BBToolsLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(runRemove.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try {
						runRemove.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
				File in1File = new File(in1);
				File in2File = new File(in2);
				in1File.delete();
				in2File.delete();
			}
			return null;
		}
	}
	
	private class BBMergeTask extends Task<Void>{
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public BBMergeTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			File ihists = new File(logs.getAbsolutePath() + separator + "correction_step1");
			ihists.mkdirs();
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				File entryDirectory = new File(corrected.getAbsolutePath() + separator + entry);
				entryDirectory.mkdirs();
				String in1 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Cleaned_1P.fastq.gz";
				String in2 = trimmed.getAbsolutePath() + separator + entry + separator + entry + "_Cleaned_2P.fastq.gz";
				String out1 = entryDirectory.getAbsolutePath() + separator + entry + "_Cor1_1P.fastq.gz";
				String out2 = entryDirectory.getAbsolutePath() + separator + entry + "_Cor1_2P.fastq.gz";
				String options = " in1=" + in1 + " in2=" + in2 + " out1=" + out1 + " out2=" + out2 + 
						" ecco=t mix=t verystrict=t ordered=t ziplevel=9 threads=" + threads + " ordered ihist=" + ihists.getAbsolutePath() +
						entry + "_ihist_corr_merge.txt";
				String runMergeCall = "java -ea -Xmx7g -cp ./current jgi.BBMerge" + options;
				try {
					Process runMerge = Runtime.getRuntime().exec(runMergeCall, null, BBToolsLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(runMerge.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try {
						runMerge.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
				File in1File = new File(in1);
				File in2File = new File(in2);
				in1File.delete();
				in2File.delete();
			}
			return null;
		}
	}
	
	private class ClumpifyTask extends Task<Void>{
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public ClumpifyTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				String in1 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor1_1P.fastq.gz";
				String in2 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor1_2P.fastq.gz";
				String out1 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor2_1P.fastq.gz";
				String out2 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor2_2P.fastq.gz";
				String options = " in1=" + in1 + " in2=" + in2 + " out1=" + out1 + " out2=" + out2 + 
						" ecc=t passes=4 reorder=t ziplevel=9 threads=" + threads;
				String runClumpifyCall = "java -ea -Xmx7g -cp ./current clump.Clumpify" + options;
				try {
					Process runClumpify = Runtime.getRuntime().exec(runClumpifyCall, null, BBToolsLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(runClumpify.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try {
						runClumpify.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
				File in1File = new File(in1);
				File in2File = new File(in2);
				in1File.delete();
				in2File.delete();
			}
			return null;
		}
	}
	
	private class TadpoleCorrectTask extends Task<Void>{
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public TadpoleCorrectTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				String in1 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor2_1P.fastq.gz";
				String in2 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor2_2P.fastq.gz";
				String out1 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor3_1P.fastq.gz";
				String out2 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor3_2P.fastq.gz";
				String options = " in1=" + in1 + " in2=" + in2 + " out1=" + out1 + " out2=" + out2 + 
						" ecc=t k=62 ziplevel=9 mode=correct threads=" + threads;
				String tadpoleCorrectCall = "java -ea -Xmx7g -cp ./current assemble.Tadpole" + options;
				try {
					Process runTadpoleCorrect = Runtime.getRuntime().exec(tadpoleCorrectCall, null, BBToolsLocation);
					BufferedReader reader = new BufferedReader(new InputStreamReader(runTadpoleCorrect.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = reader.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					reader.close();
					try {
						runTadpoleCorrect.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
				File in1File = new File(in1);
				File in2File = new File(in2);
				in1File.delete();
				in2File.delete();
			}
			return null;
		}
	}
	
	private class MergeOverlapTask extends Task<Void>{
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public MergeOverlapTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			File mergeIHists = new File(logs.getAbsolutePath() + separator + "merging");
			mergeIHists.mkdirs();
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				File entryDirectory = new File(merged.getAbsolutePath() + separator + entry);
				entryDirectory.mkdirs();
				String in1 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor3_1P.fastq.gz";
				String in2 = corrected.getAbsolutePath() + separator + entry + separator + entry + "_Cor3_2P.fastq.gz";
				String out = entryDirectory.getAbsolutePath() + separator + entry + "_merged.fastq.gz";
				String outu = entryDirectory.getAbsolutePath() + separator + entry + "_unmerged_1P.fastq.gz";
				String outu2 = entryDirectory.getAbsolutePath() + separator + entry + "_unmerged_2P.fastq.gz";
				String ihists = logs.getAbsolutePath() + separator + "merging" + separator + entry + "_ihist_merge.txt";
				String options = " in1=" + in1 + " in2=" + in2 + " out=" + out + " outu=" + outu + " outu2=" + outu2 +
						" strict=t k=93 extend2=80 rem=t ordered=t ziplevel=9 threads=" + threads + " ihist=" + ihists;
				String mergeOverlapCall = "java -ea -Xmx7g -cp ./current jgi.BBMerge" + options;
				try {
					Process runMergeOverlap = Runtime.getRuntime().exec(mergeOverlapCall, null, BBToolsLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(runMergeOverlap.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try {
						runMergeOverlap.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
				File in1File = new File(in1);
				File in2File = new File(in2);
				in1File.delete();
				in2File.delete();
			}
			return null;
		}
	}
	
	private class MergeFastQCTask extends Task<Void>{
		
		private TextArea outputField;
		private String entry;
		private String line;
		private MessageConsumer consumer;
		
		public MergeFastQCTask(TextArea outputField, String entry) {
			this.outputField = outputField;
			this.entry = entry;
		}
		
		@Override
		protected Void call() {
			BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
			this.consumer = new MessageConsumer(messageQueue, this.outputField);
			String processCall = "java -Xmx500m -classpath .:./sam-1.103.jar:./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication";
			if(System.getProperties().getProperty("os.name").contains("Windows")) {
				processCall = "java -Xmx500m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication";
			}
			File directory = new File(merged.getAbsolutePath() + separator + entry);
			for(File item : directory.listFiles()) {
				String fullProcessCall = processCall + " " + item.getAbsolutePath();
				try {
					Process readRun = Runtime.getRuntime().exec(fullProcessCall, null, FastQCLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(readRun.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try{
						readRun.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			return null;
		}
	}
	
	private class TadpoleAssemblyTask extends Task<Void>{
		
		private TextArea outputField;
		private String line;
		private MessageConsumer consumer;
		
		public TadpoleAssemblyTask(TextArea outputField) {
			this.outputField = outputField;
		}
		
		@Override
		protected Void call() {
			for(String entry : sampleList) {
				BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
				this.consumer = new MessageConsumer(messageQueue, this.outputField);
				String in1 = merged.getAbsolutePath() + separator + entry + separator + entry + "_unmerged_1P.fastq.gz";
				String in2 = merged.getAbsolutePath() + separator + entry + separator + entry + "_unmerged_2P.fastq.gz";
				String extra = merged.getAbsolutePath() + separator + entry + separator + entry + "_merged.fastq.gz";
				String out = assembly.getAbsolutePath() + separator + entry + ".fasta";
				String options = " in1=" + in1 + " in2=" + in2 + " extra=" + extra + " out=" + out + " threads=" + threads + " k=124";
				String tadpoleAssemblyCall = "java -ea -Xmx7g -cp ./current assemble.Tadpole" + options;
				try {
					Process runTadpoleAssembly = Runtime.getRuntime().exec(tadpoleAssemblyCall, null, BBToolsLocation);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(runTadpoleAssembly.getErrorStream()));
					Platform.runLater(() -> this.consumer.start());
					while((this.line = stdout.readLine()) != null) {
						try{
							messageQueue.put(this.line);
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.consumer.stop();
					stdout.close();
					try {
						runTadpoleAssembly.waitFor();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
				File in1File = new File(in1);
				File in2File = new File(in2);
				File extraFile = new File(extra);
				in1File.delete();
				in2File.delete();
				extraFile.delete();
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
			String processCall = "prodigal.windows.exe";
			String i = entry.getAbsolutePath();
			File output = new File(outputDirectory.getAbsolutePath() + "/Gene_Prediction");
			String o = output.getAbsolutePath() + separator + entry.getName() + "_results.gbk";
			String transFile = output.getAbsolutePath() + separator + entry.getName() + "_protein.gbk";
			String nucFile = output.getAbsolutePath() + separator + entry.getName() + "_nucleotide.gbk";
			String fullProcessCall = processCall + " -i" + i + " -o" + o + " -a" + transFile + " -d" + nucFile;
			try {
				Process run = Runtime.getRuntime().exec(fullProcessCall, null, ProdigalLocation);
				BufferedReader stdout = new BufferedReader(new InputStreamReader(run.getErrorStream()));
				Platform.runLater(() -> consumer.start());
				while((this.line = stdout.readLine()) != null) {
					try {
						messageQueue.put(this.line);
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				consumer.stop();
				stdout.close();
				try {
					run.waitFor();
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	public void logMessage(TextArea outputField, String msg) {
		Platform.runLater(() -> outputField.appendText("\n" + msg + "\n"));
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
