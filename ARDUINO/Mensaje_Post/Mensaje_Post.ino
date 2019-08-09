#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <WiFiClient.h> 
#include <ESP8266WebServer.h>  
#include <WiFiManager.h>

//-------------------VARIABLES GLOBALES--------------------------
int contconexion = 0;

const char *ssid = "Fibertel WiFi159 2.4GHz";//"AndroidAP";//"Speedy-0F574D";//"Fibertel WiFi159 2.4GHz";//"SO Avanzados";//"Fibertel WiFi159 2.4GHz";
const char *password = "0043442422";//"matiasmanda";//"6761727565";//"0043442422";//"SOA.2019";//"0043442422";

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
  
    Serial.println("Esperando respuesta del servidor...");
    
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
    return linea;  
}

void post_method(String post_body)
{
    //StaticJsonBuffer<500> jsonBuffer;
    //String post_body = server.arg("plain");
    Serial.println(post_body);
    const size_t capacity = JSON_OBJECT_SIZE(3);// + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
    //DynamicJsonDocument root(capacity);
    StaticJsonDocument<capacity>  root;
    DeserializationError err = deserializeJson(root,post_body);
    
    if (!err) 
    {   
        if (server.method() == HTTP_POST)
        {
            if(root["codigo"] == "PETICION"){
              char* ruta = root["dato"];
              Serial.println(ruta);
              //server.send(200, "text/plain", "Conexión OK");         
              } // 1           
        }
    }
    else
    {
        Serial.println("error in parsin json body");
        server.send(400);
    }
    
}


void setup() {
  
  // Inicia Serial
  Serial.begin(115200);
  Serial.println("");
  pinMode(PinARD,OUTPUT);
  pinMode(PinARDIn,INPUT);
  chipid = String(ESP.getChipId());
  //pinMode(interruptPin, INPUT_PULLUP);
  //attachInterrupt(interruptPin, counter, RISING); // Configuración de la interrupción 0, donde esta conectado. 
  WiFi.begin (ssid, password); 
  //wifimanager.resetSettings();
  wifimanager.autoConnect("BePIM");

  
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando...");
  }
  Serial.printf("\nConectado a la red: %s", WiFi.SSID().c_str());                   //Imprime en el pto. serie el nombre de la red WiFi de conexión
  Serial.printf("\nDirección IP: http://%s\n", WiFi.localIP().toString().c_str());  //Imprime en el pto. serie la dirección IP asignada por el router
  
  unsigned long currentMillis = millis();
  
  /**En esta parte deberia estar una funcion que reconfigure la plataforma cada vez que se encienda**/
  /**Deberia cargar las rutas posibles**/ 
  if(con == false){//registro la plataforma en el servidor por unica vez
    String resp = peticionPOSTJSON(chipid);//recibir json con vector de rutas
    Serial.println(resp);
    if(resp.equals("ok")){//verifico que el servidor devuelva ok
      Serial.println("Conexion Exitosa con el id: " + chipid);
      con = true;// si devuelve ok, dejo de enviarle peticiones
    }
  }
  
  config_rest_server_routing();
  server.begin();                                              //Inicializa el servidor (una vez configuradas las rutas)
  Serial.println("Servidor HTTP inicializado");  

}

void loop() {
  String response = peticionPOSTJSON();
  post_method(response);

}
