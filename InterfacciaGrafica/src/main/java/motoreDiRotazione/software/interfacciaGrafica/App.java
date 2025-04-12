package motoreDiRotazione.software.interfacciaGrafica;

import java.util.concurrent.TimeUnit;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

	final int MAX_SPEED= 500;
	final int STEP_ROTATION= 2100;
	
	SerialPort port;
	SerialPort[] serialPorts;
	ComboBox<String> portsList= new ComboBox<String>();
	Label stato= new Label();
	Timeline timelineControllo;
	//variable che contiene lo stato da inviare
	byte[] stateToSend;

	boolean rotationState=false;
	
	Configurations configurations=new Configurations();
	
	//oggetti dipendenti dallo stateToSend
	Slider sSpeed;
	int valSpeed=5;
	Slider sRotation;
	int valRotation=180;
	Button bStateManual;
	Button bStateConf;
	ActualStatus stateNow =new ActualStatus(false, 1, 1);
	GridPane gridConf;
	GridPane gridManual;
	
	//oggetti dell'interfaccia cha si richiamano nel codice
	TabPane interfaccia= new TabPane();
	Tab manualControl = new Tab("manuale");
	Tab configurationControl= new Tab("configurazioni");
	Tab settings= new Tab("settings");
	
	Stage mainView;
	
	public void start(Stage finestra) {
		mainView=finestra;
		timelineControllo= new Timeline(new KeyFrame(
				Duration.millis(50),
				x -> controllo()));
		timelineControllo.setCycleCount(Animation.INDEFINITE);

		interfaccia.getTabs().add(manualControl);
		interfaccia.getTabs().add(configurationControl);
		interfaccia.getTabs().add(settings);

		GridPane gridSettings= new GridPane();
		gridSettings.getStyleClass().add("gridPane");
		Label eListPorts= new Label("porte collegate");
		aggiornaPorte();
		Label eStato= new Label("StatoPorta");
		Button bSave= new Button("Seleziona");
		Button bSearch= new Button("Cerca porte");
		gridSettings.add(eListPorts, 0, 0);
		gridSettings.add(portsList, 0, 1);
		gridSettings.add(eStato, 1, 0);
		gridSettings.add(stato, 1, 1);
		gridSettings.add(bSave, 1, 2);
		gridSettings.add(bSearch, 0, 2);
		bSave.setOnAction(e->settingsSave());
		bSearch.setOnAction(e->aggiornaPorte());
		settings.setContent(gridSettings);

		manualControl.setContent(gridManual());
		configurationControl.setContent(gridConf());
		
		interfaccia.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		interfaccia.setTabMinWidth(100);		
		Scene scena= new Scene(interfaccia, 600, 400);
		scena.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		Image icon = new Image(getClass().getResource("/icona.png").toExternalForm());
        finestra.getIcons().add(icon);
		finestra.setScene(scena);
		finestra.setTitle("Centro di Controllo Faretto");
		finestra.show();
		//se la finestra viene chiusa senza chiudere la porta la chiude in automatico
		finestra.setOnCloseRequest(e->{
			if(port!=null && port.isOpen() ) {
				port.closePort();
			}
		});
	}
	
	public GridPane gridManual(){
		gridManual= new GridPane();
		gridManual.getStyleClass().add("gridPane");
		Label eSpeed=new Label("velocità");
		Label eRotation=new Label("Rotazione °");
		Label eStateManual=new Label("Rotazione");
		sSpeed=new Slider();
		sSpeed.setMax(10);
		sSpeed.setMin(1);
		sSpeed.setValue(valSpeed);
		sSpeed.setShowTickMarks(true);
		sSpeed.setSnapToTicks(true);
		sSpeed.setMajorTickUnit(1);
		sSpeed.setMinorTickCount(0);
		sRotation=new Slider();
		sRotation.setMax(360);
		sRotation.setMin(45);
		sRotation.setValue(valRotation);
		sRotation.setShowTickMarks(true);
		sRotation.setSnapToTicks(true);
		sRotation.setMajorTickUnit(45);
		sRotation.setMinorTickCount(0);
		Label eValueSpeed=new Label(""+sSpeed.getValue());
		eValueSpeed.getStyleClass().add("labelValues");
		Label eValueRotation=new Label(""+sRotation.getValue());
		eValueRotation.getStyleClass().add("labelValues");
		bStateManual=new Button(rotationState?"ON":"OFF");
		Button bSyncManual=new Button("Sync");
		Button bSaveConf=new Button("Add Config.");
		gridManual.add(eSpeed, 0, 0);
		gridManual.add(sSpeed, 1, 0);
		gridManual.add(eValueSpeed, 2, 0);
		gridManual.add(eRotation, 0, 1);
		gridManual.add(sRotation, 1, 1);
		gridManual.add(eValueRotation, 2, 1);
		gridManual.add(eStateManual, 0, 2);
		gridManual.add(bStateManual, 1, 2);
		gridManual.add(bSyncManual, 0, 3);
		gridManual.add(bSaveConf, 1, 3);
		VBox viewStateNow=stateNow.getView();
		viewStateNow.getStyleClass().add("actualStatus");
		gridManual.add(viewStateNow, 3, 0, 1, 4);
		sSpeed.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				int value=(int) Math.round(newValue.doubleValue());
				eValueSpeed.setText(""+value);
				valSpeed=value;
			}
		});
		sRotation.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				int newValueInt=(int) Math.round(newValue.doubleValue());
				if(newValueInt%45==0) {
					int value=(int) Math.round(newValue.doubleValue());
					eValueRotation.setText(""+value);
					valRotation=value;
				}
			}
		});
		bStateManual.setOnAction(e->changeRotationStatus());
		bStateManual.getStyleClass().add("slider");
		bSaveConf.setOnAction(e->newConfiguration());
		bSaveConf.getStyleClass().add("slider");
		bSyncManual.setOnAction(e->sendState());
		return gridManual;
	}
	public GridPane gridConf(){
		gridConf= new GridPane();
		gridConf.setFocusTraversable(true);
		gridConf.getStyleClass().add("gridPane");
		ListView<String> listaConf = new ListView<String>();
		listaConf.setItems(configurations.getConfigurations());
		Label eStateConf=new Label("Rotazione");
		Button bSyncConf=new Button("Sync");
		Button bDelete=new Button("Delete");
		Button bInfo=new Button("Info");
		Button bRename=new Button("Rename");
		bInfo.setAlignment(Pos.BOTTOM_CENTER);
		VBox buttonBox= new VBox(10);
		buttonBox.getChildren().addAll(bSyncConf, bDelete, bInfo, bRename);
		bStateConf=new Button(rotationState?"ON":"OFF");
		gridConf.add(listaConf, 0, 0, 1, 2);
		gridConf.add(buttonBox, 1, 0);
		gridConf.add(eStateConf, 0, 2);
		gridConf.add(bStateConf, 1, 2);
		VBox viewStateNow=stateNow.getView();
		viewStateNow.getStyleClass().add("actualStatus");
		gridConf.add(viewStateNow, 2, 0, 1, 3);
		bSyncConf.setOnAction(e->syncData(listaConf.getSelectionModel().getSelectedItem()));
		bDelete.setOnAction(e->deleteConfiguration(listaConf.getSelectionModel().getSelectedItem()));
		bInfo.setOnAction(e->showInfo(listaConf.getSelectionModel().getSelectedItem()));
		bRename.setOnAction(e->renameConfig(listaConf.getSelectionModel().getSelectedItem()));
		bStateConf.setOnAction(e->{
			changeRotationStatus();
			sendState();
		});
		return gridConf;
	}
	private void changeRotationStatus() {
		rotationState=!rotationState;
		if(rotationState) {
			bStateConf.setText("ON");
			bStateManual.setText("ON");
		}else {
			bStateConf.setText("OFF");
			bStateManual.setText("OFF");
		}
	}
	public void deleteConfiguration(String nomeConfigurazione){
		if(nomeConfigurazione!=null) {
			configurations.removeConfiguration(nomeConfigurazione);
		}
	}
	public void renameConfig(String nomeConfigurazione){
		if(nomeConfigurazione!=null) {
			Stage rename=new Stage();
			rename.setResizable(false);
	        rename.setTitle("rinomina");
	        Label eAvviso=new Label("pigia 'invio' per confermare o\n 'esc' per annullare");
	        TextField tNome=new TextField();
	        tNome.setText(nomeConfigurazione);
	        tNome.setPromptText("inserisci nuovo nome");
	        VBox box=new VBox(5,eAvviso,tNome);
	        tNome.setFocusTraversable(true);
	        tNome.setOnKeyPressed(new EventHandler<KeyEvent>() {
	        	@Override
	        	public void handle(KeyEvent event) {
		        	if(event.getCode().equals(KeyCode.ENTER)) {
		        		configurations.renameConfiguration(nomeConfigurazione,tNome.getText());
		        		rename.close();
		        	}else if(event.getCode().equals(KeyCode.ESCAPE)){
		        		rename.close();
		        	}
	        	}
	        });
	
	        Scene scena=new Scene(box,200,200);
	        rename.setScene(scena);
	        rename.show();
		}else {
			viewPopup("seleziona la configurazione da rinominare");
		}
	}
	private void showInfo(String nomeConfigurazione) {
        // Creazione dello Stage del dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL); // Impedisce interazioni con la finestra principale
        dialog.initOwner(mainView);
        dialog.setResizable(false);
        dialog.setTitle("Informazioni di: "+nomeConfigurazione);
        try{
        	String[] value=configurations.getConfigurationInfo(nomeConfigurazione);
        	VBox box=new VBox(2);
			Label title=new Label(nomeConfigurazione);
			title.getStyleClass().add("infoTitle"); //TODO: creare classe
			box.getChildren().add(title);
			box.getChildren().add(new Label("Rotazione: "+ (value[0].equals("true") ? "ON" : "OFF")));
			box.getChildren().add(new Label("Velocità: "+value[1].toString()));
			box.getChildren().add(new Label("Rotazione °: "+ value[2].toString()));
			box.setAlignment(Pos.CENTER);
	        box.setOnMouseClicked(event -> dialog.close());
	        Scene dialogScene = new Scene(box, 250, 150);
	        dialog.setX(mainView.getX()+(mainView.getWidth()/2-250/2));
	        dialog.setY(mainView.getY()+(mainView.getHeight()/2-150/2));
	        dialog.setScene(dialogScene);
	        dialog.show();
        }catch (NullPointerException e) {
			viewPopup("seleziona o aggiungi \n una configurazione");
		}
    }
	public void viewPopup(String text) {
		Popup alert = new Popup();
		Label eText=new Label(text);
		eText.getStyleClass().add("infoTitle");
		VBox box=new VBox(eText);
		box.getStyleClass().add("infoBackground");
		box.setPrefWidth(300);
		alert.getContent().add(box);
		alert.setAutoHide(true);
		alert.show(mainView);
		alert.setAnchorX(mainView.getX()+(mainView.getWidth()/2-300/2));
		alert.setAnchorY(mainView.getY()+50);
		alert.setAnchorLocation(AnchorLocation.WINDOW_BOTTOM_LEFT);
	}
	
	public void newConfiguration() {
		configurations.addConfiguration(rotationState+"|"+valSpeed+"|"+valRotation+"|");
		viewPopup("Configurazione aggiunta");
	}
	public void saveState(String stato){
		char[] charatters=stato.toCharArray();
		stateToSend= new byte[charatters.length];
		int i=0;
		for(char c : charatters) {
			stateToSend[i]=(byte)c;
			i++;
		}
		upgradeInterface(); //salvo i dati nelle corrispondenti variabili
		stateNow.setProperties(rotationState, valSpeed, valRotation);
		manualControl.setContent(gridManual());
		configurationControl.setContent(gridConf());
	}
	private void upgradeInterface(){
		String content="";
		int value=0;
		for(byte c : stateToSend){
			char chararcter=(char)c;
			if(chararcter=='|') {
				switch (value) {
				case 0:
					String text;
					if(Integer.parseInt(content)==1) {
						text="ON";
						rotationState=true;
					}else {
						text="OFF";
						rotationState=false;
					}
					bStateConf.setText(text);
					bStateManual.setText(text);
					break;
				case 1:
					valSpeed=Integer.parseInt(content)*10/MAX_SPEED;
					break;
				case 2:
					valRotation=Integer.parseInt(content)*360/STEP_ROTATION;
					break;
				}
				value++;
				content="";
			}else {
				content+=chararcter;
			}
		}
	}
	private void syncData(String nomeConfigurazione){
		try {
			String[] stato=configurations.getConfigurationInfo(nomeConfigurazione);
			rotationState=stato[0].equals("true") ? true : false;
			sSpeed.setValue(Double.parseDouble(stato[1]));
			sRotation.setValue(Double.parseDouble(stato[2]));
			sendState();
		} catch (NullPointerException e) {
			viewPopup("selsziona una configurazione");
		}
		
	}
	private void sendState(){
		int valueRotation=rotationState ? 1 : 0;
		int valueSpeed=(int)(MAX_SPEED/10*Math.round(sSpeed.getValue()));
		int valueRotationGrades=(int)(STEP_ROTATION/360.0*Math.round(sRotation.getValue()));
		saveState(valueRotation+"|"+valueSpeed+"|"+valueRotationGrades+"|"); //preparo il vettore da inviare
		try{
			port.writeBytes(stateToSend, stateToSend.length);
		}catch (NullPointerException e) {
			interfaccia.getSelectionModel().select(settings); //ci spostiamo sulla schermata delle impostazioni
			errorePorta(3);
		}
		
	}
	private void controllo(){
		if(port!=null) {
			if(!port.isOpen()) {
				//se la porta non è aperta ma una porta è selezionata
				if(portsList.getSelectionModel().getSelectedIndex()!=-1) {
					aggiornaPorte();
					port=null;
					stato.setText("dispositivo perso");
				}
			}else {
				//se la porta è aperta controlliamo se è disponibile qualche messaggio
				if(port.bytesAvailable()>0) {
					String messaggio=leggiString();
					if(messaggio.charAt(0)=='E') {
						System.out.println("ricevuto errore durante il controllo: "+messaggio);
						//abbiamo un errore dal microcontrollore
						switch(messaggio.charAt(1)) {
						case '3': 
							errorePorta(8);
							break;
						case '4': 
							errorePorta(9);
							break;
						case '5': 
							errorePorta(8);
							interfaccia.getSelectionModel().select(settings); //ci spostiamo sulla schermata delle impostazioni
							break;
						default:
							errorePorta(999);
						}
					}else {
						//abbiamo ricevuto lo stato attuale del motore
						System.out.println(messaggio);
						saveState(messaggio);
					}
				}
			}
		}
	}
	private void aggiornaPorte() {
		if(port!=null) {
			port.closePort();
		}
		serialPorts=SerialPort.getCommPorts();
		portsList.getSelectionModel().clearSelection();
		portsList.getItems().clear();
		for(SerialPort singlePort : serialPorts) {
			portsList.getItems().add(singlePort.getDescriptivePortName());
		}
	}
	/**
	 * metodo per salvare ed aprire la porta selezionata
	 */
	private void settingsSave(){
		try{
			if(port!=null) {
				//se c'è una porta selezionata
				if(port!=serialPorts[portsList.getSelectionModel().getSelectedIndex()]) {
					//se la porta selezionata è diversa da quella precedente
					port.closePort(); //chiudi porta
					port=serialPorts[portsList.getSelectionModel().getSelectedIndex()]; //selezionane un'altra
				}
			}else {
				//se non c'è una porta selezionata selezionala
				port=serialPorts[portsList.getSelectionModel().getSelectedIndex()];
			}
			if(!port.isOpen()) {
				//se la porta non è aperta
				if(port.openPort()){
					stato.setText("porta aperta");
					port.setBaudRate(115200);
					TimeUnit.SECONDS.sleep(2);
					String ricevuto=leggiString();
					//iniziamo la comunicazione
					if(ricevuto.equals("I1")) {
						System.out.println("rispondo I2");
						byte[] messaggio= {'I', '2'};
						port.writeBytes(messaggio, 2);
						//controlliamo se il dispositivo viene disconnesso nel modo sbagliato
						port.addDataListener(new SerialPortDataListener() {
							@Override
							public int getListeningEvents() {
								return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
							}
							@Override
							public void serialEvent(SerialPortEvent serialPortEvent) {
								final int event=serialPortEvent.getEventType();
								if (event == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
									port.closePort();
									interfaccia.getSelectionModel().select(settings);
								}
							}
						});
						timelineControllo.play();
					}else if(ricevuto.equals("E1")){
						errorePorta(5);
					}else {
						errorePorta(7);
					}
				}else {
					errorePorta(1);
				}
			}else {
				errorePorta(2);
				port.closePort();
				settingsSave();
			}
		}catch (ArrayIndexOutOfBoundsException e) {
			if(portsList.getItems().size()!=0) {
				errorePorta(3);
			}else {
				errorePorta(4);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String leggiString() {
		byte[] messaggioByte= new byte[12];
		String messaggio="";
		int nByte;
		if((nByte=port.readBytes(messaggioByte, 12))!=-1) {
			System.out.println("nByte: "+nByte);
			nByte=12;
			for(int i=0; i<nByte; i++) {
				if(messaggioByte[i] != 0) {
					messaggio+=(char)messaggioByte[i];
				}
			}
		}else {
			errorePorta(6);
		}
		return messaggio;
	}

	public static void main(String args[]) {
		launch(args);
	}
	private void errorePorta(int nErrore) {
		System.out.println("Errore Porta Rilevato " + nErrore);
		switch (nErrore) {
		case 1:
			stato.setText("impossibile aprire la porta");
			port=null;
			break;
		case 2:
			stato.setText("la porta è gia in uso");
			break;
		case 3:
			stato.setText("seleziona una porta");
			port=null;
			break;
		case 4:
			stato.setText("nessun dispositivo rilevato");
			aggiornaPorte();
			break;
		case 5:
			stato.setText("antenna non rilevata o inutilizzabile");
			port.closePort();
			break;
		case 6:
			stato.setText("messaggio non ricevuto correttamente");
			break;
		case 7:
			stato.setText("attenzione il dispositivo non risponde correttamente");
			port.closePort();
			aggiornaPorte();
			break;
		case 8:
			stato.setText("il motore non è in linea");
			break;
		case 9:
			stato.setText("il motore ha ricevuto ma non risposto");
			break;
		default:
			stato.setText("Errore Sconosciuto");
		}
	}
}