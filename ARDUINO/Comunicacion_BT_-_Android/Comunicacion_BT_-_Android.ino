/*Program to control LED (ON/OFF) from ESP32 using Serial Bluetooth
 * Thanks to Neil Kolbans for his efoorts in adding the support to Arduino IDE
 * Turotial on: www.circuitdigest.com 
 */
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>
#include "BluetoothSerial.h" //Header File for Serial Bluetooth, will be added by default into Arduino
#define ventana 10
#define vecesDeBusqueda 3

BluetoothSerial ESP_BT; //Object for Bluetooth
int scanTime = 1; //In seconds
char dato = ' ';
int best = -40;
char mac[18];
//int LED_BUILTIN = 2;

const int PinEntrenamiento = 33;
const int ValidacionBeaconDestino = 12;
const int ResultadoBeaconDestino = 14;
const int Pin2binario = 27;
const int Pin1binario = 26;
const int Pin0binario = 25;

String validarBeacon(char* mac){
  String potencia = "";
  BLEDevice::init("");
  BLEScan* pBLEScan = BLEDevice::getScan(); //create new scan
  pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
  BLEScanResults foundDevices = pBLEScan->start(scanTime);
  
  for (int i = 0; i < foundDevices.getCount(); i++) {
    BLEAdvertisedDevice device = foundDevices.getDevice(i);  
    BLEAddress macAdd = device.getAddress();  
    
    String dato1 = String(mac);
    dato1.toLowerCase();//equalsIgnoreCase()
    String dato2 = String(macAdd.toString().c_str());
    if (dato1 == dato2) {
      potencia = String(device.getRSSI(),DEC);
      //Serial.println("Encontrado!!");
      //Serial.println(macAdd.toString().c_str());
    }
  }
  return potencia;
}

int validarBeaconLlegada(char* mac, int pot){
  int encontrado = 2;
  BLEDevice::init("");
  BLEScan* pBLEScan = BLEDevice::getScan(); //create new scan
  pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
  BLEScanResults foundDevices = pBLEScan->start(scanTime);
  
  for (int i = 0; i < foundDevices.getCount(); i++) {
    BLEAdvertisedDevice device = foundDevices.getDevice(i);  
    BLEAddress macAdd = device.getAddress();  
    
    String dato1 = String(mac);
    dato1.toLowerCase();//equalsIgnoreCase()
    String dato2 = String(macAdd.toString().c_str());
    Serial.print("Beacons econtrados: ");
    Serial.println(device.getAddress().toString().c_str());
    Serial.print("Potencia");
    Serial.println(device.getRSSI());
    Serial.print("Ventana ");
    Serial.print((pot - ventana));Serial.print(" ");Serial.println((pot + ventana));
    if (dato1 == dato2) {
      Serial.println("Encontre Beacon");
      if(device.getRSSI() >= (pot - ventana) && device.getRSSI() <= (pot + ventana)){
        
        Serial.print("Potencia detectada del Beacon: ");
        Serial.println(dato2);
        Serial.print(" Valor: ");
        Serial.println(device.getRSSI());
        encontrado = 1;
        }
       else{
        encontrado = 0;
        }
    }
  }
  return encontrado;
}
void setup() {
  Serial.begin(115200); //Start Serial monitor in 9600
  //uint64_t chip = ESP.getEfuseMac();
  ESP_BT.begin("BePIM_"); //Name of your Bluetooth Signal
  //Serial.println("Dispositivo emparejado");
  //mac = ' ';
  //pinMode (LED_BUILTIN, OUTPUT);//Specify that LED pin is output
  
pinMode(PinEntrenamiento,OUTPUT); 
pinMode(ValidacionBeaconDestino,OUTPUT);
pinMode(ResultadoBeaconDestino,OUTPUT);
pinMode(Pin2binario,OUTPUT);
pinMode(Pin1binario,OUTPUT); 
pinMode(Pin0binario,OUTPUT);

digitalWrite(ValidacionBeaconDestino,LOW);
digitalWrite(ResultadoBeaconDestino,LOW);
digitalWrite(Pin2binario,LOW);
digitalWrite(Pin1binario,LOW);
digitalWrite(Pin0binario,LOW);
digitalWrite(PinEntrenamiento,LOW); 

}

void loop() {
  digitalWrite(ValidacionBeaconDestino,LOW);
  digitalWrite(ResultadoBeaconDestino,LOW);
  if(Serial.available()){
    
    while(Serial.available()){  
      
        dato = Serial.read();
        if (dato == 'M'){
          dato = Serial.read();
          int i = 0;
          while(dato != 'P'){
            mac[i] = dato;
            i++;
            dato = Serial.read();
           } 
          int j = 0;
          String potencia = "";
          dato = Serial.read();
          while(dato != '$'){
            potencia.concat(dato);
            j++;
            dato = Serial.read();
           } 
           Serial.print("MAC: ");
           Serial.println(mac);
           Serial.print(" Potencia: ");
           Serial.println(potencia);
           int busqueda = 0;
           int llegada = 2;
           
           while(busqueda < vecesDeBusqueda && llegada == 2){
            llegada = validarBeaconLlegada(mac,potencia.toInt());
            Serial.print("Salida validacion: ");
            Serial.println(llegada);
            Serial.println("-----------------------------------------------------------------");
            busqueda++;
           }
           if(llegada == 1){
              Serial.print("Reconoce el Beacon");
              digitalWrite(ResultadoBeaconDestino,HIGH);
            }else{
              Serial.print("No reconoce el Beacon");
              digitalWrite(ResultadoBeaconDestino,LOW);
            } 
         digitalWrite(ValidacionBeaconDestino,HIGH); 
         delay(2000);
        }
    }     
  }
  
  while (ESP_BT.available()) //Check if we receive anything from Bluetooth
  {
   digitalWrite(PinEntrenamiento,HIGH);  
    /*  codigo binario de entrenamiento Pin2binario Pin1binario Pin0binario
             *  0 0 0 : plataforma detenida (S)
             *  0 0 1 : avanzar (F)
             *  0 1 0 : girar derecha (D)
             *  1 0 0 : girar izquierda (I)
             *  0 1 1 : deshacer ultimo movimiento (U)   ANDO segun German
             *  1 1 1 : deshacer TODO (W)   OL ANDO segun German
             */
    dato = ESP_BT.read();

    if (dato == 'X')
        {        
        ESP_BT.println("OK");
        }  
    if (dato == 'S')
        {        
        //ESP_BT.println("OK");
        //Serial.println("S");
        digitalWrite(Pin2binario,LOW);
        digitalWrite(Pin1binario,LOW);
        digitalWrite(Pin0binario,LOW);
        }  
    if (dato == 'F')
        {        
        //ESP_BT.println("Avanzando...");
        //Serial.println("F");
        digitalWrite(Pin2binario,LOW);
        digitalWrite(Pin1binario,LOW);
        digitalWrite(Pin0binario,HIGH); 
        }       
    if (dato == 'D')
        {
        //ESP_BT.println("Girando a la derecha...");
        //Serial.println("D");
        digitalWrite(Pin2binario,LOW);
        digitalWrite(Pin1binario,HIGH);
        digitalWrite(Pin0binario,LOW);
        }     
    if (dato == 'I')
        {
        //ESP_BT.println("Girando a la izquierda...");
        //Serial.println("I");
        digitalWrite(Pin2binario,HIGH);
        digitalWrite(Pin1binario,LOW);
        digitalWrite(Pin0binario,LOW);
        }  
    if (dato == 'U')
        {
        //ESP_BT.println("Deshacer ultima orden");
        //Serial.println("U");
        digitalWrite(Pin2binario,LOW);
        digitalWrite(Pin1binario,HIGH);
        digitalWrite(Pin0binario,HIGH);
        delay(100);
        digitalWrite(Pin2binario,LOW);
        digitalWrite(Pin1binario,LOW);
        digitalWrite(Pin0binario,LOW);
        }  
    if (dato == 'W')
        {
        //ESP_BT.println("Deshacer toda la ruta");
        //Serial.println("W");
        digitalWrite(Pin2binario,HIGH);
        digitalWrite(Pin1binario,HIGH);
        digitalWrite(Pin0binario,HIGH);
        } 

    if (dato == 'P')
        {
          dato = ESP_BT.read();
          int i = 0;
          while(dato != '#'){
            mac[i] = dato;
            i++;
            dato = ESP_BT.read();
           }
          int Banderapcia = 0;
          String pcia;
          while(Banderapcia == 0)
            { 
            pcia = validarBeacon(mac);
            if(pcia != "")
              {
              //ESP_BT.println("P|" + pcia);
              Banderapcia = 1;
              }
          else{
              ESP_BT.println("ERROR");
              }
            }
         digitalWrite(PinEntrenamiento,LOW);
         ESP_BT.println(Serial.readStringUntil('$')+"P|" + pcia);
        }  
  }


  delay(20);
}
