package faretto.testaRotanteAsseX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.TabPane.TabClosingPolicy;

import com.fazecast.jSerialComm.SerialPort;

public class ComunicaInSeriale extends Application {

	Slider sGradiDiRotazione = new Slider(45, 360, 45);
	Slider sVelocità = new Slider(1, 10, 5);
	ComboBox<String> modelli = new ComboBox<>();
	ComboBox<String> porteCOM = new ComboBox<>();

	SerialPort nomePortaCOM[]; // tutte le porte COM disponibili
	SerialPort portaSeriale; // porta in uso
	boolean portaPresente=false;

	Label etichettaFeedback = new Label();
	Label etichettaFeedbackReg = new Label();
	Label etichettaFeedbackSettings = new Label();
	
	int vettoreModelli[][]= {{5, 45}, {5, 90}, {5, 180}, {5, 360}};
	
	byte[] messageToSend= new byte[4];

	Scene scena;
	
	public void start(Stage finestra) {

		// voglio che mostri le tacche
		sGradiDiRotazione.setShowTickMarks(true);
		// voglio che mostri le etichette
		sGradiDiRotazione.setShowTickLabels(true);
		// quanto è grande l'unità principale
		sGradiDiRotazione.setMajorTickUnit(45);
		// l'unità principale non va suddivisa ulterioremente
		sGradiDiRotazione.setMinorTickCount(0);
		// deve muoversi a scatti
		sGradiDiRotazione.setSnapToTicks(true);

		sVelocità.setShowTickMarks(true);
		sVelocità.setShowTickLabels(true);
		sVelocità.setMajorTickUnit(1);
		sVelocità.setMinorTickCount(0);
		sVelocità.setSnapToTicks(true);

		TabPane pannello = new TabPane();
		
		//blocco la possibilità di chiudere le pagine
		pannello.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE); 

		Tab comandiManuali = new Tab("Comandi manuali");
		Tab comandiRegistrati = new Tab("Comandi registrati");
		Tab impostazioni = new Tab("Impostazioni");

		// comandi personalizzati
		Label eTitle = new Label("Manual ControlLight");
		Label eGradiRotazione = new Label("Rotazione in °");
		Label eVelocitàDiRotazione = new Label("Velocità");
		Label eFeedback = new Label("Feedback: ");

		Button bStart = new Button("Start");
		Button bStop = new Button("Stop");
		Button bAggiorna = new Button("Invia nuovi dati");

		eTitle.getStyleClass().add("titolo");
		bAggiorna.getStyleClass().add("buttonAggiorna");

		GridPane grigliaComandiManuali = new GridPane();
		grigliaComandiManuali.getStyleClass().add("griglia");

		grigliaComandiManuali.add(eTitle, 0, 0, 3, 1);
		grigliaComandiManuali.add(eGradiRotazione, 0, 1);
		grigliaComandiManuali.add(sGradiDiRotazione, 1, 1, 2, 1);
		grigliaComandiManuali.add(eVelocitàDiRotazione, 0, 2);
		grigliaComandiManuali.add(sVelocità, 1, 2, 2, 1);
		grigliaComandiManuali.add(bStart, 0, 3);
		grigliaComandiManuali.add(bStop, 1, 3);
		grigliaComandiManuali.add(bAggiorna, 2, 3);
		grigliaComandiManuali.add(eFeedback, 0, 4);
		grigliaComandiManuali.add(etichettaFeedback, 1, 4, 2, 1);

		grigliaComandiManuali.setPadding(new Insets(10, 10, 10, 10));
		grigliaComandiManuali.setHgap(10);
		grigliaComandiManuali.setVgap(10);

		comandiManuali.setContent(grigliaComandiManuali);

		pannello.getTabs().add(comandiManuali);

		// Comandi registrati
		Label eTitleReg = new Label("Registered ControlLight");
		Label eFeedbackReg = new Label("Feedback: ");
		Label eRegistratiReg = new Label("Pogrammi registrati");

		creaCombobox();

		Button bStartReg = new Button("Start");
		Button bStopReg = new Button("Stop");
		Button bAggiornaReg = new Button("Invia modello");

		eTitleReg.getStyleClass().add("titolo");
		bAggiornaReg.getStyleClass().add("buttonAggiorna");

		GridPane grigliaComandiRegistrati = new GridPane();
		grigliaComandiRegistrati.getStyleClass().add("griglia");

		grigliaComandiRegistrati.add(eTitleReg, 0, 0, 3, 1);
		grigliaComandiRegistrati.add(eRegistratiReg, 0, 1, 2, 1);
		grigliaComandiRegistrati.add(modelli, 2, 1);
		grigliaComandiRegistrati.add(bStartReg, 0, 3);
		grigliaComandiRegistrati.add(bStopReg, 1, 3);
		grigliaComandiRegistrati.add(bAggiornaReg, 2, 3);
		grigliaComandiRegistrati.add(eFeedbackReg, 0, 4);
		grigliaComandiRegistrati.add(etichettaFeedbackReg, 1, 4, 2, 1);

		grigliaComandiRegistrati.setPadding(new Insets(10, 10, 10, 10));
		grigliaComandiRegistrati.setHgap(10);
		grigliaComandiRegistrati.setVgap(10);

		comandiRegistrati.setContent(grigliaComandiRegistrati);
		pannello.getTabs().add(comandiRegistrati);

		// impostazioni

		Label eTitleSet = new Label("Settings");
		Label ePortaCOM = new Label("Porta COM:");
		Label eFeedbackSet = new Label("Feedback:");
		Button bCambiaPortaCOM = new Button("Cambia Porta");
		Button bCercaPorteCOM = new Button("Cerca delle Porte");
		//CheckBox temaScuro = new CheckBox("Tema Scuro");

		//temaScuro.setOnAction(e -> temaChiaroScuro(temaScuro.isSelected()));
		
		eTitleSet.getStyleClass().add("titolo");
		bCercaPorteCOM.getStyleClass().add("buttonAggiorna");

		GridPane grigliaImpostazioni = new GridPane();
		grigliaImpostazioni.getStyleClass().add("griglia");

		grigliaImpostazioni.add(eTitleSet, 0, 0);
		//grigliaImpostazioni.add(temaScuro, 2, 0);
		grigliaImpostazioni.add(ePortaCOM, 0, 1, 2, 1);
		grigliaImpostazioni.add(porteCOM, 2, 1);
		grigliaImpostazioni.add(bCambiaPortaCOM, 0, 2);
		grigliaImpostazioni.add(bCercaPorteCOM, 2, 2, 2, 1);
		grigliaImpostazioni.add(eFeedbackSet, 0, 3);
		grigliaImpostazioni.add(etichettaFeedbackSettings, 1, 3, 2, 1);
		
		grigliaImpostazioni.setPadding(new Insets(10, 10, 10, 10));
		grigliaImpostazioni.setHgap(10);
		grigliaImpostazioni.setVgap(10);

		impostazioni.setContent(grigliaImpostazioni);

		pannello.getTabs().add(impostazioni);

		//affidiamo ad ogni pulsante un metodo
		bStart.setOnAction(e -> startMovment(false));
		bStartReg.setOnAction(e -> startMovment(true));
		bStop.setOnAction(e -> stopMovment(false));
		bStopReg.setOnAction(e -> stopMovment(true));
		bAggiorna.setOnAction(e -> aggiornaDati());
		bAggiornaReg.setOnAction(e -> aggiornaDatiReg());
		bCambiaPortaCOM.setOnAction(e -> cambiaCOM());
		bCercaPorteCOM.setOnAction(e -> cercaPorteCOM());

		scena= new Scene(pannello, 500, 300);
		finestra.setScene(scena);
		finestra.setTitle("ControlLight");
		finestra.show();
		//impostaimo il tema chiaro
		temaChiaroScuro(false); 
		// visualizziamo le porte Seriali disponibili
		cercaPorteCOM();
	}

	public void creaCombobox() {
		modelli.getItems().add("modello1");
		modelli.getItems().add("modello2");
		modelli.getItems().add("modello3");
		modelli.getItems().add("modello4");
		modelli.getSelectionModel().select(0);
	}
	
	public void temaChiaroScuro(boolean chiaro) {
		if(chiaro) {
			scena.getStylesheets().clear();
			scena.getStylesheets().add("faretto/testaRotanteAsseX/Style2.css");
		}else {
			scena.getStylesheets().clear();
			scena.getStylesheets().add("faretto/testaRotanteAsseX/Style.css");
		}
	}
	
	public void startMovment(boolean registrato) {
		System.out.println("start");
		messageToSend[3]=1; //inizia movimento
		if(registrato) {
			aggiornaDatiReg();
		}else {
			aggiornaDati();
		}
		
	}

	public void stopMovment(boolean registrato) {
		System.out.println("stop");
		messageToSend[3]=0; //ferma movimento
		if(registrato) {
			aggiornaDatiReg();
		}else {
			aggiornaDati();
		}
	}
	public void aggiornaDatiReg() {
		byte velocita = 0;
		int gradiRotazione = 0;
		switch(modelli.getValue()){
			case "modello1":
				velocita=(byte)vettoreModelli[0][0];
				gradiRotazione=vettoreModelli[0][1];
				break;
			case "modello2":
				velocita=(byte)vettoreModelli[1][0];
				gradiRotazione=vettoreModelli[1][1];
				break;
			case "modello3":
				velocita=(byte)vettoreModelli[2][0];
				gradiRotazione=vettoreModelli[2][1];
				break;
			case "modello4":
				velocita=(byte)vettoreModelli[3][0];
				gradiRotazione=vettoreModelli[3][1];
				break;
		}
		
		System.out.println(velocita+" "+gradiRotazione);
		
		modificaVettore(velocita, gradiRotazione);//prepariamo il vettore per la trasmissione
		//invia il vettore
		if(!invia()) { 
			etichettaFeedbackReg.setText("!!! errore nell'invio dei dati");
		}else {
			etichettaFeedbackReg.setText("dati inviati con successo");
		}
	}
	public void aggiornaDati() {
		int gradiRotazione = (int) (sGradiDiRotazione.getValue());
		byte velocita=(byte) (sVelocità.getValue());
		
		modificaVettore(velocita, gradiRotazione);//prepariamo il vettore per la trasmissione
		
		//invia il vettore
		if(!invia()) { 
			etichettaFeedback.setText("!!! errore nell'invio dei dati");
		}else {
			etichettaFeedback.setText("dati inviati con successo");
		}
	}
	
	public void modificaVettore(byte velocita, int gradiRotazione) {
		messageToSend[2]=velocita;
		if(gradiRotazione>127) {
			//dobbiamo trasformare l'intero in byte senza perdere dati
			int moltiplica = gradiRotazione/127;
			gradiRotazione -= 127*moltiplica;
			
			messageToSend[0]=(byte) gradiRotazione; //numero rimanente
			messageToSend[1]=(byte) moltiplica; //quante volte sommare 127
		}else {
			messageToSend[0]=(byte) (gradiRotazione);
			messageToSend[1]=0;
		}
	}
	
	public boolean invia() {
		boolean stato;
		if(portaPresente) {
			portaSeriale.writeBytes(messageToSend, 4);
			byte[] acknowledgment= new byte[4];
			long tempo1=System.currentTimeMillis();
			long tempo2;
			boolean continua=true;
			while(portaSeriale.readBytes(acknowledgment, 4)!=4 && continua) {
				//ti fermi qui fino a quando non hai ricevuto i caratteri dovuti
				tempo2=System.currentTimeMillis();
				if(tempo1-tempo2>100) {
					continua=false;
				}
			}
			for(int i=0; i<acknowledgment.length; i++) {
				System.out.println(acknowledgment[i]); //se è 1 è stato ricevuto
			}
			stato=true;
		}else {
			stato=false;
		}
		return stato;

		
	}
	
	public void cercaPorteCOM() {
		if(portaPresente) {
			portaSeriale.closePort();
			portaPresente=false;
		}
		porteCOM.getItems().clear();
		nomePortaCOM = SerialPort.getCommPorts();
		System.out.println(nomePortaCOM.length);
		for (int i = 0; i < nomePortaCOM.length; i++) {
			porteCOM.getItems().add(nomePortaCOM[i].getSystemPortName());
		}
		porteCOM.getSelectionModel().select(0);
		cambiaCOM();
	}

	public void cambiaCOM() {
		String porta = porteCOM.getValue();
		for (int i = 0; i < nomePortaCOM.length; i++) {
			if (porta.equals(nomePortaCOM[i].getSystemPortName())) {
				if (nomePortaCOM[i].openPort()) {
					portaPresente=true;
					etichettaFeedbackSettings.setText("porta in uso: " + porta);
				} else {
					etichettaFeedbackSettings.setText("impossibile aprire la porta");
				}
				portaSeriale = nomePortaCOM[i];
				i = nomePortaCOM.length - 1; //fermiamo il ciclo
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
