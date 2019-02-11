package bacterialgenomeassembly;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.Collections;

public class AdvancedOptions {
	
	private Stage primaryStage;
//	private String[] options, userDefined;
	private Page callingPage;

	public AdvancedOptions(Page callingPage) {
		Stage primaryStage = new Stage();
		this.callingPage = callingPage;
		this.primaryStage = primaryStage;
//		this.options = callingPage.getOptions();
//		this.userDefined = callingPage.getUserDefined();
	}
	
	public void defineOptions() {
		GridPane pane = new GridPane();
		ColumnConstraints cC = new ColumnConstraints();
		cC.setPercentWidth(5);
		List<ColumnConstraints> colCopies = Collections.nCopies(20, cC);
		pane.getColumnConstraints().addAll(colCopies);
		RowConstraints rC = new RowConstraints();
		rC.setPercentHeight(5);
		List<RowConstraints> rowCopies = Collections.nCopies(20, rC);
		pane.getRowConstraints().addAll(rowCopies);
		
		Button back = new Button("Back");
		back.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		pane.add(back, 1, 17, 3, 2);
		back.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
//				callingPage.setOptions(options);
//				callingPage.setUserDefined(userDefined);
				primaryStage.close();
			}
		});
		
		Scene scene = new Scene(pane, 400, 300);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
