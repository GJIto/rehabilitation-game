package br.pesca;

/** Metricas de uma tentativa de fase. E isto que mede a evolucao do paciente. */
public final class Resultado {
    public final int fase;
    public final boolean capturou;
    public final int score;
    /** Fracao do tempo de luta com a cadencia dentro da zona-alvo (0 a 1). */
    public final float pctZona;
    /** Coeficiente de variacao da cadencia (desvio padrao / media). Menor = mais regular. */
    public final float cv;
    public final float tempoLuta;
    public final int rompidas;
    public final float alvoRpm;
    /** Percepcao subjetiva de esforco (0 a 10), preenchida na tela de resultado. */
    public int pse = -1;

    public Resultado(int fase, boolean capturou, int score, float pctZona, float cv,
                     float tempoLuta, int rompidas, float alvoRpm) {
        this.fase = fase;
        this.capturou = capturou;
        this.score = score;
        this.pctZona = pctZona;
        this.cv = cv;
        this.tempoLuta = tempoLuta;
        this.rompidas = rompidas;
        this.alvoRpm = alvoRpm;
    }
}
