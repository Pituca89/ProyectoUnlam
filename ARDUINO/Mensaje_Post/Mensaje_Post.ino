#include <ArduinoJson.h>
#include <WiFi.h>
#include <WiFiClient.h>
#include <WebServer.h>

//-------------------VARIABLES GLOBALES--------------------------

const char *ssid = "Fibertel WiFi159 2.4GHz";//"Javo wifi";//"AndroidAP";//"Speedy-0F574D";//"SO Avanzados";
const char *password = "0043442422";//"44540006";//"matiasmanda";//"6761727565";//"SOA.2019";
WebServer server(80);   
int flag = 0;
//WiFiManager wifimanager;
String peticionPOSTJSON(int op,int codigo,String dato){
  
    const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
    char* host = "www.gestiondenegocio.esy.es";//"192.168.1.34";//"www.gestiondenegocio.esy.es";
    const char* response = "";
    String linea = "error";
    DynamicJsonDocument JSONencoder(capacity);
    DeserializationError err;
    JSONencoder["OPCION"] = op;//12;
    JSONencoder["ID"] = ESP.getEfuseMac();
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
}

void loop() {
  /**Metodo que se ejecuta para el entrenamiento, en donde el modulo queda a la espera de la proxima instruccion**/
  //server.handleClient(); //espero a que algun cliente se conecte y realice una peticion
  /**Metodo que se ejecuta durante la operativa**/
  peticionPOSTJSON(12,0,"");
  if(flag == 0){
    peticionPOSTJSON(13,1,"OBSTACULO");
    flag = 1;
  }
  
  delay(2000);

}
