const int dirPinIZ = 30; 
const int stepPinIZ =31 ; 
const int dirPinDER = 32; 
const int stepPinDER =33 ; 

const int PinEntrenamiento = 22;
const int Pin2binario = 28;
const int Pin1binario = 26;
const int Pin0binario = 24;

const int PinProxiFrontal = 52;     // Pin para el sensor de proximidad frontal
const int intPinSerial = 2;   // pin de interrupcion para recibir ruta por serial1 desde wifi

String rutaEntrenamiento; 

int i=0;

struct rutas
  {   
      char sentido ;    // F; Frente - D; derecha - I; izquierda 
      unsigned int pasos ;       // Numero de pasos
      int grados ;       // Numero de pines analogicos
  } ;
char caux;
rutas ruta[50]; 
String intermedio;
  
int h;
int j;
unsigned int k;
int m;
int flagSerial;
int flagBasuraSerial;
int flagPrimerInstruccionEntrenamiento;
int contObstaculo;

#define delaiPulsos 2500       // microsegundos entre pulsos, menor numero mayor velocidad de giro 
#define desvioObstaculo 300    // pasos para esquivar un obstaculo


void setup() {
pinMode(stepPinIZ,OUTPUT); 
pinMode(dirPinIZ,OUTPUT);
pinMode(stepPinDER,OUTPUT); 
pinMode(dirPinDER,OUTPUT);

pinMode(PinProxiFrontal,INPUT); 

pinMode(PinEntrenamiento,INPUT); 
pinMode(Pin2binario,INPUT);
pinMode(Pin1binario,INPUT); 
pinMode(Pin0binario,INPUT);
rutaEntrenamiento="";

Serial2.begin(115200);      //Comunicacion con placa Bluethoot para enviar rutas modo entrenamiento
Serial.begin(115200);       //debagueo
Serial1.begin(9600);      //Comunicacion con placa wifi para recibir rutas modo operacion

h=0;
j=0;
k=0;
m=0;
caux="";
flagSerial = 0;
flagBasuraSerial = 0;
flagPrimerInstruccionEntrenamiento = 0;
contObstaculo=0;
attachInterrupt(digitalPinToInterrupt(intPinSerial), interrupcionSerial, RISING); //interrupcion del wifi

}


void loop() {
unsigned int ContPasos = 0;
MotorStop();

if(flagSerial==1)
  {
  if (Serial1.available()) 
    {
    Serial.println("MODO OPERACION");
    while (Serial1.available() && caux != '#' && flagSerial == 1)
      {
      if (flagBasuraSerial == 0)
        {
        Serial.println("en busca del -");
        do{
          caux = Serial1.read();
          Serial.println(caux);
          }while (caux != '-');
        flagBasuraSerial=1;
        Serial.println("detecto -   INICIO DE CADENA");
        }
      Serial.println("ENTRO AL WHILE"); 
      //ruta[h].sentido=Serial.write(Serial1.read());
      ruta[h].sentido=Serial1.read();
      j=0;
      do{
        //caux = Serial.write(Serial1.read());
        caux = Serial1.read();
        Serial.print(" valor de caux: ");
        Serial.println(caux);
        if (caux != '|' && caux != '#')
          {
          intermedio += caux;
          }
        j++;
        }while (caux != '|' && caux != '#' && j<50);
      //Serial.print("valor de j ");
      //Serial.println(j);
      //delay(1000);
      ruta[h].pasos = intermedio.toInt();
      intermedio= "";
      h++;
      }
     Serial.println("SALIO DEL  WHILE");
    ruta[h].sentido='#';
    }
  
  //  IMPRIMIR EN SERIAL LA RUTA RECIBIDA POR SERIAL 1 DESDE PLACA WIFI 
  Serial.println(" IMPRIME LO RECIBIDO: ");
  //delay(2000);
  h=0;
  while ((ruta[h].sentido != '#')&&(h<30))
    { 
    if(h==0){Serial.println("ENTRO AL WHILE de imprimir");}
    Serial.print("Sentido: " );
    Serial.print(ruta[h].sentido);
    Serial.print(" - pasos: " );
    Serial.println(ruta[h].pasos);
    h++;
    }
  // FIN -- IMPRIMIR EN SERIAL LA RUTA RECIBIDA POR SERIAL 1 DESDE PLACA WIFI 
  
  //delay(1200);
  flagSerial=0;
  caux="";
  flagBasuraSerial = 0;
  h=0;

  //EJECUCION DE LA RUTA
  while (ruta[h].sentido != '#')
    {
    k=0;
    if (ruta[h].sentido == 'F')
      {
      while (k < ruta[h].pasos)
        {
        contObstaculo=0;
        if (digitalRead(PinProxiFrontal) == LOW){Serial1.print("-1");}    //alerta de obstaculo
          while((digitalRead(PinProxiFrontal) == LOW)&&(contObstaculo<5))    
            {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
            delay(1000);
            contObstaculo++;
            }
        if((contObstaculo==5)&&((k+desvioObstaculo)<ruta[h].pasos))
          {
          m=0;
          while (m <= 205)  //valor empirico de giro a 90 grados
            {
            GirarIzquierda();
            m++;
            }
          m=0;
          while (m <= desvioObstaculo)
            {
            if (digitalRead(PinProxiFrontal) == LOW){Serial1.print("-1");}//alerta de obstaculo
            while((digitalRead(PinProxiFrontal) == LOW))    
              {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
              }
            Avance();
            m++;
            }
          m=0;
          while (m <= 205)  //valor empirico de giro a 90 grados
            {
            GirarDerecha();
            m++;
            }
          m=0;
          while (m <= desvioObstaculo)
            {
            if (digitalRead(PinProxiFrontal) == LOW){Serial1.print("-1");}    //alerta de obstaculo
            while((digitalRead(PinProxiFrontal) == LOW))    
              {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
              }
            Avance();
            m++;
            } 
           m=0;
          while (m <= 205)  //valor empirico de giro a 90 grados
            {
            GirarDerecha();
            m++;
            } 
          
        m=0;
        while (m <= desvioObstaculo)
            {
            if (digitalRead(PinProxiFrontal) == LOW){Serial1.print("-1");}    //alerta de obstaculo
            while((digitalRead(PinProxiFrontal) == LOW))    
              {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
              }
            Avance();
            m++;
            }
         m=0;
          while (m <= 205)  //valor empirico de giro a 90 grados
            {
            GirarIzquierda();
            m++;
            } 
          k=k+desvioObstaculo;
          }
        Avance();
        k++;
        }
      }
    if (ruta[h].sentido == 'D')
      {
      while (k <= ruta[h].pasos)
        {
        GirarDerecha();
        k++;
        }
      }
    if (ruta[h].sentido == 'I')
      {
      while (k <= ruta[h].pasos)
        {
        GirarIzquierda();
        k++;
        }
      }   
    h++;
    }
  }
  
if (digitalRead(PinEntrenamiento) == HIGH)      //Modo entrenamiento
  {
  rutaEntrenamiento="";
  Serial.print("comienzo de entrenamiento: ");
  Serial.println(rutaEntrenamiento);
  while (digitalRead(PinEntrenamiento) == HIGH)
    {
      if ((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == HIGH)) //Avanzar
        { 
        ContPasos = 0;
        while((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == HIGH))
          {
          /*while(digitalRead(PinProxiFrontal) == LOW)    //DESCOMENTAR!!!!!
            {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
            }*/
          Avance();                        // Avanza 1 paso
          ContPasos++;
          }
        if(flagPrimerInstruccionEntrenamiento == 0)
          {
          rutaEntrenamiento.concat("F");
          flagPrimerInstruccionEntrenamiento=1;
          }
          else{
          rutaEntrenamiento.concat("|");
          rutaEntrenamiento.concat("F");
          }
        rutaEntrenamiento.concat(String(ContPasos));
        
        //Serial.print("Avance pasos;");
        //Serial.print(ContPasos);
        //Serial.print("   Ruta parcial: ");
        //Serial.println(rutaEntrenamiento);
        }
        
      if ((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == HIGH)&&(digitalRead(Pin0binario) == LOW)) //Girar DERECHA
        {
        ContPasos = 0;
        while((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == HIGH)&&(digitalRead(Pin0binario) == LOW))
          {
          /*while(digitalRead(PinProxiFrontal) == LOW)
            {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
            }*/
          GirarDerecha();                        // Gira derecha 1 paso
          ContPasos++;
          }
        if(flagPrimerInstruccionEntrenamiento == 0)
          {
          rutaEntrenamiento.concat("D");
          flagPrimerInstruccionEntrenamiento=1;
          }
          else{
          rutaEntrenamiento.concat("|");
          rutaEntrenamiento.concat("D");
          }
        rutaEntrenamiento.concat(String(ContPasos));
        //Serial.print("derecha pasos;");
        //Serial.print(ContPasos);
        //Serial.print("   Ruta parcial: ");
        //Serial.println(rutaEntrenamiento);
        }
        
      if ((digitalRead(Pin2binario) == HIGH)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == LOW)) //Girar IZQUIERDA
        {
        ContPasos = 0;
        while((digitalRead(Pin2binario) == HIGH)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == LOW))
          {
          /*while(digitalRead(PinProxiFrontal) == LOW)
            {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
            }*/
          GirarIzquierda();                        // Gira derecha 1 paso
          ContPasos++;
          }
        if(flagPrimerInstruccionEntrenamiento == 0)
          {
          rutaEntrenamiento.concat("I");
          flagPrimerInstruccionEntrenamiento=1;
          }
          else{
          rutaEntrenamiento.concat("|");
          rutaEntrenamiento.concat("I");
          }
        rutaEntrenamiento.concat(String(ContPasos));
        //Serial.print("Izquierda pasos;");
        //Serial.print(ContPasos);
        //Serial.print("   Ruta parcial: ");
        //Serial.println(rutaEntrenamiento);
        }
 
    }
  rutaEntrenamiento.concat("$");
  //ENVIAR rutaEntrenamiento POR SERIAL 2 al Bluethoot
  Serial.print("Ruta de Entrenamiento: ");
  Serial.println(rutaEntrenamiento);
  Serial2.print(rutaEntrenamiento); 
  flagPrimerInstruccionEntrenamiento=0; 
  }

}

void interrupcionSerial()
  {
  flagSerial=1;
  Serial.println("INTERRUPCION");  
  }


void Avance()
{
digitalWrite(dirPinIZ,HIGH); 
digitalWrite(dirPinDER,HIGH); 

digitalWrite(stepPinIZ,HIGH); 
digitalWrite(stepPinDER,HIGH); 
delayMicroseconds(delaiPulsos); 
digitalWrite(stepPinIZ,LOW);
digitalWrite(stepPinDER,LOW); 
delayMicroseconds(delaiPulsos); 

}

void MotorAntihorario()

{


}



void MotorStop()

{
  digitalWrite (stepPinIZ, LOW);
  digitalWrite (stepPinDER, LOW);
}



void GirarDerecha()
{
digitalWrite(dirPinIZ,HIGH); 
digitalWrite(dirPinDER,LOW); 
//for(int x = 0; x < giro; x++) 
  //{
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH); 
  delayMicroseconds(delaiPulsos); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  delayMicroseconds(delaiPulsos); 
  //}
}

void GirarIzquierda()
{
digitalWrite(dirPinIZ,LOW); 
digitalWrite(dirPinDER,HIGH); 
//for(int x = 0; x < giro; x++) 
  //{
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH); 
  delayMicroseconds(delaiPulsos); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  delayMicroseconds(delaiPulsos); 
  //}
}
