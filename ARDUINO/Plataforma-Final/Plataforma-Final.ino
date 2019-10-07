const int dirPinIZ = 30; 
const int stepPinIZ =31 ; 
const int dirPinDER = 32; 
const int stepPinDER =33 ; 

const int PinEntrenamiento = 22;
const int Pin2binario = 28;
const int Pin1binario = 26;
const int Pin0binario = 24;

const int PinProxiFrontal = 52;     // Pin para el sensor de proximidad frontal
const int PinProxiIzquierdo = 51;     // Pin para el sensor de proximidad Izquierdo
const int PinProxiDerecho = 53;     // Pin para el sensor de proximidad Derecho
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
String macBeaconDestino;
int potenciaBeaconDestino;
  
int h;
int j;
unsigned int k;
int m;
int flagSerial;
int flagBasuraSerial;
int flagPrimerInstruccionEntrenamiento;
int contObstaculo;

#define delaiPulsos 2500       // microsegundos entre pulsos, menor numero mayor velocidad de giro 
#define desvioObstaculo 800    // pasos MAXIMOS de desvio para esquivar un obstaculo


void setup() {
pinMode(stepPinIZ,OUTPUT); 
pinMode(dirPinIZ,OUTPUT);
pinMode(stepPinDER,OUTPUT); 
pinMode(dirPinDER,OUTPUT);

pinMode(PinProxiFrontal,INPUT); 
pinMode(PinProxiIzquierdo,INPUT); 
pinMode(PinProxiDerecho,INPUT); 

pinMode(PinEntrenamiento,INPUT); 
pinMode(Pin2binario,INPUT);
pinMode(Pin1binario,INPUT); 
pinMode(Pin0binario,INPUT);
rutaEntrenamiento="";

Serial2.begin(115200);      //Comunicacion con placa Bluethoot para enviar rutas modo entrenamiento
Serial.begin(9600);       //debagueo
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
macBeaconDestino="";
potenciaBeaconDestino=0;
}


void loop() {
unsigned int ContPasos = 0;
MotorStop();

if(flagSerial==1)
  {
  h=0;
  ruta[h].sentido = 'Q';      //para que no imprima por serial en el debagueo cuando no recibio nada
  macBeaconDestino="";
  potenciaBeaconDestino=0;
  if (Serial1.available()) 
    {
    Serial.println("MODO OPERACION");
    while (Serial1.available() && caux != '$' && flagSerial == 1)
      {
      if (flagBasuraSerial == 0)
        {
        Serial.println("en busca del -");
        do{
          caux = Serial1.read();
          //Serial.println(caux);
          }while (caux != '-');
        flagBasuraSerial=1;
        Serial.println("detecto -   INICIO DE CADENA");
        }
      //Serial.println("ENTRO AL WHILE"); 
      //ruta[h].sentido=Serial.write(Serial1.read());
      ruta[h].sentido=Serial1.read();
      j=0;
      do{
        //caux = Serial.write(Serial1.read());
        caux = Serial1.read();
        Serial.print(" valor de caux: ");
        Serial.println(caux);
        if (caux != '|' && caux != '$')
          {
          intermedio += caux;
          }
        j++;
        }while (caux != '|' && caux != '$' && j<50);
      //Serial.print("valor de j ");
      //Serial.println(j);
      //delay(1000);
      ruta[h].pasos = intermedio.toInt();
      intermedio= "";
      h++;
      }
     Serial.println("SALIO DEL  WHILE");
    ruta[h].sentido='#';

   //Inicio Guardar la MAC y la potencia del beacon recibida por wifi
    j=0;  
      do{
        caux = Serial1.read();
        if (caux != 'P')
          {
          macBeaconDestino.concat(caux);// += caux;
          }
        j++;
        }while (caux != 'P' && j<50);
      //caux = Serial1.read();  // Leo el "-" entre la potencia y el valor de la misma ejemplo P-67
      j=0;
      intermedio="";
      do{
        caux = Serial1.read();
        if (caux != '#')
          {
          intermedio += caux;
          }
        j++;
        }while (caux != '#' && j<50);
      potenciaBeaconDestino = intermedio.toInt();
      //FIN Guardar la MAC y la potencia del beacon recibida por wifi //
     
      //ruta[h].pasos = intermedio.toInt();
      //intermedio= "";
    flagSerial=0;
    caux="";
    flagBasuraSerial = 0;
    h=0;
    }
  
  //  IMPRIMIR EN SERIAL LA RUTA RECIBIDA POR SERIAL 1 DESDE PLACA WIFI 
  
  //delay(2000);
  h=0;
  while ((ruta[h].sentido != '#')&&(h<30)&&(ruta[h].sentido != 'Q'))  //preguntar por distinto de Q es para que solo imprima cuando recibe datos
    { 
    if(h==0){Serial.println(" IMPRIME en el while LO RECIBIDO: ");}
    Serial.print("Sentido: " );
    Serial.print(ruta[h].sentido);
    Serial.print(" - pasos: " );
    Serial.println(ruta[h].pasos);
    h++;
    }
   if (potenciaBeaconDestino!=0)
     {
     Serial.print("mac: " );
     Serial.println(macBeaconDestino);
     Serial.print("Potencia Beacon de destion: " );
     Serial.println(potenciaBeaconDestino);
     }
  // FIN -- IMPRIMIR EN SERIAL LA RUTA RECIBIDA POR SERIAL 1 DESDE PLACA WIFI //
  
  //delay(1200);
  

  //EJECUCION DE LA RUTA
  while ((ruta[h].sentido != '#')&&(potenciaBeaconDestino!=0))
    {
    k=0;
    if (ruta[h].sentido == 'F')
      {
      Serial.println("Ejecuta avance");
      while (k < ruta[h].pasos)
        {
          /*
        contObstaculo=0;
        if (digitalRead(PinProxiFrontal) == LOW){Serial1.print("-1");Serial.println("OBSTACULO");}    //alerta de obstaculo
          while((digitalRead(PinProxiFrontal) == LOW)&&(contObstaculo<5))    
            {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
            delay(1000);
            contObstaculo++;
            }
        if((contObstaculo==5))
          {
          GirarIzquierda(209);
          m=0;
          while ((digitalRead(PinProxiDerecho) == LOW)&&(m <= desvioObstaculo))
            {
            contObstaculo=0;
            if (digitalRead(PinProxiFrontal) == LOW)
              {Serial1.print("-1");Serial.println("OBSTACULO");    //alerta de obstaculo
              while((digitalRead(PinProxiFrontal) == LOW)&&(contObstaculo<5))    
                {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
                delay(1000);
                contObstaculo++;
                }
              }
            
            }//alerta de obstaculo
            
            Avance();
            m++;
            
            GirarDerecha(209);
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
          GirarDerecha(209);
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
          GirarIzquierda(209);
          k=k+desvioObstaculo;
          }
        */
        Avance();
        k++;
        }
        }
      
    if (ruta[h].sentido == 'D')
      {
      Serial.println("Ejecuta Giro Derecha");
      while (k <= ruta[h].pasos)
        {
        GirarDerecha(1);
        k++;
        }
      }
    if (ruta[h].sentido == 'I')
      {
      Serial.println("Ejecuta Giro Izquierda");
      while (k <= ruta[h].pasos)
        {
        GirarIzquierda(1);
        k++;
        }
      }   
    h++;
    
  
  } h=0; }
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
          while(digitalRead(PinProxiFrontal) == LOW)    
            {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
            }
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
        
        Serial.print("Avance pasos;");
        Serial.print(ContPasos);
        Serial.print("   Ruta parcial: ");
       Serial.println(rutaEntrenamiento);
        }
        
      if ((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == HIGH)&&(digitalRead(Pin0binario) == LOW)) //Girar DERECHA
        {
        ContPasos = 0;
        while((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == HIGH)&&(digitalRead(Pin0binario) == LOW))
          {
          GirarDerecha(1);                        // Gira derecha 1 paso
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
        Serial.print("derecha pasos;");
        Serial.print(ContPasos);
        Serial.print("   Ruta parcial: ");
        Serial.println(rutaEntrenamiento);
        }
        
      if ((digitalRead(Pin2binario) == HIGH)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == LOW)) //Girar IZQUIERDA
        {
        ContPasos = 0;
        while((digitalRead(Pin2binario) == HIGH)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == LOW))
          {
          GirarIzquierda(1);                        // Gira derecha 1 paso
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
       // Serial.print("Izquierda pasos;");
       // Serial.print(ContPasos);
       // Serial.print("   Ruta parcial: ");
       // Serial.println(rutaEntrenamiento);
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



void GirarDerecha(int giro)
{
int x = 0;
digitalWrite(dirPinIZ,HIGH); 
digitalWrite(dirPinDER,LOW); 
do{x++;
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH); 
  delayMicroseconds(delaiPulsos); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  delayMicroseconds(delaiPulsos); 
  }while(x < giro);
}

void GirarIzquierda(int giro)
{
int x = 0;
digitalWrite(dirPinIZ,LOW); 
digitalWrite(dirPinDER,HIGH); 
do{x++;
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH); 
  delayMicroseconds(delaiPulsos); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  delayMicroseconds(delaiPulsos); 
  }while(x < giro);
}
