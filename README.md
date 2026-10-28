# Pesca no Ritmo

Jogo sério de reabilitação (libGDX + Arduino). O paciente pedala em ritmo constante para recolher a linha e capturar o peixe.
Só a cadência da pedalada controla o jogo.

## Executar

Requisitos: JDK 11+ e Gradle.

```
gradle :desktop:run        # roda o jogo
gradle :desktop:dist       # gera desktop/build/libs/pesca-no-ritmo.jar (executável)
```

## Sem Arduino (simulador)

Na tela do menu a entrada padrão é o teclado: cada toque na **barra de espaço** = 1 volta da pedivela.
Para simular 50 RPM, toque a barra a cada ~1,2 s.

## Com Arduino UNO

1. Monte o circuito descrito no topo de `arduino/pedalada/pedalada.ino` (botão ou reed switch no pino 2 e GND; LEDs 8, 9, 10).
2. Grave o sketch no Arduino.
3. Feche o Monitor Serial da IDE (ele ocupa a porta).
4. No menu do jogo, tecle **I** até aparecer `Arduino (COMx)` ou `/dev/ttyUSB0`.

Protocolo: o Arduino envia `P,<ms>` a cada volta; o jogo devolve `V`/`A`/`R` (verde/amarelo/vermelho) para os LEDs.

## Como funciona

| Item | Descrição |
|---|---|
| Calibração | 20 s pedalando confortável; a média (após 5 s) vira a cadência-alvo (30 a 80 RPM) |
| Aquecimento | 10 s de pedalada suave até a boia |
| Luta | Cadência na zona recolhe a linha; acima da zona a tensão sobe e a linha rompe (3 rompimentos = fim); abaixo, o peixe se afasta |
| Fase 1 · Lago calmo | Zona ±25%, peixe pequeno |
| Fase 2 · Rio | Zona ±18%, oscila suavemente |
| Fase 3 · Mar aberto | Zona ±12%, alterna 9 s "puxando" (mais rápido) e 7 s "descansando" (mais lento): treino intervalado |
| Segurança | Zona nunca passa de 1,35 × alvo; pausa com **P** |
| Pontuação | 600 × tempo na zona + 300 × (1 − CV da cadência) + 100 × rapidez; metade se o peixe escapar |
| PSE | Após cada fase, escala 0 a 10 de esforço percebido |
| Registro | `sessoes_pesca.csv` (uma linha por tentativa) para mostrar a evolução |

Teclas no jogo: **P** pausar, **R** reiniciar fase, **M** menu.

## Estrutura

```
core/src/main/java/br/pesca/
  PescaGame        recursos compartilhados (ShapeRenderer, SpriteBatch, fonte, câmera)
  LutaPesca        regras e métricas (sem libGDX, testável)
  Fase             parâmetros de dificuldade das 3 fases
  CadenceTracker   pulsos -> RPM suavizado
  PulseSource      KeyboardPulseSource | SerialPulseSource (jSerialComm)
  MenuScreen · CalibrationScreen · GameScreen · ResultScreen · SummaryScreen
  SessionLogger    CSV
desktop/             launcher LWJGL3
arduino/pedalada/    sketch do UNO
```

Todo o visual usa `ShapeRenderer` (sem texturas). Para ajustar a dificuldade, edite apenas `Fase.java`.