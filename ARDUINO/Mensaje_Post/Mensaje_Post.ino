#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <ESP8266WebServer.h>  
#include <WiFiClient.h> 
//-------------------VARIABLES GLOBALES--------------------------

const char *ssid = "Fibertel WiFi159 2.4GHz";//"AndroidAP";//"Speedy-0F574D";//"SO Avanzados";
const char *password = "0043442422";//"6761727565";//"SOA.2019";
ESP8266WebServer server(80);   
int flag = 0;
int scanTime = 1; //In seconds
const char* response = "";
const char* modo;
const char* sentido;//Frente-Derecha-Izquierda-Reversa
float angulo;  
const char* confirma;//SI: Se verifica la potencia del beacon y se procesa la ruta - NO: No se realiza ninguna accion y la variable "modo" pasa a tener valor MOD_O
const size_t capacity = JSON_OBJECT_SIZE(3) + 370;
DynamicJsonDocument root(capacity);   
DynamicJsonDocument root_pet(capacity);
char caux;
int flagBasuraSerial;
const int PIN_ENVIO_SERIAL = D1;

String peticionPOSTJSON(String host,String msj){

    String payload = "error";
    String postData = "";
    DeserializationError err;

    HTTPClient http;    //Declare object of class HTTPClient
    postData = "ID=" + String(ESP.getChipId()) + "&MSJ=" + msj;
    http.begin(host);
    http.addHeader("Content-Type", "application/x-www-form-urlencoded");
    int httpcode = http.POST(postData);
    if(httpcode > 0){
      payload = http.getString();
      http.end();
      delay(100);             
      err = deserializeJson(root_pet,payload);   
      
      if (!err) {       
          if(root_pet["codigo"] == "ENVIO"){//{"codigo":"ENVIO","dato":"-D0|F400|D90|F400|D90|F400|D90#"}
            response = root_pet["dato"];// hace copia por referencia, cuando muere la funcion la variable apunta a basura
          }     
          if(root_pet["codigo"] == "REGISTRO"){
            response = root_pet["dato"];
          }            
      }
      else{
          server.send(400);
      }
    }
    return payload;     
}

void cambioModo()
{   
    String post_body = server.arg("plain");       
    DeserializationError err = deserializeJson(root,post_body);
    
    if (!err) {           
        if (server.method() == HTTP_POST){
            if(root["codigo"] == "MODO"){
              modo = root["dato"];
              server.send(200, "application/json", "{'opcion':'CAMBIO DE MODO'}");   
            }             
        }
    }
    else{
        server.send(400);
    }
}

 void config_rest_server_routing() {
    server.on("/mode", HTTP_POST, cambioModo);
}

void setup() {

  pinMode(PIN_ENVIO_SERIAL,OUTPUT);
  
  
  Serial.begin(115200);
  
  WiFi.begin (ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando...");
  }

  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  peticionPOSTJSON("http://www.gestiondenegocio.esy.es/registrar_plataforma",WiFi.localIP().toString().c_str());
  config_rest_server_routing();
  server.begin();                                              //Inicializa el servidor (una vez configuradas las rutas)
  Serial.println("Servidor HTTP inicializado"); 
  modo = "MOD_O";
}

void loop() {
  //peticionPOSTJSON(13,1,"OBSTACULO"); //metodo de envio de notificación
  server.handleClient(); //espero a que algun cliente se conecte y realice una peticion

  if(String(modo) == "MOD_E"){
       
  }
  if(String(modo) == "MOD_O"){
    peticionPOSTJSON("http://www.gestiondenegocio.esy.es/obtener_peticion","");
    //peticionPOSTJSON("obstaculo","OBSTACULO");
    //peticionPOSTJSON("liberar_plataforma","LLEGADA");
    if(response != ""){  
      digitalWrite(PIN_ENVIO_SERIAL,HIGH);
      delay(100);
      digitalWrite(PIN_ENVIO_SERIAL,LOW);   
      //enviar un pulso por algun pin para avisar el envio de una ruta
      //enviar "response" por serial
      //aguardar algun pulso proveniente de arduino para avisar que ejecuto la ruta
      Serial.write(response);
      response = "";
    }
  }
  
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
          peticionPOSTJSON("http://www.gestiondenegocio.esy.es/obstaculo","OBSTACULO");
          caux='a';
        }
      }
  }
  //delay(10000);
  //peticionPOSTJSON("http://www.gestiondenegocio.esy.es/liberar_plataforma","LLEGADA");
}
