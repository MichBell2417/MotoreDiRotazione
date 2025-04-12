#include <Arduino.h>
#include <RF24.h>

#define CE_PIN 7
#define CSN_PIN 8
RF24 radio(CE_PIN, CSN_PIN);

//dichiarazioni funzioni
void comunicaStatoSeriale(int stato[]);

//constanti
const byte LEN_STATO = 3;

int stato[3];

void setup() {
  /***************************
  *Conigurazione Radio
  ****************************/
  if (!radio.begin()) {
    Serial.println("E1"); //E1 significa che la radio no funziona
    while(1);
  }
  radio.setPALevel(RF24_PA_LOW);
  //configuriamo due canali uno in scrittura ed uno in lettura
  uint8_t addresses[2][6]={"00010", "10010"};
  radio.openReadingPipe(1, addresses[1]);
  radio.openWritingPipe(addresses[0]); 
  //settiamo in modalità TX
  radio.stopListening();

  /***************************
  *Conigurazione Seriale
  ****************************/
  Serial.begin(115200);
  //Comunichiamo lo stato di inizio
  Serial.print("I1");

  //attendiamo il segnale di conferma
  String codice="";
  bool answer=false;
  byte lenght;
  while (!answer){
    if (Serial.available() && lenght <= 1){
      codice += char(Serial.read());
    }else if (lenght == 2){
      answer = true; // fermo il while
    }
    lenght = codice.length();
  }
  
  if(codice.equals("I2")){
    int payload[]= {'I','3',0};
    if(radio.write(&payload, sizeof(payload))){
      radio.startListening();
      long tempo1=millis();
      while(!radio.available()){
        if(millis()-tempo1>1000){
          break;
        }
      }
      if(radio.available()){
        radio.read(&stato, sizeof(stato));
        comunicaStatoSeriale(stato);
        radio.stopListening();
      }else{
        Serial.println("E4"); //il motore non ha risposto
        while(1);
      }
    }else{
      Serial.println("E3"); //il motore non è stato trovato
    }
  }else{
    Serial.print("E2"); //il computer non è in linea 
    while(1);
  }
}

void comunicaStatoSeriale(int stato[]){
  for(int i=0; i<LEN_STATO; i++){
    Serial.print(stato[i]);
    Serial.print("|");
  }
}

String statoSerial="";
int length;
long tempo1;
void loop() {
  //controlliamo se sono stati ricevuti dati dalla seriale 
  if(Serial.available()){
    while(Serial.available()){
      if(Serial.available()){
        statoSerial+=(char)(Serial.read());
        tempo1=millis();
      }
    }
  }
  length=statoSerial.length();
  //ci assicuriamo che siano stati ricevuti tutti i caratteri almeno 6
  if(length>6 && millis()-tempo1>50){
    if(!statoSerial.equals("")){
      int stateToSend[3]; //all'indice 0 mettiamo lo stato della rotazione, all'indice 1 la velocità e all'indice 2 i step
      int index=0; //variabile per controllare che valore stiamo leggendo
      String strNum; //variabile temporanea per contenere i valori del messaggio
      for(int i=0; i<length; i++){
        //se non è presente il simbolo che indica di salvare il valore ('|')
        if(statoSerial[i]!='|'){
          strNum+=statoSerial[i]; //leggiamo i valori dell'index
        }else{
          //eseguiamo il seguente codice quando troviamo il carattere '|'
          //se la velocita o i gradi sono inferiori di uno impostali ad uno
          if(index==2 && strNum.toInt()<1){
            strNum="1";
          }
          if(index==1 && strNum.toInt()<1){
            strNum="1";
          }
          //salviamo il valore letto nello stateToSend
          stateToSend[index]=strNum.toInt();
          strNum="";
          index++; //cambiamo l'indice per il prossimo valore
        }
      }
      //strutturato il messaggio lo inviamo al motore
      if(!radio.write(&stateToSend, sizeof(stateToSend))){
        Serial.print("E5"); //comunichiamo che il motore non è stato raggiunto
      }
      statoSerial=""; //resettiamo il messaggio letto
    }
  }
}