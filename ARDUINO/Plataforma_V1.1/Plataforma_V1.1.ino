#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <WiFiClient.h> 
#include <ESP8266WebServer.h>  
#include <WiFiManager.h>

//-------------------VARIABLES GLOBALES--------------------------
int contconexion = 0;

const char *ssid = "AndroidAP";//"Speedy-0F574D";//"Fibertel WiFi159 2.4GHz";//"SO Avanzados";//"Fibertel WiFi159 2.4GHz";
const char *password = "matiasmanda";//"6761727565";//"0043442422";//"SOA.2019";//"0043442422";

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
//-------Funciones--------

/**Falta funcion de seleccion de ruta utilizando el vector de rutas recibido del servidor**/

String peticionPOSTJSON(String id){
    String linea = "error";
    const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
    DynamicJsonDocument JSONencoder(capacity);
 
    JSONencoder["OPCION"] = 2;
    JSONencoder["ID"] = id;
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
/**Inicio funcion para verificar conexion contra un servidor web**/
String peticionPOST(String datos) {
  String linea = "error";
  WiFiClient client;
  //strhost.toCharArray(host, 49);
  if (!client.connect(host, 80)) {
    Serial.println("Fallo de conexion");
    return linea;
  }

  client.print(String("POST ") + strurl +  " HTTP/1.1" + "\r\n" + 
               "Host: " + strhost + "\r\n" +
                "Accept: */*" + "\r\n" +
               "Content-Length: " + datos.length() + "\r\n" +
               "Content-Type: application/x-www-form-urlencoded" + "\r\n" +
               "\r\n" + datos + "\r\n");    
  /**
    client.print(String("GET ") + strurl + "?" + datos + " HTTP/1.1" + "\r\n" + 
               "Host: " + strhost + "\r\n\r\n");    
  **/       
  delay(100);             
  
  Serial.println("Esperando respuesta del servidor...");
  
  unsigned long timeout = millis();
  while (client.available() == 0) {
    if (millis() - timeout > 5000) {
      Serial.println("Cliente fuera de tiempo!");
      client.stop();
      return linea;
    }
  }
  // Lee todas las lineas que recibe del servidro y las imprime por la terminal serial
  while(client.available()){
    linea = client.readStringUntil('\n');
  }
  return linea;
}
/**Fin Funcion**/
/**Inicio funcion para obtener peticones POST**/
void post_method()
{
    //StaticJsonBuffer<500> jsonBuffer;
    String post_body = server.arg("plain");
    Serial.println(post_body);
    const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
    DynamicJsonDocument root(capacity);
      
    DeserializationError err = deserializeJson(root,post_body);
    
    if (!err) 
    {   
        if (server.method() == HTTP_POST)
        {
            if(root["opcion"] == "PRUEBA"){
              id = root["codigo"];
              Serial.println("Valor: " + id);
              server.send(200, "text/plain", "Conexión OK");         
              } // 1           
        }
    }
    else
    {
        Serial.println("error in parsin json body");
        server.send(400);
    }
    
}
/**Fin Funcion**/
void get_method(){
    mensaje = server.arg("motor");                           //Obtiene el valor enviado de "OPCION" como STRING    
    //opcion = server.arg(1);   
    formulario();  
}
void formulario(){                                             //Va a ser un formulario dinámico (se establecen condiciones que varían el resultado final)                                                       
  String form =  "<!DOCTYPE html>";                            //Declaración del tipo de documento: HTML5
         form += "<html>";                                     //Inicio del documento HTML 
         form += "<head>";                                     //Inicio de la cabecera -inf.sobre el doc.-
         form += "<meta charset=utf-8>";                       //Config. de carácteres utilizada: UTF-8
         form += "<title>BePIM</title>";                       //Título del documento                                           
         form += "</head>";                                    //Fin de la cabecera   
         form += "<body>";                                     //Inicio del cuerpo -contenido visible del doc.-
         //form += "<form action='' method='get'>";
         form += "<p><a href=\"?motor=1\"><button class=\"button\">ON</button></a></p>";  
         form += "<p><a href=\"?motor=0\"><button class=\"button\">OFF</button></a></p>"; 
         //form += "</form>";
         form += "<div>Cantidad de vueltas: " + String(pulses/20) + " En " + String((millis()/1000)) + " Segundos </div>";
         form += "</body>";                                    //Fin del cuerpo
         form += "</html>";                                    //Fin del documento HTML
  server.send(200, "text/html", form);                         //Envío del formulario como respuesta al cliente                                               
}
 void contador(){
  if(  !digitalRead (PinARDIn)) {
    pulses++;
  }  // Suma el pulso bueno que entra.
  else ; 
 } 
 void counter(){
  if(  digitalRead (PinARDIn) && (micros()-debounce > 500) && digitalRead (PinARDIn) ) { 
    // Vuelve a comprobar que el encoder envia una señal buena y luego comprueba que el tiempo es superior a 1000 microsegundos y vuelve a comprobar que la señal es correcta.
    debounce = micros(); // Almacena el tiempo para comprobar que no contamos el rebote que hay en la señal.
    pulses++;
  }  // Suma el pulso bueno que entra.
  else ; 
 } 

 void config_rest_server_routing() {
    server.on("/", HTTP_GET,get_method);  
    //server.on("/connect", HTTP_GET, get_method);
    server.on("/connect", HTTP_POST, post_method);
}
//-------------------------------------------------------------------------

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

//--------------------------LOOP--------------------------------
void loop() {

  server.handleClient(); //espero a que algun cliente se conecte y realice una peticion
  /*
  if(mensaje.equals("PETICION")){//espero el mensaje de la aplicación
    Serial.println("Mensaje recibido");
    mensaje = "";
  }
  */
  if(flag == 0){
    //digitalWrite(D1);
    Serial.print("D0|F400|D90|F400|D90|F400|D90#");
    Serial.println('\n');
    flag = 1;
  }


  delay(1200);
 //Serial.println(millis()/1000);
 if(id == 8){
    //Serial.println("Motor Encendido");  
    digitalWrite(PinARD,HIGH);  
    digitalWrite(PinARD,LOW);   
    id = 0;
  }
  if(id == 0){
    //Serial.println("Motor Apagado");
    digitalWrite(PinARD,LOW);
    id = 100;
  }
}



/*
// Check WiFi Status
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;  //Object of class HTTPClient
    http.begin("http://jsonplaceholder.typicode.com/users/1");
    int httpCode = http.GET();
    //Check the returning code                                                                    
    if (httpCode > 0) {
      const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(3) + JSON_OBJECT_SIZE(5) + JSON_OBJECT_SIZE(8) + 370;
      DynamicJsonDocument root(capacity);
      
     //const char* json = "{\"id\":1,\"name\":\"Leanne Graham\",\"username\":\"Bret\",\"email\":\"Sincere@april.biz\",\"address\":{\"street\":\"Kulas Light\",\"suite\":\"Apt. 556\",\"city\":\"Gwenborough\",\"zipcode\":\"92998-3874\",\"geo\":{\"lat\":\"-37.3159\",\"lng\":\"81.1496\"}},\"phone\":\"1-770-736-8031 x56442\",\"website\":\"hildegard.org\",\"company\":{\"name\":\"Romaguera-Crona\",\"catchPhrase\":\"Multi-layered client-server neural-net\",\"bs\":\"harness real-time e-markets\"}}";
      
      deserializeJson(root,http.getString());
      
      int id = root["id"]; // 1
      const char* name = root["name"]; // "Leanne Graham"
      const char* username = root["username"]; // "Bret"
      const char* email = root["email"]; // "Sincere@april.biz"
      
      JsonObject address = root["address"];
      const char* address_street = address["street"]; // "Kulas Light"
      const char* address_suite = address["suite"]; // "Apt. 556"
      const char* address_city = address["city"]; // "Gwenborough"
      const char* address_zipcode = address["zipcode"]; // "92998-3874"
      
      const char* address_geo_lat = address["geo"]["lat"]; // "-37.3159"
      const char* address_geo_lng = address["geo"]["lng"]; // "81.1496"
      
      const char* phone = root["phone"]; // "1-770-736-8031 x56442"
      const char* website = root["website"]; // "hildegard.org"
      
      JsonObject company = root["company"];
      const char* company_name = company["name"]; // "Romaguera-Crona"
      const char* company_catchPhrase = company["catchPhrase"]; // "Multi-layered client-server neural-net"
      const char* company_bs = company["bs"]; // "harness real-time e-markets"

      // Output to serial monitor
      Serial.print("Name:");
      Serial.println(name);
      Serial.print("Username:");
      Serial.println(username);
      Serial.print("Email:"); 
      Serial.println(email);
    }
    http.end();   //Close connection
  }
  // Delay
  delay(60000);

*/
