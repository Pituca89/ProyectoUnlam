#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <ESP8266WebServer.h>  
#include <WiFiClient.h> 
//-------------------VARIABLES GLOBALES--------------------------

char caux;
int flagBasuraSerial;

const char *ssid = "Javo wifi";//"Fibertel WiFi159 2.4GHz";//"Javo wifi";//"AndroidAP";//"Speedy-0F574D";//"SO Avanzados";
const char *password = "44540006";//"0043442422";//"44540006";//"matiasmanda";//"6761727565";//"SOA.2019";
ESP8266WebServer server(80);   
//WiFiManager wifimanager;
String peticionPOSTJSON(int op,int codigo,String dato){
  
    const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
    char* host = "www.gestiondenegocio.esy.es";//"192.168.1.34";//"www.gestiondenegocio.esy.es";
    const char* response = "";
    String linea = "error";
    DynamicJsonDocument JSONencoder(capacity);
    DeserializationError err;
    JSONencoder["OPCION"] = op;//12;
    JSONencoder["ID"] = ESP.getChipId();//ESP.getEfuseMac();
    JSONencoder["COD"] = codigo;
    JSONencoder["MSJ"] = dato;
    WiFiClient http;    //Declare object of class HTTPClient
    if (!http.connect(host, 80)) {
      Serial.println("Fallo de conexion");
      return linea;
    }
    http.print("POST"); http.print(" "); http.print("/"); http.println(" HTTP/1.1");
    http.print("HOST: "); http.println(host);
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
            response = JSONencoder["dato"];
            Serial.println(response); 
          }
          if(JSONencoder["codigo"] == "REGISTRO"){
            response = JSONencoder["dato"];
            Serial.println(response); 
          }     
          if(JSONencoder["codigo"] == "MENSAJE"){
            response = JSONencoder["dato"];
            Serial.println(response); 
          }       
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
    //StaticJsonBuffer<500> jsonBuffer;
    const char* response = "";
    const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
    String post_body = server.arg("plain");
    Serial.println(post_body);
    DynamicJsonDocument root(capacity);
      
    DeserializationError err = deserializeJson(root,post_body);
    
    if (!err) {   
        if (server.method() == HTTP_POST){
            if(root["opcion"] == "PRUEBA"){
              response = root["codigo"];
              Serial.println(response);
              server.send(200, "text/plain", "Respuesta ESP32");         
              } // 1           
        }
    }
    else{
        Serial.println("error in parsin json body");
        server.send(400);
    }
    
}

 void config_rest_server_routing() {
    server.on("/training", HTTP_POST, entrenamientoPOST);
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

  caux='a';
  flagBasuraSerial = 0;
}

void loop() {

  if (Serial.available()) {
      flagBasuraSerial=0;
      while (Serial.available() && caux != '1'&& caux != '2'&& caux != '3'&& caux != '4')   //while (Serial.available() && caux != '#' && flagSerial == 1)
      {
       if (flagBasuraSerial == 0)
        {
         do{
            caux = Serial.read();      //en busca del inicio de cadena con "-"
          }while (caux != '-');
         flagBasuraSerial=1;
       //Serial.println("detecto -   INICIO DE CADENA");
         }
      
      caux = Serial.read();
      if (caux == '1')
        {
          Serial.println("se detecto 1 por serial");
         peticionPOSTJSON(13,1,"OBSTACULO"); 
        caux='a';
        }
      }
  }
  /**Metodo que se ejecuta para el entrenamiento, en donde el modulo queda a la espera de la proxima instruccion**/
  //server.handleClient(); //espero a que algun cliente se conecte y realice una peticion
  /**Metodo que se ejecuta durante la operativa**/
  //peticionPOSTJSON(12,ESP.getChipId());
  //peticionPOSTJSON(13,1,"OBSTACULO");
  //delay(2000);

}
