/*
 * Pesca no Ritmo - sensor de pedalada (Arduino UNO R3)
 *
 * Ligacao:
 *   Botao ou reed switch: um terminal no pino 2, o outro no GND (usa INPUT_PULLUP, sem resistor)
 *   LED verde    -> pino 8  (resistor 220 ohm em serie)
 *   LED amarelo  -> pino 9  (resistor 220 ohm em serie)
 *   LED vermelho -> pino 10 (resistor 220 ohm em serie)
 *
 * Arduino -> jogo : "P,<intervalo_ms>\n" a cada volta da pedivela
 * Jogo -> Arduino : 'V' = ritmo ok (verde), 'A' = devagar (amarelo), 'R' = rapido (vermelho)
 */
const int PIN_SENSOR = 2;
const int LED_VERDE = 8, LED_AMARELO = 9, LED_VERMELHO = 10;

volatile unsigned long ultimoPulso = 0;
volatile unsigned long intervalo = 0;
volatile bool novoPulso = false;

void isrPulso() {
  unsigned long agora = millis();
  if (agora - ultimoPulso > 150) {            // debounce
    intervalo = agora - ultimoPulso;
    ultimoPulso = agora;
    novoPulso = true;
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(PIN_SENSOR, INPUT_PULLUP);
  pinMode(LED_VERDE, OUTPUT);
  pinMode(LED_AMARELO, OUTPUT);
  pinMode(LED_VERMELHO, OUTPUT);
  attachInterrupt(digitalPinToInterrupt(PIN_SENSOR), isrPulso, FALLING);
}

void loop() {
  if (novoPulso) {
    noInterrupts();
    unsigned long dt = intervalo;
    novoPulso = false;
    interrupts();
    Serial.print("P,");
    Serial.println(dt);
  }

  while (Serial.available() > 0) {
    char c = Serial.read();
    digitalWrite(LED_VERDE,    c == 'V');
    digitalWrite(LED_AMARELO,  c == 'A');
    digitalWrite(LED_VERMELHO, c == 'R');
  }
}
