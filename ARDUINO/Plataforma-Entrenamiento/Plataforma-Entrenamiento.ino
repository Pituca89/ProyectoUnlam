const int dirPinIZ = 30; 
const int stepPinIZ =31 ; 
const int dirPinDER = 32; 
const int stepPinDER =33 ; 

const int PinEntrenamiento = 22;
const int Pin2binario = 28;
const int Pin1binario = 26;
const int Pin0binario = 24;

const int PinProxiFrontal = 52;     // Pin para el sensor de proximidad frontal


int i=0;

#define delai 500       // Delay entre los avances y giros
#define pasos 500     // Cantidad de pasos por avance, una vuelta completa son 200 pasos
#define giro 205       // Cantidad de pasos por giro, 
#define delaiPulsos 2500       // microsegundos entre pulsos, menor numero mayor velocidad de giro 


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
Serial1.begin(115200);
}


void loop() {
unsigned long ContPasos = 0;
MotorStop();
if (digitalRead(PinEntrenamiento) == HIGH)
  {
  while (digitalRead(PinEntrenamiento) == HIGH)
    {
      if ((digitalRead(Pin2binario) == LOW)&&(digitalRead(Pin1binario) == LOW)&&(digitalRead(Pin0binario) == HIGH))
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
        }
    }
  
  }
delay (1000);
if (i == 0)

  {
  i++;
  for(int x = 0; x < pasos; x++) 
    {
    if (digitalRead(PinProxiFrontal) == LOW)
      {
      Serial1.print("-1");
      while(digitalRead(PinProxiFrontal) == LOW)
        {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
        }
      }
    Avance();                        // Avanza 1 paso
    
    }
      
  delay(delai);
  GirarDerecha();                 // Giro
  
  delay(delai);
  for(int x = 0; x < pasos; x++) 
    {
    if (digitalRead(PinProxiFrontal) == LOW)
      {
      Serial1.print("-1");
      while(digitalRead(PinProxiFrontal) == LOW)
        {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
        }
      }
      Avance();                        // Avanza 1 paso
     
    }
      
  delay(delai);
  GirarDerecha();                 // Giro
  
  delay(delai);
  for(int x = 0; x < pasos; x++) 
    {
    if (digitalRead(PinProxiFrontal) == LOW)
      {
      Serial1.print("-1");
      while(digitalRead(PinProxiFrontal) == LOW)
        {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
        }
      }
    Avance();                        // Avanza 1 paso
    
    }
    
  delay(delai);
   GirarDerecha();                 // Giro
   
  delay(delai);
  for(int x = 0; x < pasos; x++) 
    {
    if (digitalRead(PinProxiFrontal) == LOW)
      {
      Serial1.print("-1");
      while(digitalRead(PinProxiFrontal) == LOW)
        {                              // se queda frenado en un bucle mientras haya un obstaculo adelante  
        }
      }
      Avance();                        // Avanza 1 paso
     
    }
    
  delay(delai);
  GirarDerecha();                 // Giro
  }
else{}
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
for(int x = 0; x < giro; x++) 
  {
  digitalWrite(stepPinIZ,HIGH); 
  digitalWrite(stepPinDER,HIGH); 
  delayMicroseconds(delaiPulsos); 
  digitalWrite(stepPinIZ,LOW);
  digitalWrite(stepPinDER,LOW); 
  delayMicroseconds(delaiPulsos); 
  }
}
