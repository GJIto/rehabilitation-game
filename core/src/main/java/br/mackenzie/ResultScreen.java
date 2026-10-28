package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;

/** Resultado da fase + pergunta de percepcao de esforco (PSE, 0 a 10). */
public class ResultScreen extends ScreenAdapter {

    private final Main game;
    private final Resultado r;
    private final int idxFase;
    private int pse = 3;

    public ResultScreen(Main game, Resultado r, int idxFase) {
        this.game = game;
        this.r = r;
        this.idxFase = idxFase;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) pse = Math.min(10, pse + 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) pse = Math.max(0, pse - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) avancar();
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            registrar();
            game.setScreen(new MenuScreen(game));
        }

        game.limpar(Cores.MENU_BG);
        game.aplicarCamera();
        game.batch.begin();
        String titulo = r.capturou ? "Peixe capturado!" : "O peixe escapou";
        game.textoCentro("Fase " + r.fase + " — " + Fase.TODAS[idxFase].nome, Main.W / 2f, 660, 1.5f, Cores.CEU);
        game.textoCentro(titulo, Main.W / 2f, 600, 2.6f, Cores.BRANCO);
        game.textoCentro("Pontuação: " + r.score, Main.W / 2f, 510, 2.0f, Cores.SOL);

        float x = 400;
        game.texto("Tempo na zona-alvo: " + Math.round(r.pctZona * 100) + "%", x, 430, 1.4f, Cores.BRANCO);
        game.texto("Regularidade (variação da cadência): " + Math.round(r.cv * 100) + "%", x, 385, 1.4f, Cores.BRANCO);
        game.texto("Duração da luta: " + Math.round(r.tempoLuta) + " s", x, 340, 1.4f, Cores.BRANCO);
        game.texto("Linhas rompidas: " + r.rompidas, x, 295, 1.4f, Cores.BRANCO);
        game.texto("Cadência-alvo: " + Math.round(r.alvoRpm) + " RPM", x, 250, 1.4f, Cores.BRANCO);

        game.textoCentro("Quão cansativo foi? (0 = nada, 10 = máximo)   ← " + pse + " →   (setas)",
                Main.W / 2f, 160, 1.3f, Cores.ALERTA);
        String proxima = !r.capturou ? "tentar de novo"
                : (idxFase + 1 < Fase.TODAS.length ? "próxima fase" : "ver resumo");
        game.textoCentro("ENTER = " + proxima + "     M = menu", Main.W / 2f, 90, 1.2f, Cores.CEU);
        game.batch.end();
    }

    private void registrar() {
        r.pse = pse;
        game.logger.registrar(r);
        if (r.capturou) game.ultimos[idxFase] = r;
    }

    private void avancar() {
        registrar();
        if (!r.capturou) {
            game.setScreen(new GameScreen(game, idxFase));
        } else if (idxFase + 1 < Fase.TODAS.length) {
            game.setScreen(new GameScreen(game, idxFase + 1));
        } else {
            game.setScreen(new SummaryScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }
}
