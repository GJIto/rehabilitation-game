package br.pesca;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Calibracao: o paciente pedala confortavelmente por 20 s e a media (apos 5 s de adaptacao)
 * vira a cadencia-alvo. Cada paciente tem seu proprio ritmo de referencia.
 */
public class CalibrationScreen extends ScreenAdapter {

    private static final float DURACAO = 20f, DESCARTE = 5f;
    private static final float ALVO_MIN = 30f, ALVO_MAX = 80f;

    private final PescaGame game;
    private final boolean iniciarDepois;
    private boolean comecou;
    private float t;
    private double soma;
    private int amostras;

    public CalibrationScreen(PescaGame game, boolean iniciarDepois) {
        this.game = game;
        this.iniciarDepois = iniciarDepois;
        game.tracker.reset();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);
        game.input.update();
        game.tracker.update(delta);
        float rpm = game.tracker.rpm();

        if (!comecou && rpm > 0f) comecou = true;
        if (comecou) {
            t += delta;
            if (t > DESCARTE) {
                soma += rpm;
                amostras++;
            }
            if (t >= DURACAO) concluir(true);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && amostras > 30) concluir(true);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) concluir(false);

        game.limpar(Cores.MENU_BG);
        game.aplicarCamera();

        float progresso = Math.min(1f, t / DURACAO);
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(Cores.CINZA);
        game.shapes.rect(240, 250, 800, 40);
        game.shapes.setColor(Cores.ZONA);
        game.shapes.rect(240, 250, 800 * progresso, 40);
        game.shapes.setColor(Cores.ALERTA);
        float marca = 240 + 800 * Math.min(1f, rpm / 100f);
        game.shapes.rect(marca - 4, 235, 8, 70);
        game.shapes.end();

        game.batch.begin();
        game.textoCentro("CALIBRAÇÃO", PescaGame.W / 2f, 640, 2.6f, Cores.BRANCO);
        game.textoCentro("Pedale em um ritmo confortável, como se fosse durante um passeio.",
                PescaGame.W / 2f, 560, 1.3f, Cores.CEU);
        game.textoCentro(comecou ? "Mantenha o ritmo..." : "Comece a pedalar (a contagem inicia sozinha)",
                PescaGame.W / 2f, 500, 1.3f, Cores.BRANCO);
        game.textoCentro("Cadência atual: " + Math.round(rpm) + " RPM", PescaGame.W / 2f, 400, 1.8f, Cores.BRANCO);
        game.textoCentro(Math.max(0, Math.round(DURACAO - t)) + " s restantes", PescaGame.W / 2f, 190, 1.2f, Cores.CEU);
        game.textoCentro("ESC = pular (usar " + Math.round(game.alvoRpm) + " RPM)", PescaGame.W / 2f, 80, 1.0f, Cores.CEU);
        game.batch.end();
    }

    private void concluir(boolean medido) {
        if (medido && amostras > 30) {
            float media = (float) (soma / amostras);
            game.alvoRpm = Math.max(ALVO_MIN, Math.min(ALVO_MAX, media));
        }
        game.calibrado = true;
        if (iniciarDepois) game.setScreen(new GameScreen(game, 0));
        else game.setScreen(new MenuScreen(game));
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }
}
