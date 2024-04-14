# meccanismo-Di-Rotazione
Repository con codici riguardanti la creazioni di un sistema che attraverso un computer al quale c'è collegato un'antenna, riesce a comunicare attraverso un'interfaccia grafica apposita con un dispositivo che attraverso un motore stepper, muove un faretto sull'asse orizzontale.
CARATTERISTICHE:
-antenna: l'antenna è un semplice arduino nano munito di un'trasmettitore RF24 che attraverso un codice ("TrasmettitoreTestaRotanteRF24") recepisce i valori dalla Seriale li inserisce in un vettore e li invia al "motore".
-motore: il motore è un'altro dispositivo, ha il compito di ricevere i messaggi decodificarli capendo i comandi da eseguire ed eseguire i comandi ricevuti come la velocità di rotazione, i gradi di rotazione e quando girare o non.
-interfaccia grafica: avendo reputato scomodo per un qualsiasi utilizzatore scrivere i comandi sulla porta seriale dall'IDE di arduino, ho deciso di programmare un'interfaccia grafica. Questa è programmata in Java ed utilizza la libreria jSerialComm (ver. 2.10.4) per comunicare con l'antenna attraverso dei byte.
