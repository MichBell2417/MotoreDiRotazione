#include <Arduino.h>
#include <RF24.h>
#include <AccelStepper.h>

#define CE_PIN 5
#define CSN_PIN 6
RF24 radio(CE_PIN, CSN_PIN);

#define stepNumberForRotation 2100

byte pinUnoStepper = 10;
byte pinDueStepper = 9;
byte pinTreStepper = 8;
byte pinQuattroStepper = 7;
AccelStepper motore(AccelStepper::FULL4WIRE, pinUnoStepper, pinTreStepper, pinDueStepper, pinQuattroStepper, true);

//all'indice 0 vi è lo stato della rotazione, all'indice 1 la velocità e all'indice 2 i step
int stato[]={0,0,0};

void setup() {
  Serial.begin(115200);
  /***************************
  *Conigurazione Radio
  ****************************/
  if (!radio.begin()) {
    Serial.print("errore antenna");
    while(1);
  }

  uint8_t addresses[2][6]={"00010", "10010"};
  radio.setPALevel(RF24_PA_LOW);
  radio.openReadingPipe(1, addresses[0]);
  radio.openWritingPipe(addresses[1]); 
  //settiamo in modalità RX
  radio.startListening();
  /******************
  *Settaggio motore Stepper
  *******************/
  motore.setCurrentPosition(0);
  motore.setAcceleration(250);
  motore.setMaxSpeed(500);
  motore.setSpeed(stato[1]);

  Serial.println("Ready");
}

bool statoRotazione=false; //indica se il motore è in movimento o meno
unsigned long tempoRun=0;
int timeSpeed=0; //tempo di esecuzione del run
void loop() {
  if(radio.available()){
    Serial.println("available");
    int payload[3]; 
    radio.read(&payload, sizeof(payload));
    if(payload[2]==0){
      //controlliamo se è il trasmettitore ha bisogno dello stato
      if(payload[0]==int('I')&&payload[1]==int('3')){
        Serial.println("ricevuto richiesta stato");
        radio.stopListening();
        if(radio.write(&stato, sizeof(stato))){
          //il messaggio è stato inviato correttamente
          Serial.println(F("complete answer to I3"));
        }else{
          //il messaggio non è stato inviato correttamente
          Serial.println(F("error answer to I3"));
        }
        radio.startListening();
      }
    }else{
      //leggiamo il payload (che contiene lo stato) e lo salviamo nello stato
      Serial.println("reading new state");
      for(int i=0; i<3; i++){
        stato[i]=payload[i];
      }
      motore.setSpeed(stato[1]); //settiamo la velocità
      timeSpeed=1000*(1/motore.speed()); //calcoliamo il tempo di esecuzione del run
      Serial.print("stato: ");
      Serial.println(stato[0]);
      Serial.print("veocità: ");
      Serial.println(motore.speed());
      Serial.print("steps: ");
      Serial.println(stato[2]);
    }
  }
  //codice non bloccante e più reattivo
  //se il motore è in movimento
  if(stato[0]!=0){
    //e il motore era fermo
    if(!statoRotazione){
      //all'inizio del movimento eseguiamo una mezza rotazione
      motore.move(stato[2]/2);
    }
    if(motore.currentPosition()==stato[2]/2){
      statoRotazione=true;
      motore.move(-stato[2]);
    }else if(motore.currentPosition()==-stato[2]/2){
      motore.move(stato[2]);
    }
  }else if(statoRotazione){ 
    //se il motore si deve fermare ed era in movimento
    motore.move(0-motore.currentPosition()); //lo portiamo a 0
    statoRotazione=false;
  }
  //se l'ultimo run è stato eseguito più di timeSpeed millisecondi fa, eseguiamo un nuovo run
  if(millis()-tempoRun>=timeSpeed){
    motore.run();
    tempoRun=millis();
  }
}
