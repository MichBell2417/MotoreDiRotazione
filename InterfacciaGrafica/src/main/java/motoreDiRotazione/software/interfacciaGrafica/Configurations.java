package motoreDiRotazione.software.interfacciaGrafica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Configurations {
	private ObservableList<String> configurations=FXCollections.observableArrayList();
	private Hashtable<String, String> abbinamenti = new Hashtable<String, String>();
	private String percorsoFile=System.getProperty("user.home")+File.separator+".confRotationMotor"+File.separator+"configurazioni.csv";
	public Configurations(){
		checkForFile();
	}
	public void checkForFile(){
		try (FileReader file= new FileReader(percorsoFile);
			BufferedReader fileReader = new BufferedReader(file);){
			String row;
			while((row=fileReader.readLine())!=null) {
				String[] dati=row.split(",");
				String configuration=dati[0];
				configurations.add(configuration);
				abbinamenti.put(configuration, dati[1]);
			}
		} catch (FileNotFoundException e) {
			try (FileWriter file = new FileWriter(percorsoFile);){
			} catch (FileNotFoundException e1) {
				new File(System.getProperty("user.home")+File.separator+".confRotationMotor").mkdirs();
				checkForFile(); //ricominciamo i controlli
			}catch (IOException e2) {
				e2.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	public void addConfiguration(String stato) {
		int numero=1;
		String nome="Configurazione n."+(numero);
		if(configurations.contains(nome)) {
			while(configurations.contains(nome)) {
				numero++;
				nome="Configurazione n."+(numero);
			}
		}
		configurations.add(nome);
		abbinamenti.put(nome, stato);
		aggiornaFile();
	}
	public void removeConfiguration(String configurationName) {
		configurations.remove(configurationName);
		abbinamenti.remove(configurationName);
		aggiornaFile();
		configurations.clear();
		abbinamenti.clear();
		checkForFile();
	}
	public boolean renameConfiguration(String configurationName, String newName) {
		if(configurations.indexOf(configurationName)!=-1) {
			configurations.add(configurations.indexOf(configurationName), newName);
			configurations.remove(configurationName);
			try{
				abbinamenti.put(newName, abbinamenti.get(configurationName));
				abbinamenti.remove(configurationName);
				aggiornaFile();
				return true;
			}catch (NullPointerException e) {
				return false;
			}
		}else {
			return false;
		}
	}
	private void aggiornaFile() {
		try (FileWriter file = new FileWriter(percorsoFile);){
			for(int i=0; i<configurations.size(); i++) {
				file.write(configurations.get(i)+","+abbinamenti.get(configurations.get(i))+"\n");
			}
		} catch (FileNotFoundException e1) {
			new File(System.getProperty("user.home")+File.separator+".confRotationMotor").mkdirs();
			checkForFile(); //ricominciamo i controlli
		}catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	public String[] getConfigurationInfo(String configurationName){
		String[] stato=abbinamenti.get(configurationName).split("\\|");
		return stato;
	}
	public ObservableList<String> getConfigurations() {
		return configurations;
	}
}
