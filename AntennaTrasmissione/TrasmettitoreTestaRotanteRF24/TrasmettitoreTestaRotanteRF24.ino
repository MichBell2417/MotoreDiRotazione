#include <RF24.h>
#include <RF24_config.h>
#include <nRF24L01.h>
#include <printf.h>

uint8_t messaggioSeriale[4] = { 0, 0, 0, 0 };
uint8_t messaggioWirlessVettore[6];

boolean statoMovimento=false;

RF24 trasmettitore(7, 8);
uint8_t address[][6] = { "00001" };

void setup() {
  Serial.begin(9600);
  while (!Serial.available()) {
  }
  if(Serial.available()){
    do {
    Serial.read();
    }while (Serial.available());
    if (!trasmettitore.begin()) {
      Serial.write(48);
      while (1) {}  // hold in infinite loop
    }else{
      Serial.write(1);
    }
    trasmettitore.setPayloadSize(sizeof(messaggioWirlessVettore));
    trasmettitore.setPALevel(RF24_PA_LOW);
    trasmettitore.openWritingPipe(address[0]);
    trasmettitore.stopListening();
  }
  
}

void loop() {
  if (Serial.available()) {
    Serial.readBytes(messaggioSeriale, 4);
    for (int i = 0; i < sizeof(messaggioSeriale); i++) {
      messaggioWirless(messaggioSeriale[i], i);
    }
    if(trasmettitore.write(&messaggioWirlessVettore, sizeof(messaggioWirlessVettore))){
      Serial.write(2);
    }else{
      Serial.write(48);
    }
  }
}
void messaggioWirless(uint8_t valore, int pos){
  if(pos==0){
   Serial.println("0-1");
    messaggioWirlessVettore[0]='R';
    messaggioWirlessVettore[1]=valore;
    messaggioWirlessVettore[2]=messaggioSeriale[1];
  }else if(pos==2){
   Serial.println("2");
    messaggioWirlessVettore[3]='S';
    messaggioWirlessVettore[4]=valore;
  }else if(pos==3){
    Serial.println("3");
    Serial.println(valore);
    if(statoMovimento==true && valore=='0'){
      Serial.println("cambiato a false");
      messaggioWirlessVettore[5]='S';
      statoMovimento=false;
    }else if(statoMovimento==false && valore=='1'){
      Serial.println("cambiato a true");
      messaggioWirlessVettore[5]='S';
      statoMovimento=true;
    }else{
      messaggioWirlessVettore[5]=0;
    }
  }
}