/*********
  Rui Santos
  Complete project details at https://randomnerdtutorials.com  
*********/

// Load Wi-Fi library
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <WiFiClient.h> 
#include <ESP8266WebServer.h>  
#include <WiFiManager.h>

//-------------------VARIABLES GLOBALES--------------------------
int contconexion = 0;

const char *ssid = "Fibertel WiFi159 2.4GHz";//"SO Avanzados";//"Fibertel WiFi159 2.4GHz";
const char *password = "0043442422";//"SOA.2019";//"0043442422";

unsigned long previousMillis = 0;

char* host = "192.168.0.8";//"www.gestiondenegocio.esy.es";
String strhost = "192.168.0.8";//"www.gestiondenegocio.esy.es";
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
ESP8266WebServer server(80);   
WiFiManager wifimanager;
//-------Funciones--------

/**Falta funcion de seleccion de ruta utilizando el vector de rutas recibido del servidor**/
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
  if(  digitalRead (PinARDIn)) {
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
  //WiFi.begin (ssid, password); 
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
    String resp = peticionPOST("OPERACION=CONEXION&CHIPID=" + chipid);//recibir json con vector de rutas
    Serial.println(resp);
    if(resp.equals("ok")){//verifico que el servidor devuelva ok
      Serial.println("Conexion Exitosa con el id: " + chipid);
      con = true;// si devuelve ok, dejo de enviarle peticiones
    }
  }
  
  server.on("/", [](){                                 //RUTA "/led" DE SOLICITUD HTTP
    mensaje = server.arg("motor");                           //Obtiene el valor enviado de "OPCION" como STRING    
    //opcion = server.arg(1);   
    formulario();                                              //Llamada a la función para enviar el formulario al cliente
  });  
  server.begin();                                              //Inicializa el servidor (una vez configuradas las rutas)
  Serial.println("Servidor HTTP inicializado");  

  pulses = 0;
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
   //Serial.println(millis()/1000);
    if(mensaje.equals("1")){
      //Serial.println("Motor Encendido");  
      contador();
      digitalWrite(PinARD,HIGH);   
      flag = 0;  
    }
    if(mensaje.equals("0")){
      //Serial.println("Motor Apagado");
      if(flag == 0){
        //Serial.print("Cantidad de Vueltas: ");Serial.println(pulses,DEC);
        pulses = 0;
      }
      digitalWrite(PinARD,LOW);
      flag = 1;
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
