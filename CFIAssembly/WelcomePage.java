package bacterialgenomeassembly;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import java.util.List;
import java.util.Collections;
import java.io.File;

/* This application is intended for use on company laptops for researchers who want to
 * do their own sequence analysis, or who want to do read QC or a quick and dirty assembly.
 */
public class WelcomePage {
	
	private Stage primaryStage;
	private String version = "v0.102";
	private File FastQCLocation, BBToolsLocation;
	
	public WelcomePage(Stage primaryStage, File FastQCLocation, File BBToolsLocation) {
		this.primaryStage = primaryStage;
		this.FastQCLocation = FastQCLocation;
		this.BBToolsLocation = BBToolsLocation;
	}
	
	public void run() {
		GridPane welcomePane = new GridPane();
		ColumnConstraints cC = new ColumnConstraints();
		cC.setPercentWidth(5);
		List<ColumnConstraints> colCopies = Collections.nCopies(20, cC);
		welcomePane.getColumnConstraints().addAll(colCopies);
		RowConstraints rC = new RowConstraints();
		rC.setPercentHeight(5);
		List<RowConstraints> rowCopies = Collections.nCopies(20, rC);
		welcomePane.getRowConstraints().addAll(rowCopies);
		
		Label background = new Label();
		background.setId("welcomeBanner");
		background.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		welcomePane.add(background, 0, 0, 20, 4);
		
		Text title = new Text("CFIAssembly");
		title.setId("welcomePageTitle");
		HBox titleBox = new HBox(10);
		titleBox.getChildren().add(title);
		titleBox.setAlignment(Pos.CENTER);
		welcomePane.add(titleBox, 1, 2);
		
		Text version = new Text(this.version);
		version.setId("version");
		HBox versionBox = new HBox(10);
		versionBox.setAlignment(Pos.CENTER);
		versionBox.getChildren().add(version);
		welcomePane.add(versionBox, 18, 19, 2, 1);
		
		//Keep Button Generation concise
		ButtonHandle[] buttons = new ButtonHandle[] {
				new ButtonHandle("Read QC") {public void run() {QCPage page = new QCPage(primaryStage, FastQCLocation, BBToolsLocation); 
																page.run();}},
				new ButtonHandle("Quick Assembly") {public void run() {QuickAssembly page = new QuickAssembly(primaryStage,
																FastQCLocation, BBToolsLocation); page.run();}},
				new ButtonHandle("Assemble Genome") {public void run() {AssemblyPage page = new AssemblyPage(primaryStage,
																FastQCLocation, BBToolsLocation); page.run();}},
				new ButtonHandle("Help") {public void run() {HelpPage page = new HelpPage(primaryStage, FastQCLocation, BBToolsLocation); page.run();}}
		};
		
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				ButtonHandle handle = buttons[(i * 2) + j];
				Button button = new Button(handle.getName());
				button.getStyleClass().add("subtitle");
				button.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
				welcomePane.add(button, (j*9) + 2, (i*6) + 6, 7, 5);
				button.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						handle.run();
					}
				});
			}
		}
		
		Scene scene = new Scene(welcomePane, 800, 500);
		scene.getStylesheets().add("WelcomePage.css");
		primaryStage.setScene(scene);
//		welcomePane.setGridLinesVisible(true);
		primaryStage.show();
	}
	
	private abstract class ButtonHandle {
		
		private String name;
		
		public ButtonHandle(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public abstract void run();
	}
}
