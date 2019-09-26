/*Program to control LED (ON/OFF) from ESP32 using Serial Bluetooth
 * Thanks to Neil Kolbans for his efoorts in adding the support to Arduino IDE
 * Turotial on: www.circuitdigest.com 
 */
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>
#include "BluetoothSerial.h" //Header File for Serial Bluetooth, will be added by default into Arduino

BluetoothSerial ESP_BT; //Object for Bluetooth
int scanTime = 1; //In seconds
char dato = ' ';
int best = -40;
char mac[18];
//int LED_BUILTIN = 2;
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
      Serial.println("Encontrado!!");
      Serial.println(macAdd.toString().c_str());
    }
  }
  return potencia;
}
void setup() {
  Serial.begin(9600); //Start Serial monitor in 9600
  //uint64_t chip = ESP.getEfuseMac();
  ESP_BT.begin("BePIM_"); //Name of your Bluetooth Signal
  Serial.println("Dispositivo emparejado");
  //mac = ' ';
  //pinMode (LED_BUILTIN, OUTPUT);//Specify that LED pin is output
}

void loop() {
  
  if (ESP_BT.available()) //Check if we receive anything from Bluetooth
  {
    dato = ESP_BT.read();

    if (dato == 'X')
        {        
        ESP_BT.println("OK");
        }  
    if (dato == 'F')
        {        
        ESP_BT.println("Avanzando...");
        }       
    if (dato == 'D')
        {
        ESP_BT.println("Girando a la derecha...");
        }     
    if (dato == 'I')
        {
        ESP_BT.println("Girando a la izquierda...");
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
          //mac[i]='\n';
          Serial.println("");
          Serial.print("Received: "); Serial.println(mac);
          String pcia = validarBeacon(mac);
          if(pcia != ""){
            ESP_BT.println("P|" + pcia);
          }
          else{
            ESP_BT.println("ERROR");
          }
        }  
  }
  delay(20);
}
