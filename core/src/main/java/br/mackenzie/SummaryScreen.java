package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Resumo final: barras de tempo na zona e pontuacao por fase (figura util para o paper). */
public class SummaryScreen extends ScreenAdapter {

    private final Main game;

    public SummaryScreen(Main game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            for (int i = 0; i < game.ultimos.length; i++) game.ultimos[i] = null;
            game.setScreen(new MenuScreen(game));
        }

        game.limpar(Cores.MENU_BG);
        game.aplicarCamera();

        float baseY = 180, alturaMax = 280, larg = 140;
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < Fase.TODAS.length; i++) {
            Resultado r = game.ultimos[i];
            float x = 330 + i * 250;
            game.shapes.setColor(Cores.CINZA);
            game.shapes.rect(x, baseY, larg, alturaMax);
            if (r != null) {
                game.shapes.setColor(Cores.ZONA);
                game.shapes.rect(x, baseY, larg, alturaMax * r.pctZona);
            }
        }
        game.shapes.end();

        game.batch.begin();
        game.textoCentro("Resumo da sessão", Main.W / 2f, 660, 2.6f, Cores.BRANCO);
        game.textoCentro("Tempo na zona-alvo por fase", Main.W / 2f, 600, 1.4f, Cores.CEU);
        int total = 0;
        for (int i = 0; i < Fase.TODAS.length; i++) {
            Resultado r = game.ultimos[i];
            float cx = 330 + i * 250 + 70;
            game.textoCentro("Fase " + (i + 1), cx, baseY - 15, 1.2f, Cores.BRANCO);
            if (r != null) {
                total += r.score;
                game.textoCentro(Math.round(r.pctZona * 100) + "%", cx, baseY + alturaMax + 35, 1.4f, Cores.BRANCO);
                game.textoCentro(r.score + " pts", cx, baseY - 50, 1.2f, Cores.SOL);
            } else {
                game.textoCentro("—", cx, baseY + alturaMax + 35, 1.4f, Cores.CINZA);
            }
        }
        game.textoCentro("Pontuação total: " + total, Main.W / 2f, 95, 1.8f, Cores.SOL);
        game.textoCentro("Dados salvos em ~/.pesca-no-ritmo/sessoes_pesca.csv   |   ENTER = menu", Main.W / 2f, 50, 1.0f, Cores.CEU);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }
}
