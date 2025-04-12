package motoreDiRotazione.software.interfacciaGrafica;

public class testConfigurations {
	 public static void main(String[] args) {
		Configurations configurazioni=new Configurations();
		configurazioni.removeConfiguration("Configurazione n.1");
		if(configurazioni.renameConfiguration("Configurazione n.8","ciao gay")) {
			System.out.println("riuscito");
		}else {
			System.out.println("non riuscito");
		}
		configurazioni.getConfigurationInfo("ciao gay");
	}
}
