package motoreDiRotazione.software.interfacciaGrafica;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ActualStatus {
	boolean rotationStatus;
	int speedValue;
	int rotationGrades;
	public ActualStatus(boolean rotationValue, int speedValue, int rotationGrades) {
		setProperties(rotationValue, speedValue, rotationGrades);
	}
	public void setProperties(boolean rotationValue, int speedValueIn, int rotationGradesIn) {
		rotationGrades=rotationGradesIn;
		speedValue=speedValueIn;
		rotationStatus=rotationValue;
	}
	public VBox getView() {
		VBox box=new VBox(2);
		box.getChildren().add(new Label("STATO ATTUALE"));
		box.getChildren().add(new Label("Rotazione: "+ (rotationStatus ? "ON" : "OFF")));
		box.getChildren().add(new Label("Rotazione °: "+ rotationGrades));
		box.getChildren().add(new Label("Velocità: "+speedValue));
		return box;
	}
}
