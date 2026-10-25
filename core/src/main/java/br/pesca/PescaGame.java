package br.pesca;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fazecast.jSerialComm.SerialPort;

/**
 * Ponto de entrada e dono dos recursos compartilhados (um unico ShapeRenderer, SpriteBatch e fonte
 * para todas as telas), alem da entrada de pedalada e dos dados da sessao.
 */
public class PescaGame extends Game {

    public static final float W = 1280f, H = 720f;

    public ShapeRenderer shapes;
    public SpriteBatch batch;
    public BitmapFont font;
    public OrthographicCamera camera;
    public Viewport viewport;
    private final GlyphLayout layout = new GlyphLayout();

    public final CadenceTracker tracker = new CadenceTracker();
    public final SessionLogger logger = new SessionLogger();
    public PulseSource input;
    public String statusEntrada = "";
    private int idxEntrada = -1;                 // -1 = teclado; 0..n-1 = portas seriais

    /** Cadencia-alvo do paciente (RPM), definida na calibracao. */
    public float alvoRpm = 50f;
    public boolean calibrado = false;
    /** Ultimo resultado capturado em cada fase (usado na tela de resumo). */
    public final Resultado[] ultimos = new Resultado[Fase.TODAS.length];

    @Override
    public void create() {
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        camera = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);
        input = new KeyboardPulseSource(tracker);
        setScreen(new MenuScreen(this));
    }

    // ---------- entrada ----------

    /** Alterna entre teclado e cada porta serial encontrada. */
    public void trocarEntrada() {
        SerialPort[] portas;
        try {
            portas = SerialPort.getCommPorts();
        } catch (Throwable t) {
            portas = new SerialPort[0];
        }
        input.close();
        tracker.reset();
        idxEntrada++;
        if (idxEntrada >= portas.length) idxEntrada = -1;

        if (idxEntrada >= 0) {
            SerialPulseSource serial = new SerialPulseSource(portas[idxEntrada], tracker);
            if (serial.abrir()) {
                input = serial;
                statusEntrada = "";
                return;
            }
            statusEntrada = "Não foi possível abrir " + portas[idxEntrada].getSystemPortName();
            idxEntrada = -1;
        } else if (portas.length == 0) {
            statusEntrada = "Nenhuma porta serial encontrada";
        }
        input = new KeyboardPulseSource(tracker);
    }

    // ---------- helpers de desenho ----------

    public void aplicarCamera() {
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
    }

    public void limpar(Color c) {
        Gdx.gl.glClearColor(c.r, c.g, c.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /** O batch precisa estar com begin() chamado. */
    public void texto(String s, float x, float y, float escala, Color cor) {
        font.getData().setScale(escala);
        font.setColor(cor);
        font.draw(batch, s, x, y);
    }

    public void textoCentro(String s, float cx, float y, float escala, Color cor) {
        font.getData().setScale(escala);
        layout.setText(font, s);
        font.setColor(cor);
        font.draw(batch, s, cx - layout.width / 2f, y);
    }

    @Override
    public void dispose() {
        super.dispose();
        input.close();
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
