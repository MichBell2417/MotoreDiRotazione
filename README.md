# meccanismo-Di-Rotazione
Repository con codici riguardanti la creazioni di un sistema che, attraverso un computer, al quale c'è collegato un'[antenna](https://github.com/MichBell2417/meccanismo-Di-Rotazione/blob/main/SchemiElettrici/Antenna.pdf), riesce a comunicare attraverso un'interfaccia grafica apposita, con un [dispositivo](https://github.com/MichBell2417/meccanismo-Di-Rotazione/blob/main/SchemiElettrici/MotoreWirless.pdf) che attraverso un motore stepper, muove un faretto orizzontalmente.
## CARATTERISTICHE:
#### antenna:
l'antenna è un semplice arduino nano munito di un'trasmettitore RF24 che attraverso un codice ("[trasmettitoreMotoreRotante-2.4Ghz](trasmettitoreMotoreRotante-2.4Ghz/src)") recepisce i valori dalla Seriale e li invia al "motore".
#### motore: 
Il motore è un'altro dispositivo, ha il compito di ricevere i messaggi decodificarli capendo i comandi da eseguire ed eseguire i comandi ricevuti come la velocità di rotazione, i gradi di rotazione e quando girare o non.
#### interfaccia grafica:
avendo reputato scomodo per un qualsiasi utilizzatore scrivere i comandi sulla porta seriale dall'IDE di arduino, ho deciso di programmare un'interfaccia grafica. Questa è programmata in Java ed utilizza la libreria jSerialComm (ver. 2.11.0) per comunicare con l'antenna collegata alla porta USB. Il progetto dell'interfaccia è stato organizzato con maven e può essere eseguito attraverso terminale utilizzando un full-JDK in grado di eseguire anche javaFX.
Il file che contiene le configurazioni si trova nella cartella `*.confRotationMotor*` posizionata nella cartella dell'utente.
**esempio del comando da terminale:** 
```
*path*\jdk-21.0.5-full\bin\java -jar target/ControllerFaretto.jar
```
Dove `*path*` è il percorso nel quale si trova il JDK: `jdk-21.0.5-full` o successivi. Il comando deve essere eseguito all'interno del percorso `.../InterfacciaGrafica`, [qui](https://github.com/MichBell2417/MotoreDiRotazione/tree/main/InterfacciaGrafica).
