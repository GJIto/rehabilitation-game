package br.pesca;

/**
 * Converte pulsos (1 pulso = 1 volta da pedivela) em cadencia suavizada (RPM).
 * Pode ser alimentado por qualquer thread (serial ou teclado); a leitura e feita na thread de render.
 */
public class CadenceTracker {

    private static final long DEBOUNCE_NS = 200_000_000L;   // ignora pulsos < 200 ms (> 300 RPM)
    private static final long PARADO_NS   = 4_000_000_000L; // sem pulso ha 4 s = parado

    private final long[] ts = new long[4];
    private int n;
    private float suave;

    public synchronized void pulse() {
        pulse(System.nanoTime());
    }

    synchronized void pulse(long agora) {
        if (n > 0 && agora - ts[n - 1] < DEBOUNCE_NS) return;
        if (n == ts.length) {
            System.arraycopy(ts, 1, ts, 0, n - 1);
            n--;
        }
        ts[n++] = agora;
    }

    private synchronized float rpmBruto(long agora) {
        if (n < 2) return 0f;
        long desdeUltimo = agora - ts[n - 1];
        if (desdeUltimo > PARADO_NS) return 0f;
        float media = (ts[n - 1] - ts[0]) / (float) (n - 1);
        // se o pulso esta atrasado, a cadencia cai gradualmente em vez de "congelar"
        float intervalo = Math.max(media, desdeUltimo);
        return 60e9f / intervalo;
    }

    /** Chamar uma vez por frame. */
    public void update(float dt) {
        update(dt, System.nanoTime());
    }

    void update(float dt, long agora) {
        float bruto = rpmBruto(agora);
        suave += (bruto - suave) * Math.min(1f, dt * 3f);
        if (bruto == 0f && suave < 1f) suave = 0f;
    }

    /** Cadencia atual suavizada, em RPM. */
    public float rpm() {
        return suave;
    }

    public synchronized void reset() {
        n = 0;
        suave = 0f;
    }
}
