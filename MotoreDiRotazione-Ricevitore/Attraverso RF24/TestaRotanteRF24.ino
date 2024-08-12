#include <Stepper.h>

#include <RF24.h>
#include <RF24_config.h>
#include <nRF24L01.h>
#include <printf.h>

#define stepNumberForRotation 2100

Stepper motore(stepNumberForRotation, 10, 8, 9, 7);

uint8_t messaggio[6];

RF24 ricevitore(5, 6);
uint8_t address[][6] = { "00001" };

String comando = "";

bool movimento=false;
int rotazione=90;
int velocita=5;

void setup() {
  Serial.begin(115200);
  if (!ricevitore.begin()) {
    Serial.println(F("radio hardware is not responding!!"));
    while (1) {}  // hold in infinite loop
  } else {
    Serial.println(F("pronto"));
  }
  ricevitore.setPayloadSize(sizeof(messaggio));
  ricevitore.setPALevel(RF24_PA_LOW);
  ricevitore.openReadingPipe(1, address[0]);
  ricevitore.startListening();

  motore.setSpeed(velocita);
}
void loop() {
  if (ricevitore.available()) {
    comando = "";
    int payload = ricevitore.getPayloadSize();
    Serial.println(payload);
    ricevitore.read(&messaggio, payload);
    for (int i = 0; i < sizeof(messaggio); i++) {
      int valore = (int)messaggio[i];
      if (i == 1) {
        i += 1;
        valore += messaggio[i] * 127;
        comando += valore;
      } else if (i == 4) {
        valore = (int)messaggio[i];
        comando += valore;
      } else if (i != 2) {
        char carattere = (char)valore;
        if (i == 5 && carattere != 'S') {
          comando += "";
        } else {
          comando += carattere;
        }
      }
      Serial.println(comando);
    }
    eseguiComando();
    Serial.println("agiornamento");
  }

  if(movimento){
    Serial.println("orario");
    motore.step(rotazione);
    delay(100);
    Serial.print("antiorario");
    Serial.println(-rotazione);
    motore.step(-rotazione);
    delay(100);
  }

}
void eseguiComando() {
  int conta=0;
  Serial.print("lunghezza comando: ");
  for (int i = 0; i < comando.length(); i++) {
    char opzione;
    String param="";
    int parametro=0;
    //interpretiamo i comando con i ripettivi valori
    if (comando[i] == 'R') {
      //leggo i parametri dati
      while (comando[++i] >= '0' && comando[i] <= '9') {
        param+=comando[i]; //usata come appoggio
      }
      i--;
      parametro=atoi(param.c_str()); //trasformo la stringa in intero
      opzione = 'R';
    }else if (comando[i] == 'S') {
      while (comando[++i] >= '0' && comando[i] <= '9') {
        param+=comando[i]; //usata come appoggio
      }
      i--;
      parametro=atoi(param.c_str());
      opzione = 'S';
    }
    Serial.println(conta++);
    Serial.println(opzione);
    Serial.println(parametro);
    elabora(opzione, parametro);
  }
}
//variabili globali utili nel metodo sottostante
int rotazioneModificata;
int velocitaModificata;
void elabora(char opzione, int parametro){
  if(opzione=='R'){
    rotazioneModificata=parametro;
    rotazioneModificata=map(rotazioneModificata, 0,360,0,2100);
  }
  if(opzione=='S' && parametro!=0){
    velocitaModificata=parametro;
    //se ci sono cambiamenti 
    if(rotazioneModificata!=rotazione || velocitaModificata!=velocita){
      if(movimento){
        Serial.println("modifiche eseguite in movimento");
        stopMovimento();
        rotazione=rotazioneModificata;
        velocita=velocitaModificata;
        motore.setSpeed(velocita);
        startMovimento();
      }else{
        Serial.println("modifiche eseguite da fermo");
        rotazione=rotazioneModificata;
        velocita=velocitaModificata;
        motore.setSpeed(velocita);
      }
      Serial.print("rotazione okay: ");
      Serial.println(rotazione);
      Serial.print("velocità okay: ");
      Serial.println(velocita);
    }
  }
  if(opzione=='S' && parametro==0){
    if(movimento){
      stopMovimento();
    }else{
      startMovimento();
    }
    Serial.print("movimanto okay: ");
    Serial.println(movimento);
  }
}
void stopMovimento() {
  Serial.println("stop");
  motore.step(rotazione / 2);
    Serial.print("orario: ");
    Serial.println(rotazione / 2);
  movimento = false;
}
void startMovimento() {
  Serial.println("start");
  motore.step(-(rotazione / 2));
    Serial.print("antiorario: ");
    Serial.println(-(rotazione / 2));
  movimento = true;
}