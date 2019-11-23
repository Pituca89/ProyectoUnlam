/*MAGNETOMETRO
//#include <Wire.h>
//#include <QMC5883L.h>*/


const int dirPinIZ = 34; 
const int stepPinIZ = 35 ; 
const int dirPinDER = 36; 
const int stepPinDER = 37 ; 
const int pinRele = 46 ;    //PIN PARA MANEJAR EL RELAY QUE CONTROLA LOS LED


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
rutas ruta[50];                         // almacena la ruta recibida desde el wifi para su ejecucion
rutas rutaDeshacerEntrenamiento[50];
rutas AuxRuta[50];
String intermedio;
String macBeaconDestino;
String macBeaconDestinoMasPotencia;
int potenciaBeaconDestino;
int obstaculobandera;

int h;
int hr;
int he;   //contador para el entrenamiento
int heAux;  //contador para el entrenamiento
int j;
unsigned int k;
int m;
int p;
int q;
int r; //contador para mostrar en debagueo la ruta armada en entrenamiento
int flagSerial;
int flagBasuraSerial;
//int flagPrimerInstruccionEntrenamiento;
int contObstaculo;
int flagObstaculo;
int desvio1;        //variables usadas para contar los pasos que se desvia de la ruta original para esquivar un obstaculo
int desvio2;        //la ruta para esquivar un obstaculo es un cuadrado, hay 3 tramos de desvio a contabilizar
int desvio3;
int flagDesvioIzquierda;
int flagDesvioDerecha;
int auxDerIzq;
int flagRutaDI;

/*QMC5883L compass;     //MAGNETOMETRO
int heading;
int gradosHaciaNorte; */

#define delaiPulsos 3500       // microsegundos entre pulsos, menor numero mayor velocidad de giro 
#define delaiPulsosInicio 5000     // microsegundos entre pulsos, menor numero mayor velocidad de giro 
#define delaiPulsosMagne  15000
#define desvioObstaculoEjeX 300    // pasos MAXIMOS de desvio para esquivar un obstaculo en el eje X
#define desvioObstaculoEjeY 480    // pasos MAXIMOS de desvio para esquivar un obstaculo en el eje Y
#define CantPasosLento 100     // Cantidad de pasos a realizare al inicio de un avance y giro--> CAMBIOVELOCIDAD
#define delayEntreInstrucciones 500 //milisegundos entre cada instruccion cuando se esquiva el obstaculo
#define noventagrados 214  // PARA RUEDAS NEGRAS CON 2 VUELTAS DE CINTA
#define cientoochentagrados 425  // PARA RUEDAS NEGRAS CON 2 VUELTAS DE CINTA

void setup() {
	
delay(10000);                //delay de 10 segundos 
pinMode(stepPinIZ,OUTPUT); 
pinMode(dirPinIZ,OUTPUT);
pinMode(stepPinDER,OUTPUT); 
pinMode(dirPinDER,OUTPUT);

pinMode(pinRele,OUTPUT);

pinMode(PinProxiFrontal,INPUT); 
pinMode(PinProxiIzquierdo,INPUT); 
pinMode(PinProxiDerecho,INPUT); 

pinMode(PinEntrenamiento,INPUT); 
pinMode(Pin2binario,INPUT);
pinMode(Pin1binario,INPUT); 
pinMode(Pin0binario,INPUT);

pinMode(validacionBeaconDestino,INPUT);
pinMode(ResultadoBeaconDestino,INPUT);

/*MAGNETOMETRO
Wire.begin();
compass.init();
compass.setSamplingRate(50);*/

rutaEntrenamiento="";

Serial2.begin(115200);      //Comunicacion con placa Bluethoot para enviar rutas modo entrenamiento
Serial.begin(9600);       //debagueo
Serial1.begin(115200);      //Comunicacion con placa wifi para recibir rutas modo operacion

h=0;
j=0;
k=0;
//m=0;
p=0;
he=0;
hr=0;
caux="";
flagSerial = 0;
flagBasuraSerial = 0;
//flagPrimerInstruccionEntrenamiento = 0;
flagObstaculo=0;
flagDesvioIzquierda=0;
flagDesvioDerecha=0;
contObstaculo=0;
obstaculobandera=0;
attachInterrupt(digitalPinToInterrupt(intPinSerial), interrupcionSerial, RISING); //interrupcion del wifi
macBeaconDestino="";
macBeaconDestinoMasPotencia="";
potenciaBeaconDestino=0;
desvio1=0;        
desvio2=0;        
desvio3=0;
auxDerIzq=0;
flagRutaDI=0;
}


void loop() {
  digitalWrite(pinRele,LOW);
unsigned int ContPasos = 0;
MotorStop();

/*hacerDosGiros();     //MAGNETOMETRO
delay(2000);
orientarNorte();  */


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
		  Serial.println("");
		  Serial.println("LEYENDO BASURA BUSCANDO EL GUION");
          Serial.println(caux);
          }while (caux != '-');
        flagBasuraSerial=1;
       // Serial.println("detecto -   INICIO DE CADENA");
        }
      AuxRuta[h].sentido=Serial1.read();
      Serial.print(" valor de primer sentido: ");
      Serial.println(AuxRuta[h].sentido);
      //ruta[h].sentido=Serial1.read();
      //Serial.print(" valor de primer sentido 2: ");
      //Serial.println(AuxRuta[h].sentido);
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
      AuxRuta[h].pasos = intermedio.toInt();
      //intermedio= "";
      h++;
      }
    Serial.println("SALIO DEL WHILE QUE LEE SOLO LA RUTA SIN MAC Y POTENCIA");
    AuxRuta[h].sentido='$';
       
   ///////////Inicio Guardar la MAC y la potencia del beacon recibida por wifi /////////
    j=0;  
      do{
			if(Serial1.available())
			{
			  	caux = Serial1.read();
          Serial.print(" valor de caux en mac: ");
        Serial.println(caux);
				if (caux != 'P')
					{
					macBeaconDestino.concat(caux);// += caux;
					}
				j++;
			}
        }while (caux != 'P' && j<50);
        Serial.print("MAC RECIBIDA : ");
        Serial.println(macBeaconDestino);
      //caux = Serial1.read();  // Leo el "-" entre la potencia y el valor de la misma ejemplo P-67
      j=0;
      intermedio="";
      do{
        Serial.println("dowhile potencia");
			//if(Serial1.available())
			//{
				caux = Serial1.read();
        Serial.print(" valor de caux en potencia: ");
        Serial.println(caux);
				if (caux != '#')
				  {
				  intermedio.concat(caux);
				  }
			//}
      j++;
        }while (caux != '#' && j<50);
      potenciaBeaconDestino = intermedio.toInt();
      Serial.print("POTENCIA RECIBIDA : ");
      Serial.println(intermedio);
      //FIN Guardar la MAC y la potencia del beacon recibida por wifi /////////////////////// /

      //////////Inicio depuracion de ruta, unir giros consecutivos I D ///////
    h=0;
    hr=0;
    flagRutaDI=0;
    while (AuxRuta[h].sentido!='$')
      {
      auxDerIzq=0;
      while (AuxRuta[h].sentido=='D' || AuxRuta[h].sentido=='I')
        {
        flagRutaDI=1;
        if(AuxRuta[h].sentido=='D')
          {
           auxDerIzq+=AuxRuta[h].pasos;
           }
        else{
          auxDerIzq-=AuxRuta[h].pasos;
          }
         h++;     
        }
      if(flagRutaDI)
        {
        if(auxDerIzq>0)
          {
          ruta[hr].sentido='D'; 
          ruta[hr].pasos=auxDerIzq;
          }else{
          ruta[hr].sentido='I'; 
          ruta[hr].pasos=abs(auxDerIzq);  
          }
        flagRutaDI=0;
        hr++;
        }
        
      if(AuxRuta[h].sentido=='F')
        {
         ruta[hr].sentido='F'; 
         ruta[hr].pasos=AuxRuta[h].pasos;
         hr++; 
         h++; 
        }
      }
     ruta[hr].sentido='$';
     //////////FIN depuracion de ruta, unir giros consecutivos I D ///////
     
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
  while ((ruta[h].sentido != '$')&&(h<50)&&(potenciaBeaconDestino!=0))  //preguntar por distinto de Q es para que solo imprima cuando recibe datos
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
      digitalWrite(pinRele,HIGH);
    k=0;
    if (ruta[h].sentido == 'F')
      {
      Serial.print("Ejecuta avance: ");Serial.print(ruta[h].pasos);Serial.println(" pasos");
      flagDesvioIzquierda=0;
      while (k < ruta[h].pasos)
        {
          ///// INICIO DE ESQUIVAR OBSTACULO /////////
        while ((detectarObstaculo()== true)&&(flagDesvioIzquierda==1)&&(flagDesvioDerecha==1))
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }
        if(detectarObstaculo())
          {
          if(desvioObstaculoEjeY>(ruta[h].pasos-k))
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
			  delay(delayEntreInstrucciones);
          GirarIzquierda(noventagrados);
          delay(delayEntreInstrucciones);}else{delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);}
          flagObstaculo=0;
          desvio1=0;
          while ((desvio1 <= desvioObstaculoEjeX)&&(flagObstaculo==0))
            {
              if (detectarObstaculo()== true)
              {flagObstaculo=1;}
              else{  
               // if (desvio1 > CantPasosLento && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                  if (desvio1 > CantPasosLento && obstaculobandera > CantPasosLento && (desvioObstaculoEjeX-desvio1) > CantPasosLento)       //Inicio y fin lento
				            {
				            Avance();
				            }
				           else
          				  {
          				  AvanceInicio(); // Avanza 1 paso
          				  obstaculobandera++;
          				  }
        				  desvio1++; 
        				  }
            }
          if(flagObstaculo==1)  //detecto obstaculo en ruta alternativa tengo que volver e intentar otro camino
            {delay(delayEntreInstrucciones);
            GirarDerecha(cientoochentagrados);
            delay(delayEntreInstrucciones);
            p=0;
              while (p <= desvio1)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  // if (p > CantPasosLento && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                   if (p > CantPasosLento && obstaculobandera > CantPasosLento && (desvio1-p) > CantPasosLento)       //Inicio y fin lento
                       {
                       Avance();
                       }
                    else
                      {
                      AvanceInicio(); // Avanza 1 paso
                      obstaculobandera++;
                      }
                  p++; 
                  }
                }
            
            if(flagDesvioIzquierda==0){
                delay(delayEntreInstrucciones);
    			      GirarIzquierda(noventagrados);
                delay(delayEntreInstrucciones);
                flagDesvioIzquierda=1;
                }
            else{
                delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);
                flagDesvioDerecha=1;
                }
      
            
            }
          else{
          if(flagDesvioIzquierda==0){
          delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);}else{delay(delayEntreInstrucciones);GirarIzquierda(noventagrados);delay(delayEntreInstrucciones);}
          
          flagObstaculo=0;
          desvio2=0;
          while ((desvio2 <= desvioObstaculoEjeY)&&(flagObstaculo==0))
            {
              if (detectarObstaculo()== true)
              {flagObstaculo=1;}
              else{
               //if (desvio2 > CantPasosLento && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
               if (desvio2 > CantPasosLento && obstaculobandera > CantPasosLento && (desvioObstaculoEjeY-desvio2) > CantPasosLento)       //Inicio y fin lento
            {
              Avance();
            }
            else
            {
              AvanceInicio(); // Avanza 1 paso
			  obstaculobandera++;
            }
              desvio2++; 
              }
            }
          if(flagObstaculo==1)  //detecto obstaculo en ruta alternativa tengo que volver e intentar otro camino
            {
				delay(delayEntreInstrucciones);
            GirarDerecha(cientoochentagrados);
            delay(delayEntreInstrucciones);
            
            p=0;
              while (p <= desvio2)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  //if (p > CantPasosLento  && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                  if (p > CantPasosLento && obstaculobandera > CantPasosLento && (desvio2-p) > CantPasosLento)       //Inicio y fin lento
                  {
                    Avance();
                  }
                else
                   {
                    AvanceInicio(); // Avanza 1 paso
					          obstaculobandera++;
                  }
                  p++; 
                  }
                }
            if(flagDesvioIzquierda==0){
            delay(delayEntreInstrucciones);GirarIzquierda(noventagrados);delay(delayEntreInstrucciones);}else{delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);}
            p=0;
              while (p <= desvio1)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                //  if (p > CantPasosLento  && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                  if (p > CantPasosLento && obstaculobandera > CantPasosLento && (desvio1-p) > CantPasosLento)       //Inicio y fin lento
                  {
                    Avance();
                  }
                  else
                  {
                    AvanceInicio(); // Avanza 1 paso
					          obstaculobandera++;
                  }
                  p++; 
                  }
                }
            if(flagDesvioIzquierda==0)
              {
              delay(delayEntreInstrucciones);GirarIzquierda(noventagrados);delay(delayEntreInstrucciones);
              flagDesvioIzquierda=1;
              }
            else{
                delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);
                flagDesvioDerecha=1;
                }
            
            } else {
          if(flagDesvioIzquierda==0){
          delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);}else{delay(delayEntreInstrucciones);GirarIzquierda(noventagrados);delay(delayEntreInstrucciones);}
         flagObstaculo=0;
          desvio3=0;
          while ((desvio3 <= desvioObstaculoEjeX)&&(flagObstaculo==0))
            {
              if (detectarObstaculo()== true)
              {flagObstaculo=1;}
              else{
               // if (desvio3 > CantPasosLento && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                if (desvio3 > CantPasosLento && obstaculobandera > CantPasosLento && (desvioObstaculoEjeX-desvio3) > CantPasosLento)       //Inicio y fin lento
              {
                Avance();
              }
              else
              {
              AvanceInicio(); // Avanza 1 paso
				      obstaculobandera++;
              }
              desvio3++; 
              }
            }
          if(flagObstaculo==1)  //detecto obstaculo en ruta alternativa tengo que volver e intentar otro camino
            { 
            delay(delayEntreInstrucciones);
			      GirarDerecha(cientoochentagrados);
            delay(delayEntreInstrucciones);
            p=0;
            while (p <= desvio3)
              {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  //if (p > CantPasosLento && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                  if (p > CantPasosLento && obstaculobandera > CantPasosLento && (desvio3-p) > CantPasosLento)       //Inicio y fin lento
                  {
                    Avance();
                  }
                  else
                  {
                    AvanceInicio(); // Avanza 1 paso
					          obstaculobandera++;
                  }          
                  p++; 
                  }
              }
            if(flagDesvioIzquierda==0){
            delay(delayEntreInstrucciones);GirarIzquierda(noventagrados);delay(delayEntreInstrucciones);}
			else{delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);}
            p=0;
              while (p <= desvio2)
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                  //if (p > CantPasosLento && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                  if (p > CantPasosLento && obstaculobandera > CantPasosLento && (desvio2-p) > CantPasosLento)       //Inicio y fin lento
                  {
                    Avance();
                  }
                  else
                  {
                  AvanceInicio(); // Avanza 1 paso
					        obstaculobandera++;
                  }       
                  p++; 
                  }
                }
            if(flagDesvioIzquierda==0)
              {
              delay(delayEntreInstrucciones);GirarIzquierda(noventagrados);delay(delayEntreInstrucciones);
              }
			        else
			        {
			        delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);
			        }
            p=0;
              while (p <= desvio1 )
                {
                 if (detectarObstaculo()== true)
                  {Serial1.write("-4");                 //Definir mensaje al wifi -4 Plataforma detenida por obstaculo
                   Serial.println("Plataforma DETENIDA por OBSTACULO");
                  }        
                  else{
                 // if (p > CantPasosLento && obstaculobandera > CantPasosLento) // CAMBIOVELOCIDAD
                  if (p > CantPasosLento && obstaculobandera > CantPasosLento && (desvio1-p) > CantPasosLento)       //Inicio y fin lento
                  {
                    Avance();
                  }
                  else
                  {
                    AvanceInicio(); // Avanza 1 paso
					          obstaculobandera++;
                  }
                  p++; 
                  }
                }
            
           // }else{        //brujula
           
            
            if(flagDesvioIzquierda==0)
              {
              delay(delayEntreInstrucciones);GirarIzquierda(noventagrados);delay(delayEntreInstrucciones);
              flagDesvioIzquierda=1;
              }
			      else{
			          delay(delayEntreInstrucciones);GirarDerecha(noventagrados);delay(delayEntreInstrucciones);
                flagDesvioDerecha=1;
			          }
            k=k+desvioObstaculoEjeY;
            }
            
          }}}}
          else{
            //// FIN DE ESQUIVAR OBSTACULO /////////*/                Cambio, inicio y final lento en cada avance
          if ((k > CantPasosLento)&&((ruta[h].pasos-k) >  CantPasosLento) )// CAMBIOVELOCIDAD
         // if (k > CantPasosLento)// CAMBIOVELOCIDAD
             {
              Avance();
             }
            else
             {
             AvanceInicio(); // Avanza 1 paso
             }
          k++;
          flagDesvioIzquierda=0;
          flagDesvioDerecha=0;
          }
        }
        }
      
    if (ruta[h].sentido == 'D')
      {
      Serial.print("Ejecuta Giro Derecha: ");Serial.print(ruta[h].pasos);Serial.println(" pasos");
      //while (k <= ruta[h].pasos)
      //  {
      //  GirarDerecha(1);
      //  k++;
      //  }
      GirarDerecha(ruta[h].pasos);
      }
    if (ruta[h].sentido == 'I')
      {
      Serial.print("Ejecuta Giro Izquierda: ");Serial.print(ruta[h].pasos);Serial.println(" pasos");
      //while (k <= ruta[h].pasos)
      //  {
      //  GirarIzquierda(1);
      //  k++;
      //  }
      GirarIzquierda(ruta[h].pasos);
      }   
    h++;
    
    
  delay(1000);
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
digitalWrite(pinRele, LOW);

if (digitalRead(PinEntrenamiento) == HIGH)      //Modo entrenamiento
  {
  /*  codigo binario de entrenamiento Pin2binario Pin1binario Pin0binario
             *  0 0 0 : plataforma detenida (S)
             *  0 0 1 : avanzar (F)
             *  0 1 0 : girar derecha (D)
             *  1 0 0 : girar izquierda (I)
             *  0 1 1 : deshacer ultimo movimiento
             */
  rutaEntrenamiento="";
  he=0;
  rutaDeshacerEntrenamiento[he].sentido='F';  //guardo en la primer posicion de entrenamiento un F0 para que no influya en a ruta
  rutaDeshacerEntrenamiento[he].pasos=0;
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
          while(digitalRead(PinProxiFrontal) == LOW)    
            {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
            }
		    AvanceInicio();
        ContPasos++;
          }
          
        /*if(flagPrimerInstruccionEntrenamiento == 0)
          {
          rutaEntrenamiento.concat("F");
          flagPrimerInstruccionEntrenamiento=1;
          }
          else{
          rutaEntrenamiento.concat("|");
          rutaEntrenamiento.concat("F");
          }
        rutaEntrenamiento.concat(String(ContPasos));*/
          
        Serial.print("Avance pasos;");
        Serial.println(ContPasos);
        //Serial.print("   Ruta parcial: ");
        //Serial.println(rutaEntrenamiento);
        he++;
        rutaDeshacerEntrenamiento[he].sentido='F';
        rutaDeshacerEntrenamiento[he].pasos=ContPasos;

        /* Mostrar ruta parcial
        Serial.println("Ruta parcial: ");  
        r=0;
        do
          {
          Serial.print(rutaDeshacerEntrenamiento[he].sentido);
          Serial.println(rutaDeshacerEntrenamiento[he].pasos);
          //Serial.print("-");  
          r++;
          }while(r<=he);
        Serial.print("    valor de he: ");  
        Serial.println(he);  
        // Fin mostrar ruta parcial  */
        
        
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
        /*if(flagPrimerInstruccionEntrenamiento == 0)
          {
          rutaEntrenamiento.concat("D");
          flagPrimerInstruccionEntrenamiento=1;
          }
          else{
          rutaEntrenamiento.concat("|");
          rutaEntrenamiento.concat("D");
          }
        rutaEntrenamiento.concat(String(ContPasos));*/
        Serial.print("derecha pasos;");
        Serial.println(ContPasos);
       // Serial.print("   Ruta parcial: ");
       // Serial.println(rutaEntrenamiento);
        he++;
        rutaDeshacerEntrenamiento[he].sentido='D';
        rutaDeshacerEntrenamiento[he].pasos=ContPasos;
  
        /* Mostrar ruta parcial
        Serial.println("Ruta parcial: ");  
        r=0;
        do
          {
          Serial.print(rutaDeshacerEntrenamiento[he].sentido);
          Serial.println(rutaDeshacerEntrenamiento[he].pasos);
          //Serial.print("-");
          r++;  
          }while(r<=he);
        Serial.print("    valor de he: ");  
        Serial.println(he);  
        // Fin mostrar ruta parcial  */
        
        
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
        /*if(flagPrimerInstruccionEntrenamiento == 0)
          {
          rutaEntrenamiento.concat("I");
          flagPrimerInstruccionEntrenamiento=1;
          }
          else{
          rutaEntrenamiento.concat("|");
          rutaEntrenamiento.concat("I");
          }
        rutaEntrenamiento.concat(String(ContPasos));*/
        Serial.print("Izquierda pasos;");
        Serial.println(ContPasos);
        //Serial.print("   Ruta parcial: ");
       // Serial.println(rutaEntrenamiento);
        he++;
        rutaDeshacerEntrenamiento[he].sentido='I';
        rutaDeshacerEntrenamiento[he].pasos=ContPasos;

        /* Mostrar ruta parcial
        Serial.println("Ruta parcial: ");  
        r=0;
        do
          {
          Serial.print(rutaDeshacerEntrenamiento[r].sentido);
          Serial.println(rutaDeshacerEntrenamiento[r].pasos);
          //Serial.print("-");  
          r++;
          }while(r<=he);
        Serial.print("    valor de he: ");  
        Serial.println(he);  
        // Fin mostrar ruta parcial  */
        
        
        }
    if ((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == HIGH)&&(digitalRead(Pin0binario) == HIGH)&&(he>0)) //Deshacer
        {
         Serial.println("Entro al if Deshacer"); 
         q--;
         if(rutaDeshacerEntrenamiento[he].sentido=='F')
          {
          Retroceso(rutaDeshacerEntrenamiento[he].pasos);//Serial.print("F: ");Serial.println(rutaDeshacerEntrenamiento[he].pasos);  
          }
         if(rutaDeshacerEntrenamiento[he].sentido=='D')
          {
          GirarIzquierda(rutaDeshacerEntrenamiento[he].pasos);//Serial.print("D: ");Serial.println(rutaDeshacerEntrenamiento[he].pasos); 
          }
         if(rutaDeshacerEntrenamiento[he].sentido=='I')
          {
          GirarDerecha(rutaDeshacerEntrenamiento[he].pasos);//Serial.print("I: ");Serial.println(rutaDeshacerEntrenamiento[he].pasos); 
          }
          
         /* Mostrar ruta parcial
        Serial.println("Ruta parcial: ");  
        r=0;
        while(r<=he)
          {
          Serial.print(rutaDeshacerEntrenamiento[r].sentido);
          Serial.println(rutaDeshacerEntrenamiento[r].pasos);
         // Serial.print("-");  
          r++;
          }
        Serial.print("    valor de he: ");  
        Serial.println(he);  
        // Fin mostrar ruta parcial  */
        // Serial.println("Salio del if Deshacer");
        he--; 
        }
    if ((digitalRead(Pin2binario) == HIGH)&&(digitalRead(Pin1binario) == HIGH)&&(digitalRead(Pin0binario) == HIGH)&&(he>0)) //Deshacer TODO
        {
        while(he>0)
          {//Serial.print("Dentro del while, valor de he: ");Serial.println(he);
            if(rutaDeshacerEntrenamiento[he].sentido=='F')
              {
              Retroceso(rutaDeshacerEntrenamiento[he].pasos);//Serial.print("Ejecuta Retroceso - pasos: "); Serial.println(rutaDeshacerEntrenamiento[he].pasos);
              }
            if(rutaDeshacerEntrenamiento[he].sentido=='D')
              {
              GirarIzquierda(rutaDeshacerEntrenamiento[he].pasos);//Serial.print("Ejecuta Izquierda - pasos: "); Serial.println(rutaDeshacerEntrenamiento[he].pasos);
              }
            if(rutaDeshacerEntrenamiento[he].sentido=='I')
              {
              GirarDerecha(rutaDeshacerEntrenamiento[he].pasos);//Serial.print("Ejecuta Derecha - pasos: "); Serial.println(rutaDeshacerEntrenamiento[he].pasos);
              }
             he--;
          delay(delayEntreInstrucciones);
          }
        /* Mostrar ruta parcial
        Serial.println("Ruta parcial: ");  
        r=0;
        while(r<=he)
          {
          Serial.print(rutaDeshacerEntrenamiento[r].sentido);
          Serial.println(rutaDeshacerEntrenamiento[r].pasos);
          //Serial.print("-");  
          r++;
          }
        Serial.print("    valor de he: ");  
        Serial.println(he);  
        // Fin mostrar ruta parcial  */
        Serial.println("Salio del Deshacer TODO");  
        }
    }
//<<<<<<< HEAD
    //orientarNorte();                  //MAGNETOMETRO
    //rutaEntrenamiento.concat("N");             
    //rutaEntrenamiento.concat(gradosHaciaNorte);
  
  //=======
  heAux=1;        //porque el he=0 no se utiliza
  while(heAux<=he)
    {
     if(heAux > 1) //Primer concatenacion de la ruta no lleva PIPE |
          {
          rutaEntrenamiento.concat("|");
          }//else{}
     rutaEntrenamiento.concat(rutaDeshacerEntrenamiento[heAux].sentido);
     rutaEntrenamiento.concat(String(rutaDeshacerEntrenamiento[heAux].pasos));
     heAux++;
     }
//>>>>>>> 08bfaba8d3342c0e9bf0314f87e0edbd13a56da3
  rutaEntrenamiento.concat("$");
  //ENVIAR rutaEntrenamiento POR SERIAL 2 al Bluethoot
  Serial.print("Ruta de Entrenamiento enviada al Bluethoot: ");
  Serial.println(rutaEntrenamiento);
  Serial2.print(rutaEntrenamiento); 
  //flagPrimerInstruccionEntrenamiento=0; 
  
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

void AvanceInicio()
{
digitalWrite(dirPinIZ,HIGH); 
digitalWrite(dirPinDER,HIGH); 

digitalWrite(stepPinIZ,HIGH); 
digitalWrite(stepPinDER,HIGH); 
delayMicroseconds(delaiPulsosInicio); 
digitalWrite(stepPinIZ,LOW);
digitalWrite(stepPinDER,LOW); 
delayMicroseconds(delaiPulsosInicio); 

}

void Retroceso(int retroceso)

{
int PasosRetroceso=0;
digitalWrite(dirPinIZ,LOW); 
digitalWrite(dirPinDER,LOW); 
do{
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH); 
  delayMicroseconds(delaiPulsosInicio); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  delayMicroseconds(delaiPulsosInicio); 
  PasosRetroceso++;
  }while(PasosRetroceso<retroceso);
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
  //if (x > CantPasosLento)   
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosInicio); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  //if (x > CantPasosLento)  
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosInicio);
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
  
  //if (x > CantPasosLento)  
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosInicio);
  
  
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  
  
  //if (x > CantPasosLento)  
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosInicio);
  }while(x < giro);
}

/*
void GirarDerechaMagne(int giro)
{
int x = 0;
digitalWrite(dirPinIZ,HIGH); 
digitalWrite(dirPinDER,LOW); 
do{x++;
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH); 
  //if (x > CantPasosLento)   
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosMagne); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  //if (x > CantPasosLento)  
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosMagne);
  }while(x < giro);
}

void GirarIzquierdaMagne(int giro)
{
int x = 0;
digitalWrite(dirPinIZ,LOW); 
digitalWrite(dirPinDER,HIGH); 
do{x++;
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH);
  
  //if (x > CantPasosLento)  
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosMagne);
  
  
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  
  
  //if (x > CantPasosLento)  
  //  delayMicroseconds(delaiPulsos); 
  //else
    delayMicroseconds(delaiPulsosMagne);
  }while(x < giro);
}*/


bool detectarObstaculo()
 {
	int auxObstaculo=0;
	if (digitalRead(PinProxiFrontal) == LOW)
	{
	  Serial1.write("-1");
	  Serial.println("OBSTACULO");
	}    //alerta de obstaculo
	while((digitalRead(PinProxiFrontal) == LOW)&&(auxObstaculo<5))    
    {                              // se queda frenado en un bucle hasta 5 segundos mientras haya un obstaculo adelante  
		delay(1000);
		auxObstaculo++;
		obstaculobandera=0;
    }
	if(auxObstaculo==5)
    {
		return true;
    }
	else
	{
		return false;
    }//detectarObstaculo();
	
 }

/*
void orientarNorte(){
    gradosHaciaNorte=0;
    heading = compass.readHeading();
    gradosHaciaNorte=360-heading;
    while(heading!=0){
      if(heading>180){
        GirarDerechaMagne(((360-heading)*cientoochentagrados)/180);
      }else{
        GirarIzquierdaMagne(((180-heading)*cientoochentagrados)/180);
      }
      heading = compass.readHeading();
    }
       
 }
void hacerDosGiros(){
  GirarDerecha(cientoochentagrados*4);
}*/
