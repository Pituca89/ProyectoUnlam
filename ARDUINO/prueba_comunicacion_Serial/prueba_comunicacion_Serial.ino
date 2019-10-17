#include <SoftwareSerial.h>

//Al utilizar la biblioteca SoftwareSerial los pines RX y TX para la transmicion serie de Bluethoot se pueden cambiar mapear a otros pines.
//Sino se utiliza esta bibioteca esto no se puede realizar y se debera conectar al pin 0 y 1, conexion Serie no pudiendo imprmir por el monitor serie
//Al estar estos ocupados.
//SoftwareSerial WifiSerial(10,11); // RX | TX

String ruta = "";
char aux = "";

void setup()
{
    //Se configura la velocidad del puerto serie para poder imprimir en el puerto Serie
    Serial.begin(9600);
    Serial1.begin(115200); 
}

void loop() {
  if(Serial1.available()){
    while(Serial1.available() && aux != '#'){
      aux = Serial1.read();
      ruta.concat(aux);
    }
    Serial.println(ruta);
    ruta = "";
    aux = "";
  }
  delay(1000); 
}
