#include <ArduinoJson.h>
#include <WiFi.h>
#include <WiFiClient.h>
#include <WebServer.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>
//-------------------VARIABLES GLOBALES--------------------------

const char *ssid = "Javo wifi";//"Fibertel WiFi159 2.4GHz";//"AndroidAP";//"Speedy-0F574D";//"SO Avanzados";
const char *password = "44540006";//"0043442422";//"matiasmanda";//"6761727565";//"SOA.2019";
WebServer server(80);   
int flag = 0;
int best = -40;
int scanTime = 1; //In seconds
const char* response = "";
const char* modo;
const char* sentido;//Frente-Derecha-Izquierda-Reversa
const char* macBeacon;//Se verifica al finalizar el entrenamiento una vez confirmado
float angulo;  
const char* confirma;//SI: Se verifica la potencia del beacon y se procesa la ruta - NO: No se realiza ninguna accion y la variable "modo" pasa a tener valor MOD_O
const size_t capacity = JSON_OBJECT_SIZE(4) + JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
DynamicJsonDocument root(capacity);   
DynamicJsonDocument root_pet(capacity);  
String peticionPOSTJSON(int op,int codigo,String dato){
    
    char* host = "www.gestiondenegocio.esy.es";//"192.168.1.34";//"www.gestiondenegocio.esy.es";   
    String linea = "error";
    DeserializationError err;
    root_pet["OPCION"] = op;//12;
    root_pet["ID"] = ESP.getEfuseMac();
    root_pet["COD"] = codigo;
    root_pet["MSJ"] = dato;
    WiFiClient http;    //Declare object of class HTTPClient
    if (!http.connect(host, 80)) {
      Serial.println("Fallo de conexion");
      return linea;
    }
    http.print("POST"); http.print(" "); http.print("/"); http.println(" HTTP/1.1");
    http.print("HOST: "); http.println(host);
    //http.println("User-Agent: BePIM");
    size_t len = measureJson(root_pet);
    http.println("Content-Type: application/json");
    http.print("Content-Length: "); http.println(len);
    http.println("Connection: close");
    http.println();
    serializeJson(root_pet,http);

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
    
    err = deserializeJson(root_pet,linea);   
    
    if (!err) 
    {       
        if(root_pet["codigo"] == "ENVIO"){//{"codigo":"ENVIO","dato":"-D0|F400|D90|F400|D90|F400|D90#"}
          response = root_pet["dato"];// hace copia por referencia, cuando muere la funcion la variable apunta a basura
          Serial.println(response);
        }
        /**Solo sirve para saber las respuesta del registro y de las notificaciones**/
        //if(JSONencoder["codigo"] == "REGISTRO"){
        //  response = JSONencoder["dato"];
        //}     
        //if(JSONencoder["codigo"] == "MENSAJE"){
        //  response = JSONencoder["dato"];
        //}                     
    }
    else
    {
        Serial.println("error in parsin json body");
        server.send(400);
    }
    return linea;  
}

void entrenamientoPOST()
{   
   String post_body = server.arg("plain");        
    DeserializationError err = deserializeJson(root_pet,post_body);
    
    if (!err) {  
         
        if (server.method() == HTTP_POST){
            if(root_pet["opcion"] == "INST"){
              sentido = root_pet["sentido"];
              //macBeacon = root_pet["mac"];
              //confirma = root_pet["confirma"];  
              //angulo = root_pet["angulo"];           
              server.send(200, "text/plain", "OK");         
            } // 1             
        }
    }
    else{
        server.send(400);
    }
}

void cambioModo()
{   
    String post_body = server.arg("plain");       
    DeserializationError err = deserializeJson(root,post_body);
    
    if (!err) {  
         
        if (server.method() == HTTP_POST){
            if(root["codigo"] == "MODO"){
              modo = root["dato"];
              server.send(200, "text/plain", "CAMBIO DE MODO");   
            }             
        }
    }
    else{
        server.send(400);
    }
}
int validarBeacon(String mac){
  BLEDevice::init("");
  BLEScan* pBLEScan = BLEDevice::getScan(); //create new scan
  pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
  BLEScanResults foundDevices = pBLEScan->start(scanTime);
  
  for (int i = 0; i < foundDevices.getCount(); i++) {
    BLEAdvertisedDevice device = foundDevices.getDevice(i);
    int rssi = device.getRSSI();
    BLEAddress macAdd = device.getAddress();  
    Serial.println(macAdd.toString().c_str());
    if (rssi > best) {
      Serial.println("Encontrado!!");
    }
  }
}
 void config_rest_server_routing() {
    server.on("/training", HTTP_POST, entrenamientoPOST);
    server.on("/mode", HTTP_POST, cambioModo);
}
void setup() {

  Serial.begin(115200);
  
  WiFi.begin (ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando...");
  }

  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  peticionPOSTJSON(2,0,WiFi.localIP().toString().c_str());
  config_rest_server_routing();
  server.begin();                                              //Inicializa el servidor (una vez configuradas las rutas)
  Serial.println("Servidor HTTP inicializado"); 
  modo = "MOD_O";
}

void loop() {

  //peticionPOSTJSON(13,1,"OBSTACULO"); //metodo de envio de notificación
  server.handleClient(); //espero a que algun cliente se conecte y realice una peticion
  Serial.println(modo);
  if(String(modo) == "MOD_E"){    
    Serial.println(sentido);
  }
  if(String(modo) == "MOD_O"){
    peticionPOSTJSON(12,0,"");
    if(response != ""){     
      //enviar un pulso por algun pin para avisar el envio de una ruta
      //enviar "response" por serial
      //aguardar algun pulso proveniente de arduino para avisar que ejecuto la ruta
      response = "";
    }
  }
}
