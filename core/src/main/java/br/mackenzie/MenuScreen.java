package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** Menu principal: iniciar, calibrar, escolher a entrada (teclado/Arduino) e sair. */
public class MenuScreen extends ScreenAdapter {

    private static final int INICIAR = 0, CALIBRAR = 1, ENTRADA = 2, SAIR = 3;

    private final Main game;
    private final Rectangle[] botoes = new Rectangle[4];
    private final Vector2 toque = new Vector2();

    public MenuScreen(Main game) {
        this.game = game;
        for (int i = 0; i < botoes.length; i++) {
            botoes[i] = new Rectangle(380, 400 - i * 95, 520, 70);
        }
    }

    @Override
    public void render(float delta) {
        game.input.update();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) acionar(INICIAR);
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) acionar(CALIBRAR);
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) acionar(ENTRADA);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) acionar(SAIR);
        if (Gdx.input.justTouched()) {
            toque.set(Gdx.input.getX(), Gdx.input.getY());
            game.viewport.unproject(toque);
            for (int i = 0; i < botoes.length; i++) {
                if (botoes[i].contains(toque)) acionar(i);
            }
        }

        game.limpar(Cores.MENU_BG);
        game.aplicarCamera();

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle b : botoes) {
            game.shapes.setColor(Cores.ZONA);
            game.shapes.rect(b.x, b.y, b.width, b.height);
        }
        // peixe decorativo
        game.shapes.setColor(Cores.PEIXE[1]);
        game.shapes.ellipse(1000, 560, 120, 60, 24);
        game.shapes.triangle(1115, 590, 1160, 620, 1160, 560);
        game.shapes.setColor(Cores.PRETO);
        game.shapes.circle(1030, 595, 5, 12);
        game.shapes.end();

        game.batch.begin();
        game.textoCentro("PESCA NO RITMO", Main.W / 2f, 660, 3.2f, Cores.BRANCO);
        game.textoCentro("Pedale em ritmo constante para recolher a linha", Main.W / 2f, 590, 1.4f, Cores.CEU);
        String[] rotulos = {
            "Iniciar jogo  (ENTER)",
            "Calibrar cadência  (C)",
            "Entrada: " + game.input.nome() + "  (I)",
            "Sair  (ESC)"
        };
        for (int i = 0; i < botoes.length; i++) {
            Rectangle b = botoes[i];
            game.textoCentro(rotulos[i], b.x + b.width / 2f, b.y + 45, 1.3f, Cores.BRANCO);
        }
        game.textoCentro("Cadência-alvo: " + Math.round(game.alvoRpm) + " RPM"
                + (game.calibrado ? "" : "  (padrão, ainda não calibrada)"),
                Main.W / 2f, 150, 1.2f, Cores.BRANCO);
        if (!game.statusEntrada.isEmpty()) {
            game.textoCentro(game.statusEntrada, Main.W / 2f, 115, 1.1f, Cores.ALERTA);
        }
        game.textoCentro("No jogo: P = pausar   R = reiniciar fase   M = menu", Main.W / 2f, 60, 1.0f, Cores.CEU);
        game.batch.end();
    }

    private void acionar(int acao) {
        switch (acao) {
            case INICIAR:
                if (game.calibrado) game.setScreen(new GameScreen(game, 0));
                else game.setScreen(new CalibrationScreen(game, true));
                break;
            case CALIBRAR:
                game.setScreen(new CalibrationScreen(game, false));
                break;
            case ENTRADA:
                game.trocarEntrada();
                break;
            case SAIR:
                Gdx.app.exit();
                break;
            default:
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }
}
