const int dirPinIZ = 30; 
const int stepPinIZ =31 ; 
const int dirPinDER = 32; 
const int stepPinDER =33 ; 

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
Serial1.begin(115200);
}


void loop() {
MotorStop();
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
