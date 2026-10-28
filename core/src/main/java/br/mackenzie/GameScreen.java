package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import br.mackenzie.LutaPesca.Estado;

/**
 * Tela de jogo. As regras vivem em {@link LutaPesca}; aqui ficam so entrada, pausa e desenho
 * (tudo com ShapeRenderer: sem texturas).
 */
public class GameScreen extends ScreenAdapter {

    private static final float AGUA_Y = 360f;
    private static final float TEMPO_FIM = 1.8f;

    private final Main game;
    private final int idxFase;
    private final Fase fase;
    private final LutaPesca luta;

    private boolean pausado;
    private float relogio;       // tempo de animacao
    private float scroll;        // o cenario anda proporcionalmente a cadencia
    private float tempoFim;
    private char ultimoSinal = 0;

    public GameScreen(Main game, int idxFase) {
        this.game = game;
        this.idxFase = idxFase;
        this.fase = Fase.TODAS[idxFase];
        this.luta = new LutaPesca(fase, game.alvoRpm);
        game.tracker.reset();
    }

    // ------------------------------------------------------------ logica

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);
        game.input.update();
        game.tracker.update(delta);
        tratarTeclas();

        float cad = game.tracker.rpm();
        if (!pausado) {
            relogio += delta;
            scroll += cad * 10f * delta;
            luta.update(cad, delta);
            enviarSinal();
            if (luta.terminou()) {
                tempoFim += delta;
                if (tempoFim >= TEMPO_FIM) {
                    game.setScreen(new ResultScreen(game, luta.resultado(), idxFase));
                    return;
                }
            }
        }
        desenhar(cad);
    }

    private void tratarTeclas() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!luta.terminou()) pausado = !pausado;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) game.setScreen(new GameScreen(game, idxFase));
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) game.setScreen(new MenuScreen(game));
    }

    private void enviarSinal() {
        char c = luta.sinal == LutaPesca.Sinal.OK ? 'V' : luta.sinal == LutaPesca.Sinal.DEVAGAR ? 'A' : 'R';
        if (c != ultimoSinal) {
            game.input.sinal(c);
            ultimoSinal = c;
        }
    }

    // ------------------------------------------------------------ desenho

    private void desenhar(float cad) {
        game.limpar(Cores.CEU);
        game.aplicarCamera();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        ShapeRenderer s = game.shapes;
        float tremor = luta.rompeuHa < 0.4f ? (float) Math.sin(relogio * 80f) * 6f : 0f;

        s.begin(ShapeType.Filled);
        desenharCenario(s);
        float boatX = 220f + 120f * (luta.estado == Estado.AQUECIMENTO ? luta.progressoAquec : 1f);
        desenharBarco(s, boatX);
        desenharLinhaEPeixe(s, boatX, tremor);
        desenharMedidorTensao(s);
        desenharMedidorCadencia(s, cad);
        if (pausado) {
            s.setColor(Cores.VEU);
            s.rect(0, 0, Main.W, Main.H);
        }
        s.end();

        game.batch.begin();
        desenharTextos(cad);
        game.batch.end();
    }

    private void desenharCenario(ShapeRenderer s) {
        s.setColor(Cores.SOL);
        s.circle(1100, 620, 55, 32);

        s.setColor(Cores.BRANCO);
        for (int i = 0; i < 3; i++) {
            float vel = 0.6f + i * 0.3f;
            float cx = ((i * 520f - scroll * vel) % 1500f + 1500f) % 1500f - 150f;
            float cy = 560 + i * 40;
            s.ellipse(cx, cy, 160, 50);
            s.circle(cx + 50, cy + 35, 30);
            s.circle(cx + 100, cy + 30, 26);
        }

        s.setColor(Cores.AGUA1);
        s.rect(0, 0, Main.W, AGUA_Y);
        for (float x = -20; x < Main.W + 40; x += 40) {
            float y = AGUA_Y + (float) Math.sin(relogio * 2f + (x + scroll * 3f) * 0.03f) * 6f;
            s.circle(x, y, 22, 16);
        }
        s.setColor(Cores.AGUA2);
        s.rect(0, 0, Main.W, 230);
        s.setColor(Cores.AGUA3);
        s.rect(0, 0, Main.W, 110);
    }

    private void desenharBarco(ShapeRenderer s, float x) {
        float y = AGUA_Y + (float) Math.sin(relogio * 2f) * 4f;
        // casco (trapezio = 2 triangulos)
        s.setColor(Cores.BARCO);
        s.triangle(x - 90, y, x + 90, y, x + 60, y - 40);
        s.triangle(x - 90, y, x + 60, y - 40, x - 60, y - 40);
        // pescador: corpo (retangulo) + cabeca (circulo)
        s.setColor(Cores.ROUPA);
        s.rect(x - 14, y + 4, 28, 48);
        s.setColor(Cores.PELE);
        s.circle(x, y + 66, 15, 20);
        // vara
        s.setColor(Cores.PRETO);
        s.rectLine(x + 14, y + 36, x + 110, y + 110, 4);
    }

    private void desenharLinhaEPeixe(ShapeRenderer s, float boatX, float tremor) {
        float pontaX = boatX + 110, pontaY = AGUA_Y + 110;
        float hx, hy;
        if (luta.estado == Estado.AQUECIMENTO) {
            hx = pontaX; hy = 250;
        } else {
            hx = boatX + 140 + luta.distancia * 700f + tremor;
            hy = 215 + (float) Math.sin(relogio * 3f) * 10f;
        }

        s.setColor(Cores.BRANCO);
        s.rectLine(pontaX, pontaY, hx, hy, luta.tensao > 0.85f ? 3f : 1.5f);
        s.setColor(Cores.PRETO);
        s.circle(hx, hy, 6, 12);

        if (luta.estado == Estado.AQUECIMENTO) {
            // boia-destino
            s.setColor(Cores.ALERTA);
            s.circle(1000, AGUA_Y + 5, 16, 16);
            return;
        }
        float fw = 50f * fase.escalaPeixe, fh = 26f * fase.escalaPeixe;
        s.setColor(Cores.PEIXE[idxFase]);
        s.ellipse(hx - fw / 2f, hy - fh / 2f, fw, fh, 24);
        s.triangle(hx + fw / 2f - 4, hy, hx + fw / 2f + fw * 0.35f, hy + fh * 0.5f, hx + fw / 2f + fw * 0.35f, hy - fh * 0.5f);
        s.setColor(Cores.PRETO);
        s.circle(hx - fw / 4f, hy + fh * 0.12f, 3f + fase.escalaPeixe, 12);
    }

    private void desenharMedidorTensao(ShapeRenderer s) {
        float x = 1170, y = 120, w = 44, h = 420;
        s.setColor(Cores.CINZA);
        s.rect(x, y, w, h);
        s.setColor(Cores.ZONA);
        s.rect(x, y + h * 0.35f, w, h * 0.30f);
        boolean piscar = luta.tensao > 0.85f && ((int) (relogio * 8f)) % 2 == 0;
        s.setColor(piscar ? Cores.ALERTA : Cores.BRANCO);
        float my = y + h * Math.max(0f, Math.min(1f, luta.tensao));
        s.rect(x - 8, my - 5, w + 16, 10);
    }

    private void desenharMedidorCadencia(ShapeRenderer s, float cad) {
        float x = 260, y = 40, w = 760, h = 34;
        float max = luta.alvo * 1.8f;
        s.setColor(Cores.CINZA);
        s.rect(x, y, w, h);
        s.setColor(Cores.ZONA);
        float z0 = x + w * Math.min(1f, luta.zonaMin / max);
        float z1 = x + w * Math.min(1f, luta.zonaMax / max);
        s.rect(z0, y, z1 - z0, h);
        boolean fora = luta.sinal != LutaPesca.Sinal.OK;
        s.setColor(fora ? Cores.ALERTA : Cores.BRANCO);
        float cx = x + w * Math.max(0f, Math.min(1f, cad / max));
        s.rect(cx - 5, y - 8, 10, h + 16);
    }

    private void desenharTextos(float cad) {
        game.texto("Fase " + fase.numero + " — " + fase.nome, 30, 700, 1.5f, Cores.PRETO);
        game.texto("Tempo na zona: " + Math.round(luta.tempoLuta > 0 ? 100f * luta.tempoZona / luta.tempoLuta : 0) + "%",
                30, 665, 1.2f, Cores.PRETO);
        game.texto("Linhas rompidas: " + luta.rompidas + " / " + LutaPesca.MAX_ROMPIDAS, 30, 635, 1.2f, Cores.PRETO);
        game.texto("Cadência: " + Math.round(cad) + " RPM  (zona " + Math.round(luta.zonaMin) + "–" + Math.round(luta.zonaMax) + ")",
                Main.W - 470, 700, 1.2f, Cores.PRETO);
        game.textoCentro("TENSÃO", 1192, 580, 1.0f, Cores.BRANCO);

        String msg;
        Color cor;
        switch (luta.sinal) {
            case OK:      msg = "RITMO OK";       cor = Cores.BRANCO; break;
            case RAPIDO:  msg = "MAIS DEVAGAR";   cor = Cores.ALERTA; break;
            default:      msg = "MAIS RÁPIDO";    cor = Cores.ALERTA; break;
        }
        game.textoCentro(msg, 640, 125, 1.4f, cor);

        if (luta.estado == Estado.AQUECIMENTO) {
            game.textoCentro("Aquecimento: pedale suavemente até a boia", 640, 560, 1.4f, Cores.PRETO);
        } else if (fase.intervalado && !luta.terminou()) {
            boolean puxando = (luta.tempoLuta % 16f) < 9f;
            game.textoCentro(puxando ? "O peixe está puxando!" : "O peixe está descansando", 640, 560, 1.4f, Cores.PRETO);
        }
        if (luta.terminou()) {
            String fim = luta.estado == Estado.CAPTURADO ? "Peixe capturado!"
                    : luta.estado == Estado.ROMPEU ? "A linha rompeu" : "O peixe escapou";
            game.textoCentro(fim, 640, 450, 3.0f, Cores.PRETO);
        }
        if (pausado) {
            game.textoCentro("PAUSADO", 640, 450, 3.0f, Cores.BRANCO);
            game.textoCentro("P = continuar    R = reiniciar fase    M = menu", 640, 380, 1.3f, Cores.BRANCO);
        } else {
            game.texto("P = pausar   R = reiniciar   M = menu", 30, 30, 1.0f, Cores.BRANCO);
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        // a fase terminou ou foi abandonada: apaga os LEDs do Arduino
        game.input.sinal('X');
    }
}
