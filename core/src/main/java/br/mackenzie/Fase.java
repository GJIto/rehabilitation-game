package br.mackenzie;

/** Parametros de cada fase. Toda a dificuldade do jogo esta configurada aqui. */
public final class Fase {

    public final int numero;
    public final String nome;
    /** Meia-largura da zona-alvo, como fracao da cadencia-alvo (0,25 = +-25%). */
    public final float zonaMeia;
    /** Amplitude da oscilacao suave da zona (fase 2). */
    public final float oscilacao;
    /** Se true, a zona alterna entre "peixe puxando" (rapido) e "peixe descansando" (lento). */
    public final boolean intervalado;
    /** Distancia inicial do peixe (0 = no barco, 1 = fugiu). */
    public final float distInicial;
    /** Quanto o peixe se aproxima por segundo com o ritmo na zona. */
    public final float velRecolher;
    /** Quanto o peixe se afasta por segundo com o ritmo abaixo da zona. */
    public final float velFuga;
    /** Tempo maximo da luta (s) antes do peixe escapar. */
    public final float tempoMax;
    /** Tamanho do peixe (apenas visual). */
    public final float escalaPeixe;

    public static final Fase[] TODAS = {
        new Fase(1, "Lago calmo",  0.25f, 0.00f, false, 0.70f, 0.035f, 0.030f,  90f, 1.0f),
        new Fase(2, "Rio",         0.18f, 0.08f, false, 0.70f, 0.030f, 0.035f, 100f, 1.5f),
        new Fase(3, "Mar aberto",  0.12f, 0.00f, true,  0.75f, 0.026f, 0.040f, 120f, 2.2f),
    };

    private Fase(int numero, String nome, float zonaMeia, float oscilacao, boolean intervalado,
                 float distInicial, float velRecolher, float velFuga, float tempoMax, float escalaPeixe) {
        this.numero = numero;
        this.nome = nome;
        this.zonaMeia = zonaMeia;
        this.oscilacao = oscilacao;
        this.intervalado = intervalado;
        this.distInicial = distInicial;
        this.velRecolher = velRecolher;
        this.velFuga = velFuga;
        this.tempoMax = tempoMax;
        this.escalaPeixe = escalaPeixe;
    }

    /** Multiplicador da cadencia-alvo no instante t (s) da luta. */
    public float fatorAlvo(float t) {
        if (intervalado) {
            float ciclo = t % 16f;               // 9 s puxando, 7 s descansando
            return ciclo < 9f ? 1.12f : 0.85f;
        }
        return 1f + oscilacao * (float) Math.sin(t * 0.5f);
    }
}
