# 🎣 Pesca no Ritmo

Jogo sério de reabilitação física feito com **libGDX** e controlado por um **Arduino UNO** simulando uma bicicleta ergométrica.
O paciente pedala em ritmo constante para recolher a linha e capturar o peixe. **Só a cadência da pedalada controla o jogo.**

> Projeto Final: Jogo Sério para Reabilitação com IoT e libGDX.


## Como o jogo funciona

1. **Calibração (20 s):** o paciente pedala confortavelmente; a média, após 5 s de adaptação, vira a cadência-alvo (30 a 80 RPM).
2. **Aquecimento (10 s):** pedalada suave leva o barco até a boia.
3. **Luta com o peixe:** a cadência controla a tensão da linha.

| Cadência | Efeito |
|---|---|
| Dentro da zona-alvo | O peixe se aproxima e a tensão fica ideal |
| Acima da zona | A tensão sobe; ao chegar ao limite a linha rompe (3 rompimentos encerram a fase) |
| Abaixo da zona | O peixe se afasta e a linha afrouxa |

4. **Resultado:** pontuação, métricas e pergunta de percepção de esforço (PSE, 0 a 10).

### Fases

| Fase | Cenário | Zona-alvo | Dinâmica |
|---|---|---|---|
| 1 | Lago calmo | ±25% | Zona fixa, peixe pequeno |
| 2 | Rio | ±18% | A zona oscila suavemente |
| 3 | Mar aberto | ±12% | Treino intervalado: 9 s "puxando" (alvo × 1,12) e 7 s "descansando" (alvo × 0,85) |

Segurança: a zona nunca passa de 1,35 × a cadência-alvo e o jogo pode ser pausado a qualquer momento.

Todo o visual usa formas geométricas desenhadas com `ShapeRenderer` (sem texturas); a paleta azul e laranja evita depender de vermelho e verde.

## Executar o jogo

Requisitos: **JDK 17** (o Gradle Wrapper baixa o JDK 17 sozinho para rodar o build; não é preciso instalar o Gradle).

```bash
./gradlew lwjgl3:run          # roda o jogo (no Windows: gradlew.bat lwjgl3:run)
./gradlew lwjgl3:jar          # gera lwjgl3/build/libs/PescaNoRitmo-1.0.0.jar
java -jar lwjgl3/build/libs/PescaNoRitmo-1.0.0.jar
```

O `StartupHelper` reinicia a JVM no macOS (`-XstartOnFirstThread`) e evita falhas no Windows com nomes de usuário que têm acentos.

Executável com JDK embutido (Windows, Linux, macOS): o plugin Construo cria as tarefas `lwjgl3:package...`; use `./gradlew lwjgl3:tasks --all` para listá-las.

**IntelliJ:** abra a pasta raiz como projeto Gradle e rode a classe `Lwjgl3Launcher` ou a tarefa `lwjgl3 > Tasks > application > run`.

### Sem Arduino (simulador)

A entrada padrão é o teclado: cada toque na **barra de espaço** equivale a uma volta da pedivela. Para simular 50 RPM, toque a barra a cada ~1,2 s.

## Hardware (Arduino)

Material: Arduino UNO R3, protoboard, botão ou *reed switch* com ímã na pedivela, 3 LEDs (verde, amarelo, vermelho), 3 resistores de 220 Ω e cabo USB.

```
Sensor (reed switch ou botão)   Pino 2  ── sensor ── GND   (INPUT_PULLUP, sem resistor)
LED verde      Pino 8  ── 220 Ω ── LED ── GND
LED amarelo    Pino 9  ── 220 Ω ── LED ── GND
LED vermelho   Pino 10 ── 220 Ω ── LED ── GND
```

1. Abra `arduino/pedalada/pedalada.ino` na Arduino IDE e grave no UNO.
2. Feche o Monitor Serial da IDE (ele ocupa a porta).
3. No menu do jogo, tecle **I** até aparecer `Arduino (COMx)` ou `/dev/ttyUSB0`.

Protocolo serial (115200 baud):

| Sentido | Mensagem | Significado |
|---|---|---|
| Arduino → jogo | `P,<intervalo_ms>` | Uma volta da pedivela |
| Jogo → Arduino | `V` / `A` / `R` / `X` | LED verde (ritmo ok), amarelo (devagar), vermelho (rápido), apagar |

## Controles

| Tecla | Ação |
|---|---|
| Barra de espaço | Pedalada simulada (apenas no modo teclado) |
| ENTER | Iniciar / confirmar |
| C | Calibrar cadência (menu) |
| I | Trocar entrada: teclado ou porta serial (menu) |
| P ou ESC | Pausar / continuar (jogo) |
| R | Reiniciar a fase |
| M | Voltar ao menu |
| Setas | Escolher a PSE na tela de resultado |

## Métricas e pontuação

Pontuação da fase (0 a 1000):

```
score = 600 × z + 300 × (1 − CV) + 100 × (1 − t / t_max)      (× 0,5 se o peixe não for capturado)
```

- **z:** fração do tempo da luta com a cadência dentro da zona-alvo;
- **CV:** desvio padrão ÷ média da cadência na luta (regularidade);
- **t / t_max:** duração da luta ÷ tempo máximo da fase (90, 100 e 120 s).

Cada tentativa gera uma linha em `~/.pesca-no-ritmo/sessoes_pesca.csv` (pasta do usuário):

```
data_hora,fase,capturou,score,pct_zona,cv,tempo_s,rompidas,alvo_rpm,pse
```

Sinais de evolução: tempo na zona crescente, CV decrescente, menos rompimentos e capturas em fases mais difíceis com PSE igual ou menor.

## Estrutura do projeto

```
core/src/main/java/br/mackenzie/
  Main               ponto de entrada (Game); recursos compartilhados (ShapeRenderer, SpriteBatch, fonte, câmera)
  LutaPesca          regras e métricas (sem dependência de libGDX)
  Fase               parâmetros de dificuldade das 3 fases
  CadenceTracker     pulsos → RPM suavizado
  PulseSource        interface de entrada
  KeyboardPulseSource / SerialPulseSource (jSerialComm)
  MenuScreen · CalibrationScreen · GameScreen · ResultScreen · SummaryScreen
  SessionLogger      grava o CSV
lwjgl3/              launcher LWJGL3 (Lwjgl3Launcher, StartupHelper) e empacotamento (Construo)
arduino/pedalada/    sketch do UNO
```

## Ajustar a dificuldade

Zonas, velocidades, tempo máximo e tamanho do peixe ficam em `Fase.java`; os pesos da pontuação, em `LutaPesca.resultado()`. Os valores atuais são um ponto de partida e precisam ser ajustados com testes de jogo.

## Limitações

- O dispositivo mede apenas **cadência**: não há medição de carga.
- Parâmetros e pontuação não foram validados com pacientes ou fisioterapeutas.
