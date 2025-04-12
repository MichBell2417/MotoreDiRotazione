package motoreDiRotazione.software.interfacciaGrafica;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PannelloConInfo extends Application{

    Label testo = new Label("Hello world!");
    Stage principale;

    @Override
    public void start(Stage finestra) throws Exception {
        principale = finestra;

        Button p = new Button("cliccami");

        BorderPane principale = new BorderPane();
        principale.setCenter(testo);
        principale.setTop(p);


        Scene scena = new Scene(principale, 300, 250);
        finestra.setTitle("Hello World!");
        finestra.setScene(scena);
        finestra.show();

        p.setOnAction( e-> apriFinestra() );
    }

    void apriFinestra() {
        showCustomDialog(principale);
    }

    private void showCustomDialog(Stage owner) {
        // Creazione dello Stage del dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL); // Impedisce interazioni con la finestra principale
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Dialog Personalizzato");

        // Testo e immagine
        Text message = new Text("Questo è il testo del messaggio, forse se molto lungo meglio textarea");

        // Layout
        VBox vbox = new VBox(10, new Label("immagine?"), message);

        // Chiusura al click, altrimenti si mette timer
        vbox.setOnMouseClicked(event -> dialog.close());

        // Scene del dialog
        Scene dialogScene = new Scene(vbox, 250, 150);
        // manca posizionamento sullo schermo
        dialog.setScene(dialogScene);
        dialog.show();
        System.out.println("here");
    }

    public static void main(String[] args) {
        launch(args);
    }
}