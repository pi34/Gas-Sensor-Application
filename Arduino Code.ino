#include <SoftwareSerial.h>
SoftwareSerial espSerial(2, 3);

int buzzer = 9;
int LED_green = 10;
int LED_red = 11;
int sensor = A5;
int Threshold = 350;


void setup() {
  // put your setup code here, to run once:

  pinMode(sensor, INPUT);
  pinMode(buzzer, OUTPUT);
  pinMode(LED_green, OUTPUT);
  pinMode(LED_red, OUTPUT);
  
  Serial.begin(9600);
  espSerial.begin(115200);

  delay(200);

  if (espSerial.available()) {
    Serial.print("Esp Available!");
  }

  delay(2000);

  espSerial.println("AT+CWMODE=1");
  delay(10000);

  espSerial.println("AT+CWJAP=\"kapil2001\",\"abc1234!!\"");
  delay(20000);

  

}

void loop() {
  // put your main code here, to run repeatedly:

  int SensorValue = analogRead(sensor);

  espSerial.println("AT+CIPMUX=0");
  delay(2000);

  espSerial.println("AT+CIPSTART=\"TCP\",\"api.thingspeak.com\",80");
  delay(2000);

  espSerial.println("AT+CIPSEND=51");
  delay(2000);

  espSerial.println("GET /update?api_key=6FPWPAAUFAK2KVEM&field1="+String(SensorValue));
  delay(2000);

  espSerial.println("AT+CIPCLOSE");
  delay(2000);


  if (SensorValue > Threshold)
  {
    digitalWrite(LED_red, HIGH);
    digitalWrite(LED_green, LOW);
    tone(buzzer, 2000, 200);
  }
  else
  {
    digitalWrite(LED_green, HIGH);
    digitalWrite(LED_red, LOW);
    noTone(buzzer);
  }
  
  delay(2000);
  
}
