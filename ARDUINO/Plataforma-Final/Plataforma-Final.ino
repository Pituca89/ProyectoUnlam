const int dirPinIZ = 30; 
const int stepPinIZ = 31 ; 
const int dirPinDER = 32; 
const int stepPinDER = 33 ; 


const int PinEntrenamiento = 22;
const int Pin2binario = 28;
const int Pin1binario = 26;
const int Pin0binario = 24;

const int PinProxiFrontal = 52;     // Pin para el sensor de proximidad frontal
const int PinProxiIzquierdo = 51;     // Pin para el sensor de proximidad Izquierdo
const int PinProxiDerecho = 53;     // Pin para el sensor de proximidad Derecho
const int intPinSerial = 2;   // pin de interrupcion para recibir ruta por serial1 desde wifi

const int validacionBeaconDestino = 23; // Pin donde el bluethoot va a poner un 1 cuando haya procesado la mac y potencia del beacon de destino
const int ResultadoBeaconDestino = 25;


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
String macBeaconDestinoMasPotencia;
int potenciaBeaconDestino;
  
int h;
int j;
unsigned int k;
int m;
int p;
int flagSerial;
int flagBasuraSerial;
int flagPrimerInstruccionEntrenamiento;
int contObstaculo;
int flagObstaculo;
int desvio1;        //variables usadas para contar los pasos que se desvia de la ruta original para esquivar un obstaculo
int desvio2;        //la ruta para esquivar un obstaculo es un cuadrado, hay 3 tramos de desvio a contabilizar
int desvio3;
int flagDesvioIzquierda;

#define delaiPulsos 2500       // microsegundos entre pulsos, menor numero mayor velocidad de giro 
#define desvioObstaculo 400    // pasos MAXIMOS de desvio para esquivar un obstaculo


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

pinMode(validacionBeaconDestino,INPUT);
pinMode(ResultadoBeaconDestino,INPUT);


rutaEntrenamiento="";

Serial2.begin(115200);      //Comunicacion con placa Bluethoot para enviar rutas modo entrenamiento
Serial.begin(9600);       //debagueo
Serial1.begin(115200);      //Comunicacion con placa wifi para recibir rutas modo operacion

h=0;
j=0;
k=0;
//m=0;
p=0;
caux="";
flagSerial = 0;
flagBasuraSerial = 0;
flagPrimerInstruccionEntrenamiento = 0;
flagObstaculo=0;
flagDesvioIzquierda=0;
contObstaculo=0;
attachInterrupt(digitalPinToInterrupt(intPinSerial), interrupcionSerial, RISING); //interrupcion del wifi
macBeaconDestino="";
macBeaconDestinoMasPotencia="";
potenciaBeaconDestino=0;
desvio1=0;        
desvio2=0;        
desvio3=0;
}


void loop() {
unsigned int ContPasos = 0;
MotorStop();

if(flagSerial==1)
  {
  
  //ruta[h].sentido = 'Q';      //para que no imprima por serial en el debagueo cuando no recibio nada
  macBeaconDestino="";
  potenciaBeaconDestino=0;
  if (Serial1.available()) 
    {
    Serial.println("MODO OPERACION");
    h=0;
    while (Serial1.available() && caux != '$' && flagSerial == 1)
      {
      if (flagBasuraSerial == 0)
        {
        Serial.println("en busca del -");
        do{
          caux = Serial1.read();
          Serial.println(caux);
          }while (caux != '-');
        flagBasuraSerial=1;
       // Serial.println("detecto -   INICIO DE CADENA");
        }
      ruta[h].sentido=Serial1.read();
      Serial.print(" valor de primer sentido: ");
      Serial.println(ruta[h].sentido);
      //ruta[h].sentido=Serial1.read();
      //Serial.print(" valor de primer sentido 2: ");
      //Serial.println(ruta[h].sentido);
      intermedio= "";
      j=0;
      do{
        caux = Serial1.read();
        Serial.print(" valor de caux: ");
        Serial.println(caux);
        if (caux != '|' && caux != '$')
          {
          intermedio.concat(caux);
          }
        j++;
        }while (caux != '|' && caux != '$' && j<50);
      Serial.print(" valor de Intermedio: ");
       Serial.println(intermedio);
      ruta[h].pasos = intermedio.toInt();
      //intermedio= "";
      h++;
      }
     Serial.println("SALIO DEL  WHILE");
    ruta[h].sentido='$';

   ///////////Inicio Guardar la MAC y la potencia del beacon recibida por wifi /////////
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
      //FIN Guardar la MAC y la potencia del beacon recibida por wifi /////////////////////// /
     
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
  while ((ruta[h].sentido != '$')&&(h<30)&&(potenciaBeaconDestino!=0))  //preguntar por distinto de Q es para que solo imprima cuando recibe datos
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
  h=0;
  while ((ruta[h].sentido != '$')&&(potenciaBeaconDestino!=0))
    {
    k=0;
    if (ruta[h].sentido == 'F')
      {
      Serial.println("Ejecuta avance: ");Serial.print(ruta[h].pasos);Serial.println(" pasos");
      flagDesvioIzquierda=0;
      while (k < ruta[h].pasos)
        {
          ///// INICIO DE ESQUIVAR OBSTACULO /////////
        if(detectarObstaculo())
          {
          if(desvioObstaculo>(ruta[h].pasos-k))
          {
          Serial1.write("-5");                 //Definir mensaje al wifi -5 Plataforma detenida por obstaculo sin posibilidad de esquivarlo
          Serial.println("Plataforma DETENIDA los pasos pendientes no permiten maniobrar para esquivarlo");
            // Evaluar volver al punto anterior
           while(digitalRead(PinProxiFrontal) == LOW)    
              {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
              delay(100);
              }
          }else{
          if(flagDesvioIzquierda==0){
          GirarIzquierda(209);}else{GirarDerecha(209);}
          flagObstaculo=0;
          desvio1=0;
          while ((desvio1 <= desvioObstaculo)&&(flagObstaculo==0))
            {
              if (detectarObstaculo()== true)
              {flagObstaculo=1;}
              else{
              Avance();
              desvio1++; 
              }
            }
          if(flagObstaculo==1)  //detecto obstaculo en ruta alternativa tengo que volver e intentar otro camino
            {
            GirarDerecha(418);
            
            p=0;
              while (p <= desvio1)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  Avance();
                  p++; 
                  }
                }
            
            if(flagDesvioIzquierda==0){
            GirarIzquierda(209);}else{GirarDerecha(209);}
            flagDesvioIzquierda=1;
            }
          else{
          if(flagDesvioIzquierda==0){
          GirarDerecha(209);}else{GirarIzquierda(209);}
          
          flagObstaculo=0;
          desvio2=0;
          while ((desvio2 <= desvioObstaculo)&&(flagObstaculo==0))
            {
              if (detectarObstaculo()== true)
              {flagObstaculo=1;}
              else{
              Avance();
              desvio2++; 
              }
            }
          if(flagObstaculo==1)  //detecto obstaculo en ruta alternativa tengo que volver e intentar otro camino
            {
            GirarDerecha(418);
            
            p=0;
              while (p <= desvio2)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  Avance();
                  p++; 
                  }
                }
            if(flagDesvioIzquierda==0){
            GirarIzquierda(209);}else{GirarDerecha(209);}
            p=0;
              while (p <= desvio1)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  Avance();
                  p++; 
                  }
                }
            if(flagDesvioIzquierda==0){
            GirarIzquierda(209);}else{GirarDerecha(209);}
            flagDesvioIzquierda=1;
            } else {
          if(flagDesvioIzquierda==0){
          GirarDerecha(209);}else{GirarIzquierda(209);}
         flagObstaculo=0;
          desvio3=0;
          while ((desvio3 <= desvioObstaculo)&&(flagObstaculo==0))
            {
              if (detectarObstaculo()== true)
              {flagObstaculo=1;}
              else{
              Avance();
              desvio3++; 
              }
            }
          if(flagObstaculo==1)  //detecto obstaculo en ruta alternativa tengo que volver e intentar otro camino
            { 
            GirarDerecha(418);
            p=0;
            while (p <= desvio3)
              {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  Avance();
                  p++; 
                  }
              }
            if(flagDesvioIzquierda==0){
            GirarIzquierda(209);}else{GirarDerecha(209);}
            p=0;
              while (p <= desvio2)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  Avance();
                  p++; 
                  }
                }
            if(flagDesvioIzquierda==0){
            GirarIzquierda(209);}else{GirarDerecha(209);}
            p=0;
              while (p <= desvio1)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  Avance();
                  p++; 
                  }
                }
            flagDesvioIzquierda=1;
            }else{
            if(flagDesvioIzquierda==0){
            GirarIzquierda(209);}else{GirarDerecha(209);}
            k=k+desvioObstaculo;
            }
            
          }}}}
          else{
            //// FIN DE ESQUIVAR OBSTACULO /////////*/
          Avance();
          k++;
          }
        }
        }
      
    if (ruta[h].sentido == 'D')
      {
      Serial.println("Ejecuta Giro Derecha: ");Serial.print(ruta[h].pasos);Serial.println(" pasos");
      while (k <= ruta[h].pasos)
        {
        GirarDerecha(1);
        k++;
        }
      }
    if (ruta[h].sentido == 'I')
      {
      Serial.print("Ejecuta Giro Izquierda: ");Serial.print(ruta[h].pasos);Serial.println(" pasos");
      while (k <= ruta[h].pasos)
        {
        GirarIzquierda(1);
        k++;
        }
      }   
    h++;
    
  
  } h=0;
  // INICIO  Validar potencia y mac de beacon /////////////////////////////////////////////
  if(potenciaBeaconDestino!=0)
  {
  Serial2.print("M");
  Serial2.print(macBeaconDestino);
  Serial2.print("P");
  Serial2.print(potenciaBeaconDestino);
  Serial2.print("$");
  int contDelayValidarBeacon=0;
  while((digitalRead(validacionBeaconDestino) == LOW)&&(contDelayValidarBeacon<=60))
    {delay(500); 
    contDelayValidarBeacon++;//Espera mientras el bluethoot procesa la mac y potencia del beacon, salida forzada en 30 segundos
    }
  if (digitalRead(ResultadoBeaconDestino) == HIGH)
    {
    Serial1.print("-2");      // mensaje -2 a la placa wifi "llego ok"
    Serial.println("potencia del Beacon destino CORRECTA");
    }else{
    Serial1.print("-3");    // mensaje -3 no llego "no llego"
    Serial.println("potencia del Beacon destino FUERA DE RANGO");  
    }
  }
  // FIN  Validar potencia y mac de beacon   //////////////////////////////////////////////  */

  potenciaBeaconDestino=0;
  ruta[0].sentido='$';
  } //  FIN  Recibir y ejecutar ruta   //////////////////////////////////////////////  /


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
        Serial.println("orden de avance ");
        while((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == HIGH))
          {
          //while(digitalRead(PinProxiFrontal) == LOW)    
         //   {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
          //  }
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
        Serial.println("orden de Girar a la derecha ");
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
        Serial.println("orden de Girar a la izquierda ");
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
        Serial.print("Izquierda pasos;");
        Serial.print(ContPasos);
        Serial.print("   Ruta parcial: ");
        Serial.println(rutaEntrenamiento);
        }
 
    }
  rutaEntrenamiento.concat("$");
  //ENVIAR rutaEntrenamiento POR SERIAL 2 al Bluethoot
  Serial.print("Ruta de Entrenamiento enviada al Bluethoot: ");
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

bool detectarObstaculo()
 {
  int auxObstaculo=0;
  if (digitalRead(PinProxiFrontal) == LOW){Serial1.write("-1");Serial.println("OBSTACULO");}    //alerta de obstaculo
  while((digitalRead(PinProxiFrontal) == LOW)&&(auxObstaculo<5))    
    {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
    delay(1000);
    auxObstaculo++;
    }
  if(auxObstaculo==5)
    {
    return true;
    }else{
    return false;
    }//detectarObstaculo();
 }
