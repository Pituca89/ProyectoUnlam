#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <WiFiClient.h> 
#include <ESP8266WebServer.h>  
#include <WiFiManager.h>

//-------------------VARIABLES GLOBALES--------------------------
int contconexion = 0;

const char *ssid = "Javo wifi";//"Fibertel WiFi159 2.4GHz";//"AndroidAP";//"Speedy-0F574D";//"Fibertel WiFi159 2.4GHz";//"SO Avanzados";//"Fibertel WiFi159 2.4GHz";
const char *password = "44540006";//"0043442422";//"matiasmanda";//"6761727565";//"0043442422";//"SOA.2019";//"0043442422";

unsigned long previousMillis = 0;

char* host = "www.gestiondenegocio.esy.es";//"192.168.1.34";//"www.gestiondenegocio.esy.es";
String strhost = "www.gestiondenegocio.esy.es";//"192.168.1.34";//"www.gestiondenegocio.esy.es";
String strurl = "/index.php";
String chipid = "";
boolean con = false;
int cantUser = 0;
String mensaje = "";
String opcion = "";
int PinARD = D1; 
int PinARDIn = D2; 
volatile long pulses = 0;
int flag = 0;
static volatile unsigned long debounce = 0; // Tiempo del rebote.
const int interruptPin = 0;
int id = 100;
String ruta_prueba = "D0|F400|D90|F400|D90|F400|D90#";
ESP8266WebServer server(80);   
WiFiManager wifimanager;


String peticionPOSTJSON(){
  
    String linea = "error";
    const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
    DynamicJsonDocument JSONencoder(capacity);
    DeserializationError err;
    JSONencoder["OPCION"] = 12;
    JSONencoder["ID"] = ESP.getChipId();
    /*
    JsonArray values = JSONencoder.createNestedArray("values"); //JSON array
    values.add(20); //Add value to array
    values.add(21); //Add value to array
    values.add(23); //Add value to array
    */
    WiFiClient http;    //Declare object of class HTTPClient
  if (!http.connect(host, 80)) {
      Serial.println("Fallo de conexion");
      return linea;
    }
    http.print("POST"); http.print(" "); http.print("/"); http.println(" HTTP/1.1");
    http.print("HOST: "); http.println(strhost);
    //http.println("User-Agent: BePIM");
    size_t len = measureJson(JSONencoder);
    http.println("Content-Type: application/json");
    http.print("Content-Length: "); http.println(len);
    http.println("Connection: close");
    http.println();
    serializeJson(JSONencoder,http);

    delay(100);             
     
    unsigned long timeout = millis();
    while (http.available() == 0) {
      if (millis() - timeout > 5000) {
        Serial.println("Cliente fuera de tiempo!");
        http.stop();
        return linea;
      }
    }
    
    // Lee todas las lineas que recibe del servidro y las imprime por la terminal serial
    while(http.available()){
      linea = http.readStringUntil('\n');
    }
    
    err = deserializeJson(JSONencoder,linea);   
    if (!err) 
    {           
        if(JSONencoder["codigo"] == "ENVIO"){//{"codigo":"ENVIO","dato":"-D0|F400|D90|F400|D90|F400|D90#"}
          const char* ruta = JSONencoder["dato"];
          Serial.println(ruta); 
          }          
    }
    else
    {
        Serial.println("error in parsin json body");
        server.send(400);
    }
    return linea;  
}

void setup() {

  Serial.begin(115200);
  WiFi.begin (ssid, password); 
  //wifimanager.resetSettings();
  wifimanager.autoConnect("BePIM");

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando...");
  }

}

void loop() {
  peticionPOSTJSON();
  delay(2000);

}
