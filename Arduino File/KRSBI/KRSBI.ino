/*
 * Catatan
 * L_EN, R_EN HIGH, Motor hidup
 * L_EN, R_EN LOW, Motor mati
 * RPWM != 0, LPWM = 0, Searah Jarum Jam
 * RPWM = 0, LPWM != 0, Berlawanan Jarum Jam
 */

String x,y,a,string;

//Motor Kiri
int RPWM1=3;
int LPWM1=5;

//Motor Belakang
int RPWM2=6;
int LPWM2=9;

//Motor Kanan
int RPWM3=10;
int LPWM3=11;

void setup() {
  Serial.begin(9600);
  //Setup Motor Kiri
  pinMode(RPWM1, OUTPUT);
  pinMode(LPWM1, OUTPUT);

  //Setup Motor Belakang
  pinMode(RPWM2, OUTPUT);
  pinMode(LPWM2, OUTPUT);

  //Setup Motor Kanan
  pinMode(RPWM3, OUTPUT);
  pinMode(LPWM3, OUTPUT);
}

void loop() {
  //testMotor();
  getData();  
}

void testMotor(){
  motorKiri(0, 255);        //Kiri Maju
  delay(2000);
  motorKiri(255, 0);        //Kiri Mundur
  delay(2000);
  motorKiri(0, 0);          //Kiri Berhenti
  delay(2000);
  motorKanan(0, 200);        //Kanan Mundur
  delay(2000);
  motorKanan(200, 0);        //Kanan Maju
  delay(2000);
  motorKanan(0, 0);          //Kanan Berhenti
  delay(2000);
  motorBelakang(0, 100);     //Ke Kiri
  delay(2000);
  motorBelakang(100, 0);     //ke Kanan
  delay(2000);
  motorBelakang(0, 0);       //Berhenti
  delay(2000);
}

//Mundur lpwm = 0, Maju rpwm = 0; Berhenti lpwm = 0, rpwm = 0;
void motorKiri(int rpwm, int lpwm){
  analogWrite(RPWM1, rpwm);
  analogWrite(LPWM1, lpwm);
}

//Ke Kanan lpwm = 0, Ke Kiri rpwm = 0; Berhenti lpwm = 0, rpwm = 0;
void motorBelakang(int rpwm, int lpwm){
  analogWrite(RPWM2, rpwm);
  analogWrite(LPWM2, lpwm);
}

//Maju lpwm = 0, Mundur rpwm = 0; Berhenti lpwm = 0, rpwm = 0;
void motorKanan(int rpwm, int lpwm){
  analogWrite(RPWM3, rpwm);
  analogWrite(LPWM3, lpwm);
}

void getData() {
  if(Serial.available()){
    string = "";
    while(Serial.available()){
      char comeIn = ((byte)Serial.read());
      if(comeIn == ':') {
        break;
      }else{
        string += comeIn;
      }
      delay(5);
    }
    if(string.startsWith("x")){
      x = string.substring(1);
    }
    else if(string.startsWith("y")){
      y = string.substring(1);
    }
    else if(string.startsWith("a")){
      a = string.substring(1);
    }
    Serial.print(x);
    Serial.print(" : ");
    Serial.print(y);
    Serial.print(" : ");
    Serial.println(a);
  }
}
