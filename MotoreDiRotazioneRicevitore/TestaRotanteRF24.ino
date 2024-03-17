#include <Stepper.h>

#include <RF24.h>
#include <RF24_config.h>
#include <nRF24L01.h>
#include <printf.h>

#define stepNumberForRotation 2100

Stepper motore(stepNumberForRotation, 7, 8, 9, 10);

uint8_t messaggio[6];

RF24 ricevitore(5, 6);
uint8_t address[][6] = { "00001" };

void setup() {
  Serial.begin(9600);
  if (!ricevitore.begin()) {
    Serial.println(F("radio hardware is not responding!!"));
    while (1) {}  // hold in infinite loop
  }else{
    Serial.println(F("pronto"));
  }
  ricevitore.setPayloadSize(sizeof(messaggio));
  ricevitore.setPALevel(RF24_PA_LOW);
  ricevitore.openReadingPipe(1, address[0]);
  ricevitore.startListening();
}
void loop() {
  if (ricevitore.available()) {
    int payload = ricevitore.getPayloadSize();
    Serial.println(payload);
    ricevitore.read(&messaggio, payload);
    Serial.println("ho ricevuto qualcosa");
    for (int i = 0; i < sizeof(messaggio); i++) {
      Serial.println(messaggio[i]);
    }
  }
}