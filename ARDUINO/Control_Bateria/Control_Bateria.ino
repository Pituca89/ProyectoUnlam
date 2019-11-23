// Placa NodeMCU 0.9


#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <ESP8266WebServer.h>  
#include <WiFiClient.h> 
#include <Wire.h>  //para voltimetro
#include <WiFiManager.h>
//-------------------VARIABLES GLOBALES--------------------------

const char *ssid = "Bepim";//"Fibertel WiFi159 2.4GHz";//"Javo wifi";//"Fibertel WiFi159 2.4GHz";//"AndroidAP";//"Speedy-0F574D";//"SO Avanzados";
const char *password = "12345678";//"0043442422";//"44540006";//"0043442422";//"6761727565";//"SOA.2019";
ESP8266WebServer server(80);   
int flag = 0;
int scanTime = 1; //In seconds
const char* response = "";
const char* modo;
const char* sentido;//Frente-Derecha-Izquierda-Reversa
float angulo;  
int MAX_BUFFER = 50;
const char* confirma;//SI: Se verifica la potencia del beacon y se procesa la ruta - NO: No se realiza ninguna accion y la variable "modo" pasa a tener valor MOD_O
const size_t capacity = JSON_OBJECT_SIZE(3) + 370;
DynamicJsonDocument root(capacity);   
DynamicJsonDocument root_pet(capacity);
char caux;
int flagBasuraSerial;
const int PIN_ENVIO_SERIAL = D1;

#define VoltajeBateriaNivelAlto 12.8
#define VoltajeBateriaNivelBajo 10

WiFiManager wifimanager;
//voltimetro
int lectura;
float voltaje;
int porcentaje;
unsigned long startMillis;  //some global variables available anywhere in the program
unsigned long currentMillis;
const unsigned long period = 5000;
String url = "http://www.gestiondenegocio.esy.es/";
String peticionPOSTJSON(String host,String msj){

    String payload = "error";
    String postData = "";
    url.concat(host);
    DeserializationError err;

    HTTPClient http;    //Declare object of class HTTPClient
    postData = "ID=" + String(ESP.getChipId()) + "&MSJ=" + msj;
    http.begin(url);
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
    url = "http://www.gestiondenegocio.esy.es/";
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

	startMillis = millis();//para voltimetro
  pinMode(PIN_ENVIO_SERIAL,OUTPUT);
  
  
  Serial.begin(115200);
  
  //WiFi.begin (ssid, password);
  
  wifimanager.autoConnect("BePIM");
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando...");
  }

  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  peticionPOSTJSON("registrar_plataforma",WiFi.localIP().toString().c_str());
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
    peticionPOSTJSON("obtener_peticion","");
    //peticionPOSTJSON("obstaculo","OBSTACULO");
    //peticionPOSTJSON("liberar_plataforma","LLEGADA");
    if(response != ""){  
      digitalWrite(PIN_ENVIO_SERIAL,HIGH);
      delay(100);
      digitalWrite(PIN_ENVIO_SERIAL,LOW);   
      //enviar un pulso por algun pin para avisar el envio de una ruta
      //enviar "response" por serial
      //aguardar algun pulso proveniente de arduino para avisar que ejecuto la ruta
      
      String instruccion = String(response);
      if(instruccion.length() <= MAX_BUFFER){
        Serial.print(instruccion);
      }else{
        int division = instruccion.length() / MAX_BUFFER;
        int resto = instruccion.length() % MAX_BUFFER;
        int cantEnvios = division + resto;
        int cantEnviosAux = 0;
        int index = 0;
        char aux[MAX_BUFFER];
        while(cantEnviosAux < cantEnvios - 1){
          String substr = instruccion.substring(index,index + MAX_BUFFER);
          //substr.toCharArray(aux,MAX_BUFFER);
          Serial.flush();
          Serial.write(substr.c_str());
          Serial.flush();
          index += MAX_BUFFER;
          cantEnviosAux++;
          delay(1000);
        }
        String substr = instruccion.substring(index,instruccion.length());
        //substr.toCharArray(aux,MAX_BUFFER);
        Serial.write(substr.c_str());
      }   
      
      //Serial.write(response);
      response = "";
    }
  }
  
  if (Serial.available()) {
      flagBasuraSerial=0;
      //while (Serial.available() && caux != '1'&& caux != '2'&& caux != '3'&& caux != '4' && caux != '5')   //while (Serial.available() && caux != '#' && flagSerial == 1)
      while (Serial.available())
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
            peticionPOSTJSON("notificacion","OBSTACULO");
            caux='a';
          }
        if (caux == '2')
        {
          peticionPOSTJSON("notificacion","LLEGO A DESTINO");
          caux='a';
        }
        if (caux == '3')
        {
          //Serial.println("se detecto 3 por serial");
          peticionPOSTJSON("notificacion","VERIFICAR UBICACION PLATAFORMA");
          caux='a';
        }       
        if (caux == '4')
        {
          //Serial.println("se detecto 4 por serial");
          peticionPOSTJSON("notificacion","PLATAFORMA DETENIDA POR OBSTACULO");
          caux='a';
        }
        if (caux == '5')
        {
          //Serial.println("se detecto 5 por serial");
          peticionPOSTJSON("notificacion","PLATAFORMA DETENIDA POR OBSTACULO");
          caux='a';
        }
        delay(1000);
      }
    }
	
	
//VOLTIMETRO	z
	currentMillis = millis();  
	if (currentMillis - startMillis >= period){
		lectura=analogRead(A0);
		voltaje=((lectura/6.544)/10);             //si alimentamos el modulo con 5v se divide por 4.092 , si alimentamos con 3,5V es 6.544
		//Serial.print("Voltaje: ");
		//Serial.print(voltaje);
		//Serial.print("V   -");
		if(voltaje>VoltajeBateriaNivelAlto){
			porcentaje=100;
		}
		else{
			if(voltaje<VoltajeBateriaNivelBajo){
				porcentaje=0;
			}
			else{
				porcentaje=(int)(((voltaje-VoltajeBateriaNivelBajo)*100)/2);			
			}
		}
    peticionPOSTJSON("bateria",String(porcentaje));
		//Serial.print("Porcentaje de bateria: ");
		//Serial.print(porcentaje);
		//Serial.println("%");
		startMillis = currentMillis;
	}
}
