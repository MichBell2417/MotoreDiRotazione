#include <Stepper.h>

/*
V1.0.1
-Rimosso:
 la variabile setAntiCLockwiseRotation dalle dichiarazioni e dal loop

 else if(tipo=="r"){//se risulta vera vuol dire che si vuole cambiare i step per la rotazione in senso antiorario
      //controlla se il numero inserito nella seriale può essere usato per impostare i step in senso antiorario
      if(Intero>=0 && Intero<=stepPerRotation){
        setAnticlockwiseRotation=Intero;
        Serial.print("I STEP IN SENSO ANTIORARIO CHE VENGONO ESEGUITI SONO: ");
        Serial.println(setAnticlockwiseRotation);
      }else{
        Serial.println("!!! i step in senso antiorario inseriti non possono essere eseguiti");//restituiamo questa scritta di errore
      }
 Perfezionato il programma
V1.0.2
-trasformazione degli step in grades da 0 e 2100 a 0 e 360 (RIGA 154) Intero=map(Intero,0,360,0,2100);
-trasformazione della velocità da 1 a 16 in 1 a 10(funzione map riga 130) Intero=map(Intero, 1, 10, 1, 16);
*/


int stepPerRotation = 2100; //step nei quali il motorte svolge una rotazione di 360°

Stepper motoreX(stepPerRotation, 8, 10, 9, 11);

int setRotation=0;
int setClockwiseRotation=setRotation; //i step che il motore fa in senso orario
int speedStandard=10; //velocità con la quale il motore inizia la rotazione
int speedMax=16; //impopstare la velocità massima dopo la quale il motore sgrana/si rovina
int speedMin=1; //se si imposta a 0 il codice non funzionerà

bool halfRotation=true;
bool Stop=false; 
bool Start=false; 
bool active=false;

String numeri="1234567890"; //stringa che verà utilizzata per scopire quanti numeri contiene la stringa inserita nella seriale
String lettere="SR";//contiene le lettere predefinita che hanno una funzione:
                    //S=speed/Stop se non ha valori numerici dopo la S
                    //R=step da eseguire in senso orario corrisponde alla variabile "setClockwiseRotation"
                    //r=step da eseguire in senso antiorario corrisponde alla variabile "setAnticlockwiseRotation"

void setup() {
  pinMode(8, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(11, OUTPUT);
  motoreX.setSpeed(speedStandard);
  Serial.begin(9600);
  Serial.print("LA VELOCITA è: ");
  Serial.println(speedStandard);
  Serial.println("settare la rotazione con R (min: 1; max=360) e successivamente scrivere S per attivare la rotazione, \n per modificare la velocita bisogna scrivere S(numero che sia tra 1 e 10)");
}

void loop() {
  if(halfRotation){
    if(Start){
      active=true;
      Start=false;
      halfRotation=false;
      int rotation=setClockwiseRotation/2;
      Serial.println(rotation);
      motoreX.step(-rotation);
    }
    if(Stop){
      active=false;
      halfRotation=false;
      Stop=false;
      int rotation=setClockwiseRotation/2;
      Serial.println(rotation);
      motoreX.step(+rotation);
      Serial.println("Il movimento è stato bloccato");
    }
  }
  if(active){
    motoreX.step(setClockwiseRotation);
    delay(25);
    if(Serial.available()){
      check();
    }
    if(!active){
      Serial.println("sono ritornato per completare il giro");
    }
    motoreX.step(-setClockwiseRotation);
    delay(25);
    if(Serial.available()){
      check();
    }
  }
  if(Serial.available()){
    check();
  }
}

void check(){
  String readString;
  while (Serial.available()) {
    char c = Serial.read();
    delay(50);
    readString += c;
  }
  Serial.println(readString);
  //controllare quali lettere tra quelle predefinite (che si trovano nella variabile "lettere") sono presenti all'interno della stringa presa da seriale
  String tipo="";//qui vengono memorizzate le lettere
  int numeroLettere=0;//serve per contare i caratteri che devono essere eliminati dalla stringa
  for(int x=0; x<1; x++){ //controlliamo solo il primo carattere della stringa da seriale 
                          //(il codice che scriveremo da seriale conterrà solo una lettera che si trova all'inizio della stringa)
    for(int l=0; l<lettere.length(); l++){
      if(readString[x]==lettere[l]){
        Serial.print("lettera---------");
        tipo=tipo+readString[x];
        Serial.println(tipo);
        numeroLettere++;//incrementiamo i caratteri da rimuovere
      }
    }
  }
  //rimuovere i caratteri controllati, dalla stringa presa dalla seriale
  readString.remove(0,numeroLettere);//rimuoviamo i caratteri dall'indice 0 fino all'indice corrispondente ai caratteri controllati
  Serial.println(readString);//scriviamo la stringa rimanente
  //controlliamo ora se il numero messo è un intero o una stringa (se sarà una stringa il codice dovrà essere ignorato)
  int ContaInt=0;//controlliamo quanti caratteri che dicano gli interi sono presenti
  for(int x=0; x<readString.length(); x++){
    for(int n=0; n<numeri.length(); n++){
      if(readString[x]==numeri[n]){
        Serial.print("numero---------");
        ContaInt++;
        Serial.println(readString[x]);
      }
    }
  }
  //nel caso il numero risulti essere un intero facciamo i controlli successivi
  if(ContaInt==readString.length()){
    //convertiamo la stringa della seriale ad intero e la memoriziamo all'interno di una variabile (Nome:"Intero")
    int Intero=atoi(readString.c_str());
    if(tipo=="S"){//se risulta vera vuol dire che si vuole cambiare la velocità
      //controlla se il numero inserito nella seriale può essere usato per settare la velocità del motore
      Intero=map(Intero, 1, 10, 1, 16);
      if(Intero>=speedMin && Intero<=speedMax){
        motoreX.setSpeed(Intero);
        Serial.print("LA VELOCITA è: ");
        Serial.println(Intero);
      }else if(readString.length()==0){
        if(setClockwiseRotation!=0 && active==false){
          Start=true;
          halfRotation=true;
          setClockwiseRotation=setRotation;
          Serial.println("Il movimento è stato sbloccato");
        }else if(setClockwiseRotation!=0 && active==true){
          halfRotation=true;
          Stop=true;
          loop();
        }else if(setClockwiseRotation==0){
          Serial.println("!!! settare la rotazione, attualmente corrispone a 0");
        }
        
      }else{
        Serial.println("!!! la velocità non può essere settata");//restituiamo questa scritta di errore
      }
    }else if(tipo=="R"){//se risulta vera vuol dire che si vuole cambiare i step per la rotazione in senso orario
      //controlla se il numero inserito nella seriale può essere usato per impostare i step in senso orario
      Intero=map(Intero,0,360,0,2100);
      if(Intero>=0 && Intero<=stepPerRotation && active==false){
        setClockwiseRotation=Intero;
        setRotation=Intero;
        Serial.print("I STEP IN SENSO ORARIO CHE VENGONO ESEGUITI SONO: ");
        Serial.println(setClockwiseRotation);
        Serial.println("è stato aggiornato il setRotation");
      }else{
        if(active){
          Serial.println("!!! FERMA L'ESECUZIONE E RIPROVA");
        }else{
          Serial.println("!!! i step in senso orario inseriti non possono essere eseguiti prova a dimunirli min:0 max:360");//restituiamo questa scritta di errore
        }
        
      }
    }else{
      if(tipo.equals("")){
        Serial.println("!!! attenzione non risulta essere inserito alcun comando");
      }else{
        Serial.print("!!! attenzione la lettera:");
        Serial.print(tipo);
        Serial.println(" non fa parte di alcun comando");
      }
      
    }
  }else{
    Serial.println("!!! attenzione è stato trovato un errore");
    Serial.println("!!! la sintassi deve essere per esempio: S5. quindi il comando e il valore");
  }
}